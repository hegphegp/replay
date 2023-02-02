package com.bazinga.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.IndexDetailQuery;
import com.bazinga.replay.query.StockIndexQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.HistoryBlockInfoService;
import com.bazinga.replay.service.IndexDetailService;
import com.bazinga.replay.service.StockIndexService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import com.tradex.model.suport.F10Cates;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsBlockStocksComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private IndexDetailService indexDetailService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    /**
     * 指数股票明细
     */
    public void indexBLockDetail(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(new Date()));
        query.setTradeDateTo(DateTimeUtils.getDate235959(new Date()));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        List<String> list = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            list.add(tradeDateStr);
        }
        //List<String> list = Lists.newArrayList("2022-12-29");
        String stockCode = "000300";
        String diff = ".SH";
        String blockName = "沪深300";
        getBlockDetail(stockCode,diff,blockName,list);
        thsDataComponent.thsLoginOut();
    }


    public void getBlockDetail(String blockCode,String diff,String blockName,List<String> dates){
        int ret = thsDataComponent.thsLogin();
        for (String dateStr:dates){
            System.out.println(blockCode+"_"+blockName+"_"+dateStr);
            String quote_str = JDIBridge.THS_DataPool("index_weight",dateStr+";"+blockCode+diff,"thscode:Y,security_name:Y,weight:Y");
            if(!StringUtils.isEmpty(quote_str)){
                JSONObject jsonObject = JSONObject.parseObject(quote_str);
                JSONArray tables = jsonObject.getJSONArray("tables");
                if(tables==null||tables.size()==0){
                    return;
                }
                JSONObject tableJson = tables.getJSONObject(0);
                JSONObject tableInfo = tableJson.getJSONObject("table");
                List<String> stockCodes = tableInfo.getJSONArray("thscode").toJavaList(String.class);
                List<String> stockNames = tableInfo.getJSONArray("security_name").toJavaList(String.class);
                int i = 0;
                if(!CollectionUtils.isEmpty(stockCodes)){
                    for (String stockCode:stockCodes){
                        String formatStockCode = formatStockCode(stockCode);
                        IndexDetail indexDetail = new IndexDetail();
                        indexDetail.setBlockName(blockName);
                        indexDetail.setIndexCode(blockCode);
                        indexDetail.setStockCode(formatStockCode);
                        indexDetail.setKbarDate(dateStr);
                        indexDetail.setStockName(stockNames.get(i));
                        indexDetailService.save(indexDetail);
                        i++;
                    }
                }

            }
        }
        thsDataComponent.thsLoginOut();
    }
    public String formatStockCode(String stockCode){
        String replace = stockCode.replace(".SH", "");
        String formatStockCode = replace.replace(".SZ", "");
        return formatStockCode;
    }

    /**
     * 指数股票明细
     */
    public void preIndexBLockDetail(String preDate,String date){
        IndexDetailQuery query = new IndexDetailQuery();
        query.setKbarDate(preDate);
        List<IndexDetail> details = indexDetailService.listByCondition(query);
        for (IndexDetail detail:details){
            IndexDetail indexDetail = new IndexDetail();
            indexDetail.setStockName(detail.getStockName());
            indexDetail.setStockCode(detail.getStockCode());
            indexDetail.setIndexCode(detail.getIndexCode());
            indexDetail.setBlockName(detail.getBlockName());
            indexDetail.setKbarDate(date);
            indexDetail.setCreateTime(new Date());
            indexDetailService.save(indexDetail);
        }
    }



}
