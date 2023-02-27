package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.replay.dto.CirculateDetailDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.CommonUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class HistoryBlockInfoComponent {

    private static final int  LOOP_TIMES =3;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockAverageLineService stockAverageLineService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
    @Autowired ThsDataComponent thsDataComponent;

    public void initHistoryBlockInfo(){
        List<HistoryBlockInfo> blocks = getBlocks();
        saveHistoryBlockInfo(blocks);
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        thsDataComponent.initHistoryBlockStocks(historyBlockInfos);
    }

    public void saveBlockStocks(List<HistoryBlockStocks> historyBlockStocks) {
        if(historyBlockStocks==null){
            return;
        }
        for (HistoryBlockStocks history:historyBlockStocks){
            historyBlockStocksService.save(history);
        }
    }

    public void getPreBlockStocks(String preTradeDate,String tradeDate) {
        HistoryBlockStocksQuery historyBlockStocksQuery = new HistoryBlockStocksQuery();
        historyBlockStocksQuery.setTradeDate(preTradeDate);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(historyBlockStocksQuery);
        if(historyBlockStocks==null){
            return;
        }
        for (HistoryBlockStocks history:historyBlockStocks){
            HistoryBlockStocks stocks = new HistoryBlockStocks();
            stocks.setBlockCode(history.getBlockCode());
            stocks.setBlockName(history.getBlockName());
            stocks.setStocks(history.getStocks());
            stocks.setTradeDate(tradeDate);
            stocks.setCreateTime(new Date());
            historyBlockStocksService.save(stocks);
        }
    }

    public  List<HistoryBlockInfo> getBlocks() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "ths_index");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        paramsdate.put("exchange", "A");
        paramMap.put("params", paramsdate);
        int times = 1;
        while (times <= LOOP_TIMES){
            try {
                String body = Jsoup.connect("http://api.tushare.pro").ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(JSONObject.toJSONString(paramMap)).post().text();
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray fields = data.getJSONArray("items");
                if (CollectionUtils.isEmpty(fields)) {
                    return null;
                }
                List<HistoryBlockInfo> blockInfos = convertHistoryBlockCirculate(fields);
                return blockInfos;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}",  e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public void saveHistoryBlockInfo(List<HistoryBlockInfo> blocks){
        if(CollectionUtils.isEmpty(blocks)){
            return;
        }
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(new HistoryBlockInfoQuery());
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            historyBlockInfoService.deleteById(blockInfo.getId());
        }
        List<String> list = Lists.newArrayList("同花顺漂亮100");
        for (HistoryBlockInfo block:blocks){
            boolean flag = false;
            for (String badBlockName:list){
                if(block.getBlockName().contains(badBlockName)){
                    flag = true;
                }
            }
            if(flag){
                continue;
            }
            historyBlockInfoService.save(block);
        }
    }


    public List<HistoryBlockInfo> convertHistoryBlockCirculate(JSONArray jsonArray){
        List<HistoryBlockInfo> blocks = Lists.newArrayList();
        for(int i = 0;i<jsonArray.size();i++){
            JSONArray blcoksArray = jsonArray.getJSONArray(i);
            String blockCode = blcoksArray.get(0).toString().replace(".TI","");
            String blockName = blcoksArray.get(1).toString();
            String marketDate = null;
            if(blcoksArray.get(4)==null){
                System.out.println(JSONObject.toJSONString(blcoksArray));
            }else {
                marketDate = blcoksArray.get(4).toString();
            }
            String blockType = blcoksArray.get(5).toString();
            HistoryBlockInfo historyBlockInfo = new HistoryBlockInfo();
            historyBlockInfo.setBlockCode(blockCode);
            historyBlockInfo.setBlockName(blockName);
            historyBlockInfo.setMarketDate(marketDate);
            if(StringUtils.isBlank(blockType)){
                continue;
            }
            if(historyBlockInfo.getBlockCode().startsWith("864")||historyBlockInfo.getBlockCode().startsWith("883")){
                continue;
            }
            if(blockType.equals("N")) {
                historyBlockInfo.setBlockType(1);
            }else if(blockType.equals("I")){
                historyBlockInfo.setBlockType(0);
            }else{
                continue;
            }
            blocks.add(historyBlockInfo);
        }
        return blocks;
    }

    public void compareHistoryBlockStocks(){
        List<String> blockStocks = getBlockStocks("886001", "20230215");
        List<String> hisBlockStocks = getBlockStocks("886001", "20230214");
        for (String stockCode:blockStocks){
            if(!hisBlockStocks.contains(stockCode)){
                System.out.println(stockCode);
            }
        }
        if (blockStocks.contains("002276")){
            System.out.println("====="+1111);
        }

        if (hisBlockStocks.contains("002276")){
            System.out.println("====="+2222);
        }

    }


    public List<String> getBlockStocks(String blockCode,String tradeDate){
        List<String> list = Lists.newArrayList();
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode(blockCode);
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocksList = historyBlockStocksService.listByCondition(query);
        if(org.springframework.util.CollectionUtils.isEmpty(historyBlockStocksList)){
            return null;
        }
        HistoryBlockStocks historyBlockStocks = historyBlockStocksList.get(0);
        String stocks = historyBlockStocks.getStocks();
        if(org.apache.commons.lang3.StringUtils.isBlank(stocks)){
            return null;
        }
        String[] split = stocks.split(",");
        List<String> stockList = Lists.newArrayList(split);
        for (String stockCode:stockList){
            boolean shMain = MarketUtil.isShMain(stockCode);
            boolean chuangYe = MarketUtil.isChuangYe(stockCode);
            boolean szMain = MarketUtil.isSzMain(stockCode);
            if(shMain||chuangYe||szMain){
                list.add(stockCode);
            }
        }
        return list;
    }

}
