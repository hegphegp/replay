package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.dto.BlockStockDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.HistoryBlockStocksService;
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

import static Ths.JDIBridge.THS_Snapshot;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsQuoteSaveComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void saveHS300FutureQuoteIndex(){
        int ret = thsLogin();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2021-09-10",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd)+ "===  开始了");
            quoteHuShen300QiHuo(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd));
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd)+ "=========  结束了");
        }
        thsLoginOut();

    }

    public void saveQuoteHuShen300QiHuo(){
        int ret = thsLogin();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("2023-02-15",DateUtil.yyyy_MM_dd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd)+ "===  开始了");
            quoteHuShen300QiHuo(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd));
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd)+ "=========  结束了");
        }
        thsLoginOut();

    }

    public void quoteHuShen300QiHuo(String tradeDate){
        String quote_str = THS_Snapshot("IFZL.CFE","preClose;latest;amt;latestVolume;ms;amount","",tradeDate+" 09:15:00",tradeDate+" 15:00:00");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONArray timeArray = tableJson.getJSONArray("time");
            if(timeArray==null||timeArray.size()==0){
                return;
            }
            List<String> times = timeArray.toJavaList(String.class);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            List<BigDecimal> amts = tableInfo.getJSONArray("amt").toJavaList(BigDecimal.class);
            List<BigDecimal> amounts = tableInfo.getJSONArray("amount").toJavaList(BigDecimal.class);
            List<BigDecimal> vols = tableInfo.getJSONArray("latestVolume").toJavaList(BigDecimal.class);
            List<BigDecimal> latests = tableInfo.getJSONArray("latest").toJavaList(BigDecimal.class);
            List<BigDecimal> preCloses = tableInfo.getJSONArray("preClose").toJavaList(BigDecimal.class);
            List<String> miss = tableInfo.getJSONArray("ms").toJavaList(String.class);
            int i = 0;
            for (String time:times){
                Date date = DateUtil.parseDate(time, DateUtil.DEFAULT_FORMAT);
                ThsQuoteInfo quote = new ThsQuoteInfo();
                quote.setStockCode("IFZLCFE");
                quote.setStockName("中证500股指期货");
                quote.setQuoteDate(DateUtil.format(date,DateUtil.yyyyMMdd));
                quote.setQuoteTime(DateUtil.format(date,DateUtil.HHmmss));
                quote.setCurrentPrice(latests.get(i));
                quote.setPreClosePrice(preCloses.get(i));
                Long timeStamp = formatTimeStamp(quote.getQuoteDate(), quote.getQuoteTime(), miss.get(i));
                quote.setTimeStamp(timeStamp);
                if(amts.get(i)==null) {
                    quote.setAmt(BigDecimal.ZERO);
                }else{
                    quote.setAmt(amts.get(i));
                }
                quote.setAmount(amounts.get(i));
                if(vols.get(i)==null) {
                    quote.setVol(0l);
                }else{
                    quote.setVol(vols.get(i).longValue());
                }
                thsQuoteInfoService.save(quote);
                i++;
            }
        }
    }

    public Long formatTimeStamp(String tradeDate,String tradeTime,String mill){
        if(StringUtils.isBlank(mill)){
            mill = "000";
        }
        if(mill.length()==2){
            mill = "0"+mill;
        }
        if(mill.length()==1){
            mill = "00"+mill;
        }
        String timeStr = tradeDate+tradeTime+mill;
        Date realTime = DateUtil.parseDate(timeStr, DateUtil.yyyyMMddHHmmssSSS);
        long timeStamp = realTime.getTime();
        return timeStamp;
    }

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
        //new ThsDataComponent().quoteInfo("600207","","");
        System.out.println(System.getProperty("java.library.path"));
        System.load("E://iFinDJava.dll");

        int ret = -1;
        if (args.length > 0) {
            System.out.println("login with cn account");
        }
        else {
            System.out.println("login with en account");
        }

        int a = 0;
        if (ret != 1) {
            while(true)
            {
               // System.out.print(++a);
                ret = JDIBridge.THS_iFinDLogin("ylz203", "182883");
                //System.out.println("THS_iFinDLogin ==> ");



                /*String strResultDataSerious = JDIBridge.THS_DateSerial("002233.SZ","ths_open_price_stock;ths_high_price_stock;ths_low_stock;ths_close_price_stock;ths_avg_price_stock;ths_vol_stock;ths_trans_num_stock;ths_amt_stock;ths_macd_stock;ths_kdj_stock;ths_vstd_stock;ths_boll_stock;ths_rsi_stock;ths_ma_stock;ths_sar_stock;ths_wr_stock;ths_cci_stock;ths_obv_stock;ths_vol_w_stock;ths_vol_m_stock","100;100;100;100;100;100;;;26,12,9,100,100,100;9,3,3,100,100,100;10,100;26,2,100,100,100;6,100,100;10,100,100;4,100,100;14,100,100;14,100,100;100,100,100;;","Days:Tradedays,Fill:Previous,Interval:D","2018-05-31","2018-06-15");
                System.out.println("THS_iFinDhis ==> " + strResultDataSerious );*/

               /* String change = JDIBridge.THS_HistoryQuotes("113528.SH", "change", "PriceType:1", "2021-10-29", "2021-11-09");
                System.out.println("quote ==>"+change);*/

                String s    = JDIBridge.THS_HistoryQuotes("000001.SZ","open,high,low,close","","2022-05-18","2022-05-19");
                System.out.println(s);

                JDIBridge.THS_iFinDLogout();
                System.out.println("THS_iFinDLogout ==> ");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        else {
            System.out.println("Login failed == > " + ret);
        }
    }

}
