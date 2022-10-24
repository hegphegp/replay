package com.bazinga.replay.component;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class IndexKbarCurrentComponent {
    @Autowired
    private StockKbarComponent stockKbarComponent;

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

}
