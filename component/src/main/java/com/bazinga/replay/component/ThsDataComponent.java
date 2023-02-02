package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.dto.BlockStockDTO;
import com.bazinga.replay.dto.OpenPictureDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.HistoryBlockInfoService;
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
import java.util.concurrent.TimeUnit;

import static Ths.JDIBridge.THS_Snapshot;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsDataComponent {
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void initHistoryBlockStocks(List<HistoryBlockInfo> historyBlockInfos){
        int ret = thsLogin();
        int i = 0;
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            i++;
            System.out.println(i + "===========kaishil");
            /*THREAD_POOL_QUOTE.execute(() ->{*/
                HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
                query.setBlockCode(historyBlockInfo.getBlockCode());
                query.setTradeDate(DateUtil.format(new Date(),DateUtil.yyyyMMdd));
                List<HistoryBlockStocks> blockStocks = historyBlockStocksService.listByCondition(query);
                String blockCode = historyBlockInfo.getBlockCode();
                String blockName = historyBlockInfo.getBlockName();
                if(CollectionUtils.isEmpty(blockStocks)) {
                    //long time0 = new Date().getTime();
                    List<BlockStockDTO> blockStockDTOS = getBlockStocks(blockCode, DateUtil.format(new Date(), DateUtil.yyyy_MM_dd));
                    //long time1 = new Date().getTime();
                   //System.out.println(time1 - time0);
                    List<HistoryBlockStocks> historys = converToHistoryBlockStocks(blockCode, blockName, blockStockDTOS);
                   // long time2 = new Date().getTime();
                   // System.out.println(time2 - time1);
                    saveBlockStocks(blockCode, historys);
                    System.out.println(blockCode + "===========结束了");
                }else{
                    System.out.println(blockCode + "===========yizhixin");
                }
            /*});*/
        }
        /*try {
            Thread.sleep(100000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
        thsLoginOut();
    }
    public void saveBlockStocks(String blockCode,List<HistoryBlockStocks> historyBlockStocks) {
        if(historyBlockStocks==null){
            return;
        }
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode(blockCode);
        List<HistoryBlockStocks> blockStocks = historyBlockStocksService.listByCondition(query);
        List<String> list = Lists.newArrayList();
        for (HistoryBlockStocks blockStock:blockStocks){
            list.add(blockStock.getTradeDate());
        }
        for (HistoryBlockStocks history:historyBlockStocks){
            if(list.contains(history.getTradeDate())){
                continue;
            }
            //System.out.println(history.getBlockCode()+"======"+history.getTradeDate());
            historyBlockStocksService.save(history);
        }
    }

    public List<HistoryBlockStocks> converToHistoryBlockStocks(String blockCode,String blockName,List<BlockStockDTO> list){
        List<HistoryBlockStocks> historys = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return historys;
        }
        Map<String,List<String>> inMap = new HashMap<>();
        Map<String,List<String>> outMap = new HashMap<>();
        for (BlockStockDTO blockStockDTO:list){
            if(blockStockDTO.getStatus()==0){
                List<String> stocks = inMap.get(blockStockDTO.getMarketDate());
                if(stocks==null){
                    stocks = new ArrayList<>();
                    inMap.put(blockStockDTO.getMarketDate(),stocks);
                }
                stocks.add(blockStockDTO.getStockCode());
            }
            if(blockStockDTO.getStatus()==1){
                List<String> stocks = outMap.get(blockStockDTO.getMarketDate());
                if(stocks==null){
                    stocks = new ArrayList<>();
                    outMap.put(blockStockDTO.getMarketDate(),stocks);
                }
                stocks.add(blockStockDTO.getStockCode());
            }
        }
        Date fisrtDate = DateUtil.parseDate("20171201", DateUtil.yyyyMMdd);
        Date endDate = DateTimeUtils.getDate000000(new Date());
        Date date = DateUtil.parseDate("19910101", DateUtil.yyyyMMdd);
        List<String> reals = Lists.newArrayList();
        for (int i=0; i<=20000;i++){
            date = DateUtil.addDays(date,1);
            String dateStr = DateUtil.format(date, DateUtil.yyyyMMdd);
            List<String> ins = inMap.get(dateStr);
            List<String> outs = outMap.get(dateStr);
            if(!CollectionUtils.isEmpty(ins)) {
                for (String inStock : ins) {
                    if (!reals.contains(inStock)) {
                        reals.add(inStock);
                    }
                }
            }
            if(!CollectionUtils.isEmpty(outs)) {
                for (String outStock : outs) {
                    if (reals.contains(outStock)) {
                        reals.remove(outStock);
                    }

                }
            }
            String str = null;
            if(reals.size()>0){
                for (String stockCode:reals){
                    if(str == null){
                        str = stockCode;
                    }else{
                        str = str+","+stockCode;
                    }
                }
            }
            if(str!=null){
                HistoryBlockStocks historyBlockStocks = new HistoryBlockStocks();
                historyBlockStocks.setBlockCode(blockCode);
                historyBlockStocks.setBlockName(blockName);
                historyBlockStocks.setCreateTime(new Date());
                historyBlockStocks.setStocks(str);
                historyBlockStocks.setTradeDate(dateStr);
                if(date.before(fisrtDate)||date.after(endDate)) {
                    continue;
                }else {
                    historys.add(historyBlockStocks);
                }
            }
        }
        return historys;
    }

    public List<BlockStockDTO> getBlockStocks(String blockCode,String tradeDateStr){
        ArrayList<BlockStockDTO> list = Lists.newArrayList();
        String quote_str = JDIBridge.THS_DataPool("indexConsRecord",blockCode+".TI;1990-02-01;"+tradeDateStr+";全部","date:Y,thscode:Y,securityName:Y,status:Y");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return list;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            JSONArray dateJson = tableInfo.getJSONArray("date");
            if(dateJson==null||dateJson.size()==0){
                return list;
            }
            List<String> dates = tableInfo.getJSONArray("date").toJavaList(String.class);
            List<String> stockCodes = tableInfo.getJSONArray("thscode").toJavaList(String.class);
            List<String> names = tableInfo.getJSONArray("securityName").toJavaList(String.class);
            List<String> types = tableInfo.getJSONArray("status").toJavaList(String.class);
            int i = 0;
            for (String time:dates){
                String stockCode = MarketUtil.thsToGeneralStock(stockCodes.get(i));
                BlockStockDTO blockStock = new BlockStockDTO();
                blockStock.setMarketDate(time);
                blockStock.setStockCode(stockCode);
                blockStock.setStockName(names.get(i));
                if(types.get(i).equals("剔除")) {
                    blockStock.setStatus(1);
                }else{
                    blockStock.setStatus(0);
                }
                list.add(blockStock);
                i++;
            }
        }
        return list;
    }


    public List<StockIndex>  initStockIndex(String thscode,String stockName,List<String> tradeDates){
        List<StockIndex> stockIndexList = Lists.newArrayList();
        int ret = thsLogin();
        for (String tradeDate:tradeDates) {
            System.out.println(tradeDate);
            BigDecimal diff = getMacdIndex(thscode, tradeDate, 100);
            BigDecimal dea = getMacdIndex(thscode, tradeDate, 101);
            BigDecimal macd = getMacdIndex(thscode, tradeDate, 102);
            String dateyyyyMMdd = DateUtil.format(DateUtil.parseDate(tradeDate, DateUtil.yyyy_MM_dd), DateUtil.yyyyMMdd);
            String stockCode = MarketUtil.thsToGeneralStock(thscode);
            StockIndex stockIndex = new StockIndex();
            stockIndex.setStockCode(stockCode);
            stockIndex.setStockName(stockName);
            stockIndex.setKbarDate(dateyyyyMMdd);
            stockIndex.setDea(dea);
            stockIndex.setDiff(diff);
            stockIndex.setMacd(macd);
            stockIndex.setUniqueKey(stockCode+"_"+dateyyyyMMdd);
            stockIndex.setCreateTime(new Date());
            stockIndexList.add(stockIndex);
        }
        thsLoginOut();
        return stockIndexList;

    }

    public List<BlockStockDTO> getBlockKbars(String blockCode,String blockName){
        ArrayList<BlockStockDTO> list = Lists.newArrayList();
        String quote_str = JDIBridge.THS_HistoryQuotes(blockCode+".TI","open,close,high,low,volume,amount","","2017-10-01","2022-05-19");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return list;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            JSONArray dateJson = tableInfo.getJSONArray("date");
            if(dateJson==null||dateJson.size()==0){
                return list;
            }
            List<BigDecimal> highs = tableInfo.getJSONArray("high").toJavaList(BigDecimal.class);
            List<BigDecimal> lows = tableInfo.getJSONArray("low").toJavaList(BigDecimal.class);
            List<BigDecimal> closes = tableInfo.getJSONArray("close").toJavaList(BigDecimal.class);
            List<BigDecimal> opens = tableInfo.getJSONArray("open").toJavaList(BigDecimal.class);
            List<BigDecimal> volumes = tableInfo.getJSONArray("volume").toJavaList(BigDecimal.class);
            List<BigDecimal> amounts = tableInfo.getJSONArray("amount").toJavaList(BigDecimal.class);
        }
        return list;
    }

    /**
     * DEA 101 DIFF 100  MACD 102
     * @param blockCode
     * @param tradeDate
     * @param index
     * @return
     */
    public BigDecimal getMacdIndex(String blockCode,String tradeDate,int index){
        String quote_str = JDIBridge.THS_BasicData(blockCode,"ths_macd_index",tradeDate+",26,12,9,"+index+",100");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return null;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            if(tableInfo==null){
                return null;
            }
            List<BigDecimal> macdValues = tableInfo.getJSONArray("ths_macd_index").toJavaList(BigDecimal.class);
            if(CollectionUtils.isEmpty(macdValues)){
                return null;
            }
            BigDecimal macdValue = macdValues.get(0).setScale(2,BigDecimal.ROUND_HALF_UP);
            return macdValue;

        }
        return null;
    }

    public int thsLogin(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("antt012", "445371");
            return ret;
        }catch (Exception e){
            log.error("同花顺登录失败",e);
            return -1;
        }
    }

    public int thsLoginOut(){
        try {
            System.load("E://iFinDJava.dll");
            int ret = JDIBridge.THS_iFinDLogin("antt012", "445371");
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
                ret = JDIBridge.THS_iFinDLogin("ylz200", "620865");
                //System.out.println("THS_iFinDLogin ==> ");



                /*String strResultDataSerious = JDIBridge.THS_DateSerial("002233.SZ","ths_open_price_stock;ths_high_price_stock;ths_low_stock;ths_close_price_stock;ths_avg_price_stock;ths_vol_stock;ths_trans_num_stock;ths_amt_stock;ths_macd_stock;ths_kdj_stock;ths_vstd_stock;ths_boll_stock;ths_rsi_stock;ths_ma_stock;ths_sar_stock;ths_wr_stock;ths_cci_stock;ths_obv_stock;ths_vol_w_stock;ths_vol_m_stock","100;100;100;100;100;100;;;26,12,9,100,100,100;9,3,3,100,100,100;10,100;26,2,100,100,100;6,100,100;10,100,100;4,100,100;14,100,100;14,100,100;100,100,100;;","Days:Tradedays,Fill:Previous,Interval:D","2018-05-31","2018-06-15");
                System.out.println("THS_iFinDhis ==> " + strResultDataSerious );*/

               /* String change = JDIBridge.THS_HistoryQuotes("113528.SH", "change", "PriceType:1", "2021-10-29", "2021-11-09");
                System.out.println("quote ==>"+change);*/

                String s    = THS_Snapshot("002420.SZ","tradeTime;latest;amt;vol;bid1;ask1","","2022-12-09 09:24:30","2022-12-09 09:30:04");

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


    public OpenPictureDTO getOpenPicture(String stockCode,String kbarDate){
        thsLogin();
        String result = THS_Snapshot(MarketUtil.generalToThsStock(stockCode), "tradeDate;tradeTime;askSize1;bidSize1", "",
                kbarDate+ " 09:25:00", kbarDate+" 09:25:03");
        JSONObject jsonObject = JSONObject.parseObject(result);
        JSONArray tables = jsonObject.getJSONArray("tables");
        if(tables==null||tables.size()==0){
            return null;
        }
        JSONObject tableJson = tables.getJSONObject(0);
        JSONObject tableInfo = tableJson.getJSONObject("table");
        JSONArray dateJson = tableInfo.getJSONArray("tradeDate");
        if(dateJson==null||dateJson.size()==0){
            return null;
        }
        List<String> tradeTimes = tableInfo.getJSONArray("tradeTime").toJavaList(String.class);
        List<String> bidSize1s = tableInfo.getJSONArray("bidSize1").toJavaList(String.class);
        List<String> tradeDates = tableInfo.getJSONArray("tradeDate").toJavaList(String.class);
        List<String> askSize1s = tableInfo.getJSONArray("askSize1").toJavaList(String.class);
        int index = tradeTimes.size()-1;
        if(tradeTimes.size()==2){
            if(tradeTimes.get(0).equals(tradeTimes.get(1))){
                long bidqytIndex0 = Double.valueOf(bidSize1s.get(0)).longValue();
                if(bidqytIndex0==0){
                    index=0;
                }
            }
        }
        OpenPictureDTO openPictureDTO = new OpenPictureDTO();
        openPictureDTO.setStockCode(stockCode);
        openPictureDTO.setBidQty(Double.valueOf(bidSize1s.get(index)).longValue());
        openPictureDTO.setAskQty(Double.valueOf(askSize1s.get(index)).longValue());
        openPictureDTO.setTradeTime(tradeTimes.get(index));
        openPictureDTO.setTradeDate(tradeDates.get(index));
        thsLoginOut();
        return openPictureDTO;
    }



}
