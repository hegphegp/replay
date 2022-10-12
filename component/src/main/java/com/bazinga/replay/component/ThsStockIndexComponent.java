package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.dto.BlockStockDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.StockIndexQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.HistoryBlockInfoService;
import com.bazinga.replay.service.StockIndexService;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsStockIndexComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private ThsBlockKbarComponent thsBlockKbarComponent;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    /**
     * 上证macd diff dea
     */
    public void shMACDIndex(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(new Date());
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> list = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            list.add(tradeDateStr);
        }
        String stockCode = "399300";
        String stockName = "沪深300";
        List<StockIndex> szIndexs = thsDataComponent.initStockIndex(stockCode+".SZ", stockName, list);
        int ret = thsDataComponent.thsLogin();
        int i = 0;
        for (StockIndex stockIndex:szIndexs){
            i++;
            System.out.println(stockIndex.getKbarDate()+"========"+i);
            getStockIndex(stockCode,stockName,stockIndex.getKbarDate(),stockIndex,".SZ");
            stockIndexService.save(stockIndex);
        }
        thsDataComponent.thsLoginOut();
    }

    /**
     * 板块macd diff dea
     */
    public void blockMACDIndex(){
        Date date = new Date();
        String endDate = DateUtil.format(date, DateUtil.yyyyMMdd);
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20170101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate(endDate,DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> list = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            list.add(tradeDateStr);
        }
        HistoryBlockInfoQuery historyBlockInfoQuery = new HistoryBlockInfoQuery();
        historyBlockInfoQuery.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(historyBlockInfoQuery);
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            System.out.println(blockInfo.getBlockCode());
            String blockCode = blockInfo.getBlockCode();
            String thsBlockCode = MarketUtil.generalToThsStock(blockCode);
            Date marketDate = DateUtil.parseDate(blockInfo.getMarketDate(), DateUtil.yyyyMMdd);
            StockIndexQuery stockIndexQuery = new StockIndexQuery();
            stockIndexQuery.setStockCode(thsBlockCode);
            List<String> initeds = Lists.newArrayList();
            List<StockIndex> stockIndices = stockIndexService.listByCondition(stockIndexQuery);
            if(stockIndices!=null) {
                for (StockIndex stockIndex : stockIndices) {
                    initeds.add(stockIndex.getKbarDate());
                }
            }
            List<String> dates = Lists.newArrayList();
            for (String tradeDate:list){
                Date listDate = DateUtil.parseDate(tradeDate, DateUtil.yyyy_MM_dd);
                String yyyymmdd = tradeDate.replace("-", "");
                if((!listDate.before(marketDate))&&(!initeds.contains(yyyymmdd))){
                    dates.add(tradeDate);
                }
            }
            List<StockIndex> szIndexs = thsDataComponent.initStockIndex(thsBlockCode, blockInfo.getBlockName(), dates);
            for (StockIndex stockIndex:szIndexs){
                stockIndexService.save(stockIndex);
            }
        }

    }

    public void getStockIndex(String blockCode,String blockName,String tradeDate,StockIndex stockIndex,String diff){
        System.out.println(blockCode+"_"+blockName+"_"+tradeDate);
        String quote_str = JDIBridge.THS_BasicData(blockCode+diff, "ths_bias_index;ths_macd_index", tradeDate+",6,100;"+tradeDate+",26,12,9,102,100");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> biass = tableInfo.getJSONArray("ths_bias_index").toJavaList(BigDecimal.class);
            List<BigDecimal> macds = tableInfo.getJSONArray("ths_macd_index").toJavaList(BigDecimal.class);
            BigDecimal bias = biass.get(0);
            BigDecimal macd = macds.get(0);
            stockIndex.setBias6(bias);
            //stockIndex.setMacd(macd);
        }
        String quote_str1 = JDIBridge.THS_BasicData(blockCode+diff, "ths_bias_index", tradeDate+",12,100");
        if(!StringUtils.isEmpty(quote_str1)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str1);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> biass = tableInfo.getJSONArray("ths_bias_index").toJavaList(BigDecimal.class);
            BigDecimal bias = biass.get(0);
            stockIndex.setBias12(bias);
        }

        String quote_str2 = JDIBridge.THS_BasicData(blockCode+diff, "ths_bias_index", tradeDate+",24,100");
        if(!StringUtils.isEmpty(quote_str2)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str2);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> biass = tableInfo.getJSONArray("ths_bias_index").toJavaList(BigDecimal.class);
            BigDecimal bias = biass.get(0);
            stockIndex.setBias24(bias);
        }

    }



}
