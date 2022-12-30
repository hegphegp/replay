package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsLoginComponent {


    public int thsLogin(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("lsyjx002", "091303");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public int thsLoginOut(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("lsyjx002", "091303");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }


    public static void main(String[] args) {
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("lsyjx002", "334033");
        }catch (Exception e){
            log.error("同花顺登录失败",e);
        }
        String quote_str = JDIBridge.THS_RealtimeQuotes("000001.SH", "open;high;low;latest;tradeDate;tradeTime");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            if(tableInfo==null){
                return;
            }
            List<BigDecimal> macdValues = tableInfo.getJSONArray("ths_macd_index").toJavaList(BigDecimal.class);

        }
    }

}
