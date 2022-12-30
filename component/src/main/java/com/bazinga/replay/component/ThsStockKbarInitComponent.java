package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.query.ThsStockKbarQuery;
import com.bazinga.replay.service.ThsCirculateInfoService;
import com.bazinga.replay.service.ThsStockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.ThsStockUtils;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static Ths.JDIBridge.THS_HistoryQuotes;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsStockKbarInitComponent {
    @Autowired
    private ThsLoginComponent thsLoginComponent;
    @Autowired
    private ThsStockKbarService thsStockKbarService;
    @Autowired
    private ThsCirculateInfoService circulateInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private ThsCirculateInfoComponent thsCirculateInfoComponent;
    @Autowired
    private ThsStockKbarComponent thsStockKbarComponent;
    public void initIndexStockKbarinfo(String startTradeDate,String endTradeDate){
        String format = "20180102";
        ThsCirculateInfoQuery query = new ThsCirculateInfoQuery();
        query.setTradeDate(format);
        List<ThsCirculateInfo> circulateInfos = circulateInfoService.listByCondition(query);
        if(CollectionUtils.isEmpty(circulateInfos)){
            return;
        }
        thsLoginComponent.thsLogin();
        int i = 0;
        for(ThsCirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(format+"========"+circulateInfo.getStockCode()+"====="+i);
            ThsStockKbarQuery stockKbarQuery = new ThsStockKbarQuery();
            stockKbarQuery.setStockCode(circulateInfo.getStockCode());
            List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(stockKbarQuery);
            if(!CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            String start = DateUtil.dateStringFormat(startTradeDate, DateUtil.yyyyMMdd, DateUtil.yyyy_MM_dd);
            String end = DateUtil.dateStringFormat(endTradeDate, DateUtil.yyyyMMdd, DateUtil.yyyy_MM_dd);
            initCurrentStockKbar(circulateInfo.getStockCode(),circulateInfo.getStockName(),start,end);
        }
        thsLoginComponent.thsLoginOut();
    }

    public void circulateCheckStockAndUpdate(String tradeDateStr){
        List<ThsCirculateInfo> circulateInfos = thsCirculateInfoComponent.getMarketACirculate(tradeDateStr);
        List<ThsStockKbar> stockKbars = thsStockKbarComponent.getDayStockKbars(tradeDateStr);
        Map<String, ThsStockKbar> stockKbarMap = stockKbars.stream().collect(Collectors.toMap(ThsStockKbar::getStockCode, stockKbar -> stockKbar));
        for (ThsCirculateInfo circulateInfo:circulateInfos){
            ThsStockKbar thsStockKbar = stockKbarMap.get(circulateInfo.getStockCode());
            if(thsStockKbar==null){
                System.out.println(circulateInfo.getStockCode()+"===="+circulateInfo.getStockName()+"===="+tradeDateStr+"====遗漏数据日期");
                String dateyyyy_MM_dd = DateUtil.dateStringFormat(tradeDateStr, DateUtil.yyyyMMdd, DateUtil.yyyy_MM_dd);
                initCurrentStockKbar(circulateInfo.getStockCode(),circulateInfo.getStockName(),dateyyyy_MM_dd,dateyyyy_MM_dd);
            }
        }

    }

    public void initCurrentStockKbar(String stockCode,String stockName,String startDate,String endDate){
        List<ThsStockKbar> stockKbars = Lists.newArrayList();
        String thsStockCode = ThsStockUtils.commonStockCodeToThs(stockCode);
        String quote_str = JDIBridge.THS_HistoryQuotes(thsStockCode,"open,high,low,close,preClose,volume,amount,turnoverRatio,pe,adjustmentFactorBackward1,ths_trading_status_stock,totalCapital","",startDate,endDate);
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<String> tradeDates = tableJson.getJSONArray("time").toJavaList(String.class);
            if(tableInfo==null|| CollectionUtils.isEmpty(tradeDates)){
                return;
            }
            List<BigDecimal> opens = tableInfo.getJSONArray("open").toJavaList(BigDecimal.class);
            List<BigDecimal> highs = tableInfo.getJSONArray("high").toJavaList(BigDecimal.class);
            List<BigDecimal> lows = tableInfo.getJSONArray("low").toJavaList(BigDecimal.class);
            List<BigDecimal> closes = tableInfo.getJSONArray("close").toJavaList(BigDecimal.class);
            List<BigDecimal> preCloses = tableInfo.getJSONArray("preClose").toJavaList(BigDecimal.class);
            List<Long> quantitys = tableInfo.getJSONArray("volume").toJavaList(Long.class);
            List<BigDecimal> amounts = tableInfo.getJSONArray("amount").toJavaList(BigDecimal.class);
            List<BigDecimal> turnRatios = tableInfo.getJSONArray("turnoverRatio").toJavaList(BigDecimal.class);
            List<BigDecimal> pes = tableInfo.getJSONArray("pe").toJavaList(BigDecimal.class);
            List<BigDecimal> adjustmentFactorBackwards = tableInfo.getJSONArray("adjustmentFactorBackward1").toJavaList(BigDecimal.class);
            List<String> tradeTypeContents = tableInfo.getJSONArray("ths_trading_status_stock").toJavaList(String.class);
            List<BigDecimal> marketValues = tableInfo.getJSONArray("totalCapital").toJavaList(BigDecimal.class);
            int i = 0;
            for (String tradeDate:tradeDates){
                String dateyyyymmdd = DateUtil.dateStringFormat(tradeDate, DateUtil.yyyy_MM_dd, DateUtil.yyyyMMdd);
                ThsStockKbar stockKbar = new ThsStockKbar();
                stockKbar.setStockCode(stockCode);
                stockKbar.setStockName(stockName);
                stockKbar.setKbarDate(dateyyyymmdd);
                stockKbar.setUniqueKey(stockCode+"_"+dateyyyymmdd);
                stockKbar.setOpenPrice(opens.get(i));
                stockKbar.setClosePrice(closes.get(i));
                stockKbar.setHighPrice(highs.get(i));
                stockKbar.setLowPrice(lows.get(i));
                if(quantitys.get(i)!=null) {
                    stockKbar.setTradeQuantity(quantitys.get(i) / 100);
                }else {
                    stockKbar.setTradeQuantity(0L);
                }
                if(amounts.get(i)!=null) {
                    stockKbar.setTradeAmount(amounts.get(i).setScale(2, BigDecimal.ROUND_HALF_UP));
                }else{
                    stockKbar.setTradeAmount(BigDecimal.ZERO);
                }
                stockKbar.setZeroPrice(preCloses.get(i));
                stockKbar.setAdjFactor(adjustmentFactorBackwards.get(i));
                if(pes.get(i)!=null) {
                    stockKbar.setPe(pes.get(i).setScale(2, BigDecimal.ROUND_HALF_UP));
                }
                stockKbar.setTurnRatio(turnRatios.get(i));
                if(turnRatios.get(i)==null){
                    stockKbar.setTurnRatio(BigDecimal.ZERO);
                }
                stockKbar.setMarketValue(marketValues.get(i));
                stockKbar.setCreateTime(new Date());
                String tradeTypeContent = tradeTypeContents.get(i);
                if(tradeTypeContent.equals("交易")||tradeTypeContent.equals("新股上市")){
                    stockKbars.add(stockKbar);
                }else{
                    System.out.println(stockCode+"===="+stockName+"===="+tradeDate+"====="+tradeTypeContent);
                }
                i++;
            }

        }
        Map<String, ThsStockKbar> map = new HashMap<>();
        String quote_strFuquan = THS_HistoryQuotes(thsStockCode,"open,high,low,close","CPS:6",startDate,endDate);
        if(!StringUtils.isEmpty(quote_strFuquan)){
            JSONObject jsonObject = JSONObject.parseObject(quote_strFuquan);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<String> tradeDates = tableJson.getJSONArray("time").toJavaList(String.class);
            if(tableInfo==null){
                return;
            }
            List<BigDecimal> opens = tableInfo.getJSONArray("open").toJavaList(BigDecimal.class);
            List<BigDecimal> highs = tableInfo.getJSONArray("high").toJavaList(BigDecimal.class);
            List<BigDecimal> lows = tableInfo.getJSONArray("low").toJavaList(BigDecimal.class);
            List<BigDecimal> closes = tableInfo.getJSONArray("close").toJavaList(BigDecimal.class);
            int i = 0;
            for (String tradeDate:tradeDates) {
                String dateyyyymmdd = DateUtil.dateStringFormat(tradeDate, DateUtil.yyyy_MM_dd, DateUtil.yyyyMMdd);
                ThsStockKbar stockKbar = new ThsStockKbar();
                stockKbar.setAdjOpenPrice(opens.get(i));
                stockKbar.setAdjClosePrice(closes.get(i));
                stockKbar.setAdjHighPrice(highs.get(i));
                stockKbar.setAdjLowPrice(lows.get(i));
                map.put(dateyyyymmdd,stockKbar);
                i++;
            }
        }
        for (ThsStockKbar stockKbar:stockKbars){
            ThsStockKbar adjStockKbar = map.get(stockKbar.getKbarDate());
            stockKbar.setAdjClosePrice(adjStockKbar.getAdjClosePrice());
            stockKbar.setAdjOpenPrice(adjStockKbar.getAdjOpenPrice());
            stockKbar.setAdjHighPrice(adjStockKbar.getAdjHighPrice());
            stockKbar.setAdjLowPrice(adjStockKbar.getAdjLowPrice());
            ThsStockKbar byUniqueKey = thsStockKbarService.getByUniqueKey(stockKbar.getUniqueKey());
            if(byUniqueKey==null) {
                thsStockKbarService.save(stockKbar);
            }else{
                stockKbar.setId(byUniqueKey.getId());
                thsStockKbarService.updateById(stockKbar);
            }
        }
    }


}
