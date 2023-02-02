package com.bazinga.replay.component;
import com.bazinga.replay.model.StockCommonReplay;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockCommonReplayQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.StockCommonReplayService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.util.DateUtil;
import com.bazinga.util.HttpUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class SendReplayInfoAutoComponent {
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private ThsStockIndexComponent thsStockIndexComponent;
    @Autowired
    private StockKbarService stockKbarService ;
    @Autowired
    private StockCommonReplayService stockCommonReplayService;
    List<String> addresses = Lists.newArrayList("125.93.72.195:5368","125.93.72.195:5379","125.93.72.195:5366","125.93.72.195:5367");

    public void sendStockKbarReplay(String tradeDate){
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDate(tradeDate);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            System.out.println(i++);
            sendReplayStockKbarHttp(stockKbar);
        }
    }
    public void sendStockCommonReplay(String tradeDate){
        int i = 0;
        StockCommonReplayQuery query = new StockCommonReplayQuery();
        query.setKbarDate(tradeDate);
        List<StockCommonReplay> stockCommonReplays = stockCommonReplayService.listByCondition(query);
        for (StockCommonReplay stockCommonReplay:stockCommonReplays){
            System.out.println(i++);
            sendStockCommonReplayHttp(stockCommonReplay);
        }
    }





    public void sendReplayStockKbarHttp(StockKbar stockKbar){
        if(stockKbar==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:addresses){
            String url = "http://"+address+"/dragon/basicInfo/saveStockKbar";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockKbar.getStockCode());
        map.put("stockName",stockKbar.getStockName());
        map.put("tradeDate",stockKbar.getKbarDate());
        map.put("openPrice",stockKbar.getOpenPrice().toString());
        map.put("closePrice",stockKbar.getClosePrice().toString());
        map.put("highPrice",stockKbar.getHighPrice().toString());
        map.put("lowPrice",stockKbar.getLowPrice().toString());
        if(stockKbar.getAdjOpenPrice()!=null) {
            map.put("adjOpenPrice", stockKbar.getAdjOpenPrice().toString());
        }
        if(stockKbar.getAdjClosePrice()!=null) {
            map.put("adjClosePrice", stockKbar.getAdjClosePrice().toString());
        }
        if(stockKbar.getAdjHighPrice()!=null) {
            map.put("adjHighPrice", stockKbar.getAdjHighPrice().toString());
        }
        if(stockKbar.getAdjLowPrice()!=null) {
            map.put("adjLowPrice", stockKbar.getAdjLowPrice().toString());
        }
        if(stockKbar.getAdjFactor()!=null) {
            map.put("stockFactor", stockKbar.getAdjFactor().toString());
        }
        if(stockKbar.getTradeQuantity()!=null) {
            map.put("tradeQuantity", stockKbar.getTradeQuantity().toString());
        }
        if(stockKbar.getTradeAmount()!=null) {
            map.put("tradeAmount", stockKbar.getTradeAmount().toString());
        }
        String result = null;
        for(String url:urls) {
            try {
                result = HttpUtil.sendHttpGet(url, map);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
            log.info("调用wave结果 stockCode:{} result：{}", stockKbar.getStockCode(), result);
        }
    }
    public void sendStockCommonReplayHttp(StockCommonReplay stockCommonReplay){
        if(stockCommonReplay==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:addresses){
            String url = "http://"+address+"/dragon/basicInfo/saveStockCommonReplay";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockCommonReplay.getStockCode());
        map.put("stockName",stockCommonReplay.getStockName());
        map.put("tradeDate",stockCommonReplay.getKbarDate());
        if(stockCommonReplay.getAvgPre1Price()!=null) {
            map.put("avgPre1Price", stockCommonReplay.getAvgPre1Price().toString());
        }
        if(stockCommonReplay.getAvgPre1Rate()!=null) {
            map.put("avgPre1Rate", stockCommonReplay.getAvgPre1Rate().toString());
        }
        if(stockCommonReplay.getEndRaiseRate55()!=null) {
            map.put("endRaiseRate55", stockCommonReplay.getEndRaiseRate55().toString());
        }
        if(stockCommonReplay.getPlankPriceThanLow10()!=null) {
            map.put("plankPriceThanLow10", stockCommonReplay.getPlankPriceThanLow10().toString());
        }
        if(stockCommonReplay.getAvgExchange10()!=null) {
            map.put("avgExchange10", stockCommonReplay.getAvgExchange10().toString());
        }
        if(stockCommonReplay.getRateDay5()!=null) {
            map.put("rateDay5", stockCommonReplay.getRateDay5().toString());
        }
        if(stockCommonReplay.getRateDay3()!=null) {
            map.put("rateDay3", stockCommonReplay.getRateDay3().toString());
        }
        if(stockCommonReplay.getGatherPriceThanLow10()!=null) {
            map.put("gatherPriceThanLow10", stockCommonReplay.getGatherPriceThanLow10().toString());
        }
        if(stockCommonReplay.getPlanksDay10()!=null) {
            map.put("planksDay10", stockCommonReplay.getPlanksDay10().toString());
        }
        String result = null;
        for(String url:urls) {
            try {
                result = HttpUtil.sendHttpGet(url, map);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }





}
