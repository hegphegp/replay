package com.bazinga.replay.component;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
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
    private IndexDetailService indexDetailService;
    @Autowired
    private StockCommonReplayService stockCommonReplayService;
    @Autowired
    private PlankExchangeDailyService plankExchangeDailyService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
    @Autowired
    private StockBollingService stockBollingService;
    @Autowired
    private StockAttributeReplayService stockAttributeReplayService;
    /*List<String> addresses = Lists.newArrayList("125.93.72.195:5368","125.93.72.195:5377","125.93.72.195:5379","125.93.72.195:5366","125.93.72.195:5367"
            ,"125.93.72.194:5288","125.93.72.194:5011","125.93.72.194:5277","125.93.72.197:5999");

    List<String> tigerAddress = Lists.newArrayList("117.186.20.117:6602");*/


    List<String> addresses = Lists.newArrayList();

    List<String> tigerAddress = Lists.newArrayList("117.186.20.117:6601");

    public void sendStockKbarReplay(String tradeDate){
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDate(tradeDate);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            System.out.println("stockKbar===="+i++);
            sendReplayStockKbarHttp(stockKbar);
        }
    }
    public void sendStockCommonReplay(String tradeDate){
        int i = 0;
        StockCommonReplayQuery query = new StockCommonReplayQuery();
        query.setKbarDate(tradeDate);
        List<StockCommonReplay> stockCommonReplays = stockCommonReplayService.listByCondition(query);
        for (StockCommonReplay stockCommonReplay:stockCommonReplays){
            System.out.println("stockCommonReplay==="+i++);
            sendStockCommonReplayHttp(stockCommonReplay);
        }
    }

    public void sendIndexDetail(String tradeDate){
        int i = 0;
        IndexDetailQuery query = new IndexDetailQuery();
        query.setKbarDate(tradeDate);
        List<IndexDetail> indexDetails = indexDetailService.listByCondition(query);
        for (IndexDetail indexDetail:indexDetails){
            System.out.println("indexDetail====="+i++);
            sendIndexDetailHttp(indexDetail);
        }
    }
    public void sendPlankExchangeDaily(String tradeDate){
        int i = 0;
        PlankExchangeDailyQuery query = new PlankExchangeDailyQuery();
        query.setTradeDate(tradeDate);
        List<PlankExchangeDaily> plankExchangeDailies = plankExchangeDailyService.listByCondition(query);
        for (PlankExchangeDaily plankExchangeDaily:plankExchangeDailies){
            System.out.println("plankExchangeDaily====="+i++);
            sendPlankExchangeDailyHttp(plankExchangeDaily);
        }
    }


    public void sendHistoryBlockStocks(String tradeDate){
        int i = 0;
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> stocksList = historyBlockStocksService.listByCondition(query);
        for (HistoryBlockStocks historyBlockStocks:stocksList){
            System.out.println("historyBlockStocks====="+i++);
            sendHistoryBLockStocksHttp(historyBlockStocks);
        }
    }

    public void sendStockBolling(String tradeDate){
        int i = 0;
        StockBollingQuery query = new StockBollingQuery();
        query.setKbarDate(tradeDate);
        List<StockBolling> stockBollings = stockBollingService.listByCondition(query);
        for (StockBolling stockBolling:stockBollings){
            System.out.println("stockBolling====="+i++);
            sendStockBollingHttp(stockBolling);
        }
    }

    public void sendStockAttributeReplay(String tradeDate){
        int i = 0;
        StockAttributeReplayQuery query = new StockAttributeReplayQuery();
        query.setKbarDate(tradeDate);
        List<StockAttributeReplay> stockAttributeReplays = stockAttributeReplayService.listByCondition(query);
        for (StockAttributeReplay stockAttributeReplay:stockAttributeReplays){
            System.out.println("stockAttributeReplay====="+i++);
            sendStockAttributeReplayHttp(stockAttributeReplay);
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
        for (String address:tigerAddress){
            String url = "http://"+address+"/tiger/basicInfo/saveStockKbar";
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



    public void sendIndexDetailHttp(IndexDetail indexDetail){
        if(indexDetail==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:addresses){
            String url = "http://"+address+"/dragon/basicInfo/saveIndexDetail";
            urls.add(url);
        }
        for (String address:tigerAddress){
            String url = "http://"+address+"/tiger/basicInfo/saveIndexDetail";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",indexDetail.getStockCode());
        map.put("stockName",indexDetail.getStockName());
        map.put("kbarDate",indexDetail.getKbarDate());
        map.put("indexCode",indexDetail.getIndexCode());
        map.put("blockName",indexDetail.getBlockName());
        String result = null;
        for(String url:urls) {
            try {
                result = HttpUtil.sendHttpGet(url, map);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }

    public void sendPlankExchangeDailyHttp(PlankExchangeDaily plankExchangeDaily){
        if(plankExchangeDaily==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:addresses){
            String url = "http://"+address+"/dragon/basicInfo/savePlankExchangeDaily";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",plankExchangeDaily.getStockCode());
        map.put("stockName",plankExchangeDaily.getStockName());
        map.put("tradeDate",plankExchangeDaily.getTradeDate());
        map.put("maxExchangeMoneyDate",plankExchangeDaily.getMaxExchangeMoneyDate());
        if(plankExchangeDaily.getPlankType()!=null) {
            map.put("plankType", plankExchangeDaily.getPlankType().toString());
        }
        if(plankExchangeDaily.getExchangeMoney()!=null) {
            map.put("exchangeMoney", plankExchangeDaily.getExchangeMoney().toString());
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

    public void sendHistoryBLockStocksHttp(HistoryBlockStocks historyBlockStocks){
        if(historyBlockStocks==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:tigerAddress){
            String url = "http://"+address+"/tiger/basicInfo/saveHistoryBlockStocks";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",historyBlockStocks.getBlockCode());
        map.put("stockName",historyBlockStocks.getBlockName());
        map.put("tradeDate",historyBlockStocks.getTradeDate());
        map.put("stocks",historyBlockStocks.getStocks());

        String result = null;
        for(String url:urls) {
            try {
                result = HttpUtil.sendHttpGet(url, map);
            } catch (Exception e) {
                log.info(e.getMessage(), e);
            }
        }
    }

    public void sendStockBollingHttp(StockBolling stockBolling){
        if(stockBolling==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:tigerAddress){
            String url = "http://"+address+"/tiger/basicInfo/saveStockBolling";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockBolling.getStockCode());
        map.put("stockName",stockBolling.getStockName());
        map.put("kbarDate",stockBolling.getKbarDate());
        if(stockBolling.getDayType()!=null) {
            map.put("dayType", stockBolling.getDayType().toString());
        }
        if(stockBolling.getUpPrice()!=null){
            map.put("upPrice",stockBolling.getUpPrice().toString());
        }
        if(stockBolling.getMiddlePrice()!=null){
            map.put("middlePrice",stockBolling.getMiddlePrice().toString());
        }
        if(stockBolling.getLowPrice()!=null){
            map.put("lowPrice",stockBolling.getLowPrice().toString());
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


    public void sendStockAttributeReplayHttp(StockAttributeReplay stockAttributeReplay){
        if(stockAttributeReplay==null){
            return;
        }
        List<String> urls = Lists.newArrayList();
        for (String address:tigerAddress){
            String url = "http://"+address+"/tiger/basicInfo/saveStockAttributeReplay";
            urls.add(url);
        }
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockAttributeReplay.getStockCode());
        map.put("stockName",stockAttributeReplay.getStockName());
        map.put("kbarDate",stockAttributeReplay.getKbarDate());
        if(stockAttributeReplay.getAvgRangeDay10()!=null) {
            map.put("avgRangeDay10", stockAttributeReplay.getAvgRangeDay10().toString());
        }
        if(stockAttributeReplay.getAvgAmountDay5()!=null){
            map.put("avgAmountDay5",stockAttributeReplay.getAvgAmountDay5().toString());
        }
        if(stockAttributeReplay.getRateDay5()!=null){
            map.put("rateDay5",stockAttributeReplay.getRateDay5().toString());
        }
        if(stockAttributeReplay.getRateDay3()!=null){
            map.put("rateDay3",stockAttributeReplay.getRateDay3().toString());
        }
        if(stockAttributeReplay.getMarketNew()!=null){
            map.put("marketNew",stockAttributeReplay.getMarketNew().toString());
        }
        if(stockAttributeReplay.getMarketValue()!=null){
            map.put("marketValue",stockAttributeReplay.getMarketValue().toString());
        }
        if(stockAttributeReplay.getPlanksDay10()!=null){
            map.put("planksDay10",stockAttributeReplay.getPlanksDay10().toString());
        }
        if(stockAttributeReplay.getClosePlanksDay10()!=null){
            map.put("closePlanksDay10",stockAttributeReplay.getClosePlanksDay10().toString());
        }
        if(stockAttributeReplay.getUpperShadowRate()!=null){
            map.put("upperShadowRate",stockAttributeReplay.getUpperShadowRate().toString());
        }
        if(stockAttributeReplay.getHighRate()!=null){
            map.put("highRate",stockAttributeReplay.getHighRate().toString());
        }
        if(stockAttributeReplay.getHighTime()!=null){
            map.put("highTime",stockAttributeReplay.getHighTime());
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
