package com.bazinga.replay.component;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import com.bazinga.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class IndexKbarCurrentComponent {
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private ThsStockIndexComponent thsStockIndexComponent;

    public void indexKbarCurrent(){
        BigDecimal openPriceSZ = stockKbarComponent.calCurrentIndexKbarOpenPrice("999999", "上证指数", 1);
        BigDecimal openPriceHS300 = stockKbarComponent.calCurrentIndexKbarOpenPrice("399300", "沪深300指数", 1);
        sendHttp("999999", "上证指数", openPriceSZ);
        sendHttp("399300", "沪深300指数", openPriceHS300);
    }

    public void sendHttp(String stockCode,String stockName,BigDecimal openPrice){
        String url = "http://120.26.85.183:8080/wave/replay/kbarOpenPrice";
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockCode);
        map.put("stockName",stockName);
        map.put("openPrice",openPrice.toString());
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, map);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        log.info("调用wave结果 stockCode:{} result：{}",stockCode,result);
    }


    public void indexStockKbarSend(){
        String yyyyMMdd = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
        StockKbar stockKbarSZ = stockKbarComponent.initSpecialStockKbarRecentTradeDate("999999", "上证指数", yyyyMMdd);
        StockKbar stockKbarHS = stockKbarComponent.initSpecialStockKbarRecentTradeDate("399300", "沪深300指数", yyyyMMdd);
        sendHttpStockKbar(stockKbarSZ);
        sendHttpStockKbar(stockKbarHS);
    }

    public void sendHttpStockKbar(StockKbar stockKbar){
        String url = "http://120.26.85.183:8080/wave/basicInfo/saveStockKbar";
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockKbar.getStockCode());
        map.put("stockName",stockKbar.getStockName());
        map.put("tradeDate",stockKbar.getKbarDate());
        map.put("openPrice",stockKbar.getOpenPrice().toString());
        map.put("closePrice",stockKbar.getClosePrice().toString());
        map.put("highPrice",stockKbar.getHighPrice().toString());
        map.put("lowPrice",stockKbar.getLowPrice().toString());
        map.put("tradeQuantity",stockKbar.getTradeQuantity().toString());
        map.put("tradeAmount",stockKbar.getTradeAmount().toString());
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, map);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        log.info("调用wave结果 stockCode:{} result：{}",stockKbar.getStockCode(),result);
    }

    public void stockIndexSend(){
        String dateyyyyMMhh = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
        //String dateyyyyMMhh = "20221209";
        StockIndex stockIndexHS = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh, "399300", "沪深300", ".SZ");
        StockIndex stockIndexSZ = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh, "000001", "上证指数", ".SH");
        sendHttpStockIndex( stockIndexHS);
        sendHttpStockIndex( stockIndexSZ);
    }

    public void sendHttpStockIndex(StockIndex stockIndex){
        String url = "http://120.26.85.183:8080/wave/basicInfo/saveStockIndex";
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockIndex.getStockCode());
        map.put("stockName",stockIndex.getStockName());
        map.put("tradeDate",stockIndex.getKbarDate());
        map.put("macd",stockIndex.getMacd().toString());
        map.put("diff",stockIndex.getDiff().toString());
        map.put("dea",stockIndex.getDea().toString());
        map.put("bias6",stockIndex.getBias6().toString());
        map.put("bias12",stockIndex.getBias12().toString());
        map.put("bias24",stockIndex.getBias24().toString());
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, map);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        log.info("调用wave结果 stockCode:{} result：{}",stockIndex.getStockCode(),result);
    }

}
