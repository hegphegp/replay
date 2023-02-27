package com.bazinga.replay.component;


import Ths.JDIBridge;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.query.ThsStockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsCirculateInfoService;
import com.bazinga.replay.service.ThsStockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.replay.util.ThsStockUtils;
import com.bazinga.util.CommonUtil;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsCirculateInitComponent {
    @Autowired
    private ThsLoginComponent thsLoginComponent;
    @Autowired
    private ThsCirculateInfoService thsCirculateInfoService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private ThsStockKbarService thsStockKbarService;
    @Autowired
    private ThsStockKbarInitComponent thsStockKbarInitComponent;
    public void initAMarketCirculateInfo(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20171201",DateUtil.yyyyMMdd));
        query.setTradeDateTo(new Date());
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        thsLoginComponent.thsLogin();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            System.out.println(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd));
            initAMarketCirculate(DateUtil.format(tradeDatePool.getTradeDate(),DateUtil.yyyy_MM_dd));
        }
        thsLoginComponent.thsLoginOut();
    }

    public void initAMarketCirculate(String tradeDate){
        List<ThsCirculateInfo> circulateInfos = Lists.newArrayList();
        String quote_str = JDIBridge.THS_DataPool("block",tradeDate+";001005010","thscode:Y,security_name:Y");
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
            List<String> thsStockCodes = tableInfo.getJSONArray("THSCODE").toJavaList(String.class);
            List<String> stockNames = tableInfo.getJSONArray("SECURITY_NAME").toJavaList(String.class);
            int i = 0;
            for (String thsStockCode:thsStockCodes){
                String dateyyyymmdd = DateUtil.dateStringFormat(tradeDate, DateUtil.yyyy_MM_dd, DateUtil.yyyyMMdd);
                String stockCode = ThsStockUtils.thsToCommonStockCode(thsStockCode);
                Integer marketCode = MarketUtil.getMarketCode(stockCode);
                boolean isAGeneralStock = MarketUtil.isAStocks(stockCode);
                if(isAGeneralStock) {
                    ThsCirculateInfo circulateInfo = new ThsCirculateInfo();
                    circulateInfo.setCirculate(0l);
                    circulateInfo.setCirculateZ(0l);
                    circulateInfo.setStockCode(stockCode);
                    circulateInfo.setStockName(stockNames.get(i));
                    circulateInfo.setTradeDate(dateyyyymmdd);
                    circulateInfo.setUniqueKey(stockCode + "_" + dateyyyymmdd);
                    circulateInfo.setStockType(CommonUtil.getStockType(circulateInfo.getCirculateZ().longValue()));
                    circulateInfo.setMarketType(marketCode);
                    circulateInfo.setCreateTime(new Date());
                    circulateInfos.add(circulateInfo);
                }
                i++;
            }
        }
        getCirculateZ(tradeDate,circulateInfos);
        for (ThsCirculateInfo circulateInfo:circulateInfos){
            circulateInfo.setStockType(CommonUtil.getStockType(circulateInfo.getCirculateZ().longValue()));
            thsCirculateInfoService.save(circulateInfo);
        }


    }

    public void getCirculateZ(String tradeDate,List<ThsCirculateInfo> circulateInfos){
        String codeContent = "";
        int i = 1;
        for (ThsCirculateInfo circulateInfo:circulateInfos){
            String thsStockCode = ThsStockUtils.commonStockCodeToThs(circulateInfo.getStockCode());
            if(i<circulateInfos.size()) {
                codeContent = codeContent + thsStockCode + ",";
            }else {
                codeContent = codeContent+thsStockCode;
            }
            i++;
        }
        Map<String, ThsCirculateInfo> map = new HashMap<>();
        String quote_str = JDIBridge.THS_BasicData(codeContent,"ths_free_float_shares_stock;ths_total_shares_stock",tradeDate+";"+tradeDate);
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return;
            }
            for (int index=0;index<tables.size();index++){
                JSONObject tableJson = tables.getJSONObject(index);
                String thsCode = tableJson.getString("thscode");
                JSONObject tableInfo = tableJson.getJSONObject("table");
                if(tableInfo==null){
                    return;
                }
                List<Long> circualteZ = tableInfo.getJSONArray("ths_free_float_shares_stock").toJavaList(Long.class);
                List<Long> circualteTotal = tableInfo.getJSONArray("ths_total_shares_stock").toJavaList(Long.class);
                ThsCirculateInfo circulateInfo = new ThsCirculateInfo();
                circulateInfo.setCirculate(circualteTotal.get(0));
                circulateInfo.setCirculateZ(circualteZ.get(0));
                String stockCode = ThsStockUtils.thsToCommonStockCode(thsCode);
                map.put(stockCode,circulateInfo);
            }
        }
        for (ThsCirculateInfo circulateInfo:circulateInfos){
            ThsCirculateInfo circulateValue = map.get(circulateInfo.getStockCode());
            circulateInfo.setCirculate(circulateValue.getCirculate());
            circulateInfo.setCirculateZ(circulateValue.getCirculateZ());
        }
    }
    public void initChangeIndexStockKbarinfo(){
        thsLoginComponent.thsLogin();
        Map<String, Map<String, List<ThsCirculateInfo>>> circulateChange =getCirculateChange();
        Map<String, List<ThsCirculateInfo>> inmap = circulateChange.get("inmap");
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            List<ThsCirculateInfo> changes = inmap.get(format);
            if(changes==null) {
                continue;
            }
            for (ThsCirculateInfo circulateInfo:changes) {
                System.out.println(format + "========" + circulateInfo.getStockCode());
                ThsStockKbarQuery stockKbarQuery = new ThsStockKbarQuery();
                stockKbarQuery.setStockCode(circulateInfo.getStockCode());
                List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(stockKbarQuery);
                if (!CollectionUtils.isEmpty(stockKbars)) {
                    continue;
                }
                thsStockKbarInitComponent.initCurrentStockKbar(circulateInfo.getStockCode(), circulateInfo.getStockName(), "2017-10-01", "2022-12-23");
            }

        }
        thsLoginComponent.thsLoginOut();
    }



    public void initAMarketChangeCirculateInfo(String startDate,String endDate){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate(startDate,DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateTimeUtils.getDate235959(DateUtil.parseDate(endDate,DateUtil.yyyyMMdd)));
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        thsLoginComponent.thsLogin();
        Map<String, Map<String, List<ThsCirculateInfo>>> circulateChange =getCirculateChange();
        Map<String, List<ThsCirculateInfo>> inmap = circulateChange.get("inmap");
        Map<String, List<ThsCirculateInfo>> outmap = circulateChange.get("outmap");
        thsLoginComponent.thsLoginOut();
        List<ThsCirculateInfo> preCirculateInfos = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyymmdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            System.out.println(yyyymmdd+"====="+preCirculateInfos.size());
            ThsCirculateInfoQuery circulateInfoQuery = new ThsCirculateInfoQuery();
            circulateInfoQuery.setTradeDate(yyyymmdd);
            List<ThsCirculateInfo> circulateInfos = thsCirculateInfoService.listByCondition(circulateInfoQuery);
            if(CollectionUtils.isEmpty(circulateInfos)){
                List<String> outList = Lists.newArrayList();
                List<ThsCirculateInfo> outStocks = outmap.get(yyyymmdd);
                if(outStocks!=null) {
                    for (ThsCirculateInfo circulateInfo : outStocks) {
                        outList.add(circulateInfo.getStockCode());
                    }
                }
                for(ThsCirculateInfo preCirculateInfo:preCirculateInfos){
                    if(outList.contains(preCirculateInfo.getStockCode())){
                        continue;
                    }
                    ThsCirculateInfo  circulateInfo= new ThsCirculateInfo();
                    circulateInfo.setCirculate(0l);
                    circulateInfo.setCirculateZ(0l);
                    circulateInfo.setTradeDate(yyyymmdd);
                    circulateInfo.setStockCode(preCirculateInfo.getStockCode());
                    circulateInfo.setStockName(preCirculateInfo.getStockName());
                    circulateInfo.setUniqueKey(preCirculateInfo.getStockCode() + "_" + yyyymmdd);
                    circulateInfo.setStockType(0);
                    Integer marketCode = MarketUtil.getMarketCode(preCirculateInfo.getStockCode());
                    circulateInfo.setCreateTime(new Date());
                    circulateInfo.setMarketType(marketCode);
                    thsCirculateInfoService.save(circulateInfo);
                }
                List<ThsCirculateInfo> inStocks = inmap.get(yyyymmdd);
                if(inStocks!=null) {
                    for (ThsCirculateInfo inStock : inStocks) {
                        ThsCirculateInfo circulateInfo = new ThsCirculateInfo();
                        circulateInfo.setCirculate(0l);
                        circulateInfo.setCirculateZ(0l);
                        circulateInfo.setTradeDate(yyyymmdd);
                        circulateInfo.setStockCode(inStock.getStockCode());
                        circulateInfo.setStockName(inStock.getStockName());
                        circulateInfo.setUniqueKey(circulateInfo.getStockCode() + "_" + yyyymmdd);
                        circulateInfo.setStockType(0);
                        Integer marketCode = MarketUtil.getMarketCode(circulateInfo.getStockCode());
                        circulateInfo.setCreateTime(new Date());
                        circulateInfo.setMarketType(marketCode);
                        thsCirculateInfoService.save(circulateInfo);
                    }
                }
            }
            ThsCirculateInfoQuery circulateInfoQuery1 = new ThsCirculateInfoQuery();
            circulateInfoQuery1.setTradeDate(yyyymmdd);
            List<ThsCirculateInfo> circulateInfoTodays = thsCirculateInfoService.listByCondition(circulateInfoQuery1);
            preCirculateInfos.clear();
            preCirculateInfos.addAll(circulateInfoTodays);
        }
    }


    public Map<String,Map<String,List<ThsCirculateInfo>>> getCirculateChange(){
        String endDate = DateUtil.format(new Date(), DateUtil.yyyy_MM_dd);
        Map<String, Map<String,List<ThsCirculateInfo>>> map = new HashMap<>();
        Map<String, List<ThsCirculateInfo>> inmap = new HashMap<>();
        Map<String, List<ThsCirculateInfo>> outmap = new HashMap<>();
        String quote_str = JDIBridge.THS_DataPool("sectorConsRecord","001005010;2017-12-01;"+endDate+";ALL","date:Y,thscode:Y,securityName:Y,status:Y");
        if(!StringUtils.isEmpty(quote_str)){
            JSONObject jsonObject = JSONObject.parseObject(quote_str);
            JSONArray tables = jsonObject.getJSONArray("tables");
            if(tables==null||tables.size()==0){
                return map;
            }
            JSONObject tableJson = tables.getJSONObject(0);
            JSONObject tableInfo = tableJson.getJSONObject("table");
            if(tableInfo==null){
                return map;
            }
            List<String> tradeDates = tableInfo.getJSONArray("date").toJavaList(String.class);
            List<String> thscodes = tableInfo.getJSONArray("thscode").toJavaList(String.class);
            List<String> statues = tableInfo.getJSONArray("status").toJavaList(String.class);
            List<String> stockNames = tableInfo.getJSONArray("securityName").toJavaList(String.class);
            int i = 0;
            for (String tradeDate:tradeDates){
                String stockCode = ThsStockUtils.thsToCommonStockCode(thscodes.get(i));
                boolean isA = MarketUtil.isAStocks(stockCode);
                String dateStr = DateUtil.dateStringFormat(tradeDate, DateUtil.yyyy_MM_dd, DateUtil.yyyyMMdd);
                if(isA) {
                    ThsCirculateInfo circulateInfo = new ThsCirculateInfo();
                    circulateInfo.setStockName(stockNames.get(i));
                    circulateInfo.setTradeDate(dateStr);
                    circulateInfo.setStockCode(stockCode);
                    String status = statues.get(i);
                    if (status.equals("纳入")) {
                        List<ThsCirculateInfo> circulateInfos = inmap.get(dateStr);
                        if(circulateInfos==null){
                            circulateInfos = Lists.newArrayList();
                            inmap.put(dateStr,circulateInfos);
                        }
                        circulateInfos.add(circulateInfo);
                    } else {
                        List<ThsCirculateInfo> circulateInfos = outmap.get(dateStr);
                        if(circulateInfos==null){
                            circulateInfos = Lists.newArrayList();
                            outmap.put(dateStr,circulateInfos);
                        }
                        circulateInfos.add(circulateInfo);
                    }
                }
                i++;
            }

        }

        map.put("inmap",inmap);
        map.put("outmap",outmap);
        return map;
    }

}
