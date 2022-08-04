package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockBlockLevelComponent {
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private RedisMoniorService redisMoniorService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
    @Autowired
    private StockIndexService stockIndexService;

    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");
    List<String> NO_USE_BLOCK = Lists.newArrayList("沪股通","深股通","标普道琼斯","新股","次新"
            ,"创业板重组松绑","高送转","填权","共同富裕示范区","融资融券","MSCI","ST");


    public void stockFirstBlockInfo(){

        Map<String, HistoryBlockInfo> blockInfoMap = new HashMap<>();
        List<HistoryBlockInfo> historyBlockInfos = getHistoryBlockInfo();
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            blockInfoMap.put(historyBlockInfo.getBlockCode(),historyBlockInfo);
        }
        List<TradeDatePool> tradeDates = getTradeDates();
        Map<String, StockKbar> preStockKbarMap = new HashMap<>();
        for (TradeDatePool tradeDatePool:tradeDates){
            Map<String, String> levelMap = new HashMap<>();
            List<BlockEndRateDTO> blockRates = Lists.newArrayList();
            String dateyyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setKbarDate(dateyyyyMMdd);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            for (StockKbar stockKbar:stockKbars){
                boolean flag = noUseBlock(stockKbar.getStockCode(), blockInfoMap);
                if(!flag){
                    continue;
                }
                StockKbar preStockKbar = preStockKbarMap.get(stockKbar.getStockCode());
                if(preStockKbar!=null){
                    BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                    BlockEndRateDTO blockEndRateDTO = new BlockEndRateDTO();
                    blockEndRateDTO.setBlockCode(stockKbar.getStockCode());
                    blockEndRateDTO.setEndRate(endRate);
                    blockRates.add(blockEndRateDTO);
                }
            }
            preStockKbarMap.clear();
            for (StockKbar stockKbar:stockKbars){
                preStockKbarMap.put(stockKbar.getStockCode(),stockKbar);
            }
            if(blockRates.size()<=0){
                continue;
            }
            List<BlockEndRateDTO> blockRateSorts = BlockEndRateDTO.endRateSort(blockRates);
            int i = 0;
            for (BlockEndRateDTO blockEndRateDTO:blockRateSorts){
                i++;
                List<String> stocks = getBlockStocks(blockEndRateDTO.getBlockCode(), dateyyyyMMdd);
                if(CollectionUtils.isEmpty(stocks)){
                    continue;
                }
                for (String stockCode:stocks){
                    String level = levelMap.get(stockCode);
                    if(level==null){
                        levelMap.put(stockCode,i+"_"+i);
                    }else{
                        String value = levelMap.get(stockCode);
                        List<String> values = Lists.newArrayList(value.split("_"));
                        Integer little = Integer.valueOf(values.get(1));
                        if(i>little){
                            String valueStr = values.get(0)+"_"+i;
                            levelMap.put(stockCode,valueStr);
                        }
                    }
                }
            }
            for (String stockCode:levelMap.keySet()){
                RedisMonior redisMonior = new RedisMonior();
                redisMonior.setRedisKey(stockCode+"_"+dateyyyyMMdd);
                redisMonior.setRedisValue(levelMap.get(stockCode));
                redisMoniorService.save(redisMonior);
            }

        }

    }

    public boolean noUseBlock(String blockCode,Map<String,HistoryBlockInfo> blockInfoMap){
        HistoryBlockInfo blockInfo = blockInfoMap.get(blockCode);
        for (String blockName:NO_USE_BLOCK){
            if(blockInfo.getBlockName().contains(blockName)){
                return false;
            }
        }
        return true;
    }


    public List<HistoryBlockInfo> getHistoryBlockInfo(){
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        return historyBlockInfos;
    }



    public Map<String,CirculateInfo> getCirculateInfoMap(List<CirculateInfo> circulateInfos){
        Map<String, CirculateInfo> circulateInfoMap= new HashMap<>();
        for (CirculateInfo circulateInfo:circulateInfos){
            circulateInfoMap.put(circulateInfo.getStockCode(),circulateInfo);
        }
        return circulateInfoMap;
    }

    public List<TradeDatePool> getTradeDates(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate("20220701",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }





    public List<String> getBlockStocks(String blockCode,String tradeDate){
        if(tradeDate.equals(DateUtil.format(new Date(),DateUtil.yyyyMMdd))){
            Date preTradeDate = commonComponent.preTradeDate(new Date());
            tradeDate = DateUtil.format(preTradeDate,DateUtil.yyyyMMdd);
        }
        List<String> list = Lists.newArrayList();
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode(blockCode);
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocksList = historyBlockStocksService.listByCondition(query);
        if(CollectionUtils.isEmpty(historyBlockStocksList)){
            return null;
        }
        HistoryBlockStocks historyBlockStocks = historyBlockStocksList.get(0);
        String stocks = historyBlockStocks.getStocks();
        if(StringUtils.isBlank(stocks)){
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


    public List<StockKbar> limitQueueToList(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        return list;
    }


    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            /*query.setKbarDateFrom("");
            query.setKbarDateTo("");*/
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
           /*if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            stockKbars = stockKbars.subList(20, stockKbars.size());*/
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            //List<StockKbar> best = commonComponent.deleteNewStockTimes(stockKbars, 2000);
            return result;
        }catch (Exception e){
            return null;
        }
    }


    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,StockKbar kbar){
        BigDecimal reason = null;
        if(!(kbar.getClosePrice().equals(kbar.getAdjClosePrice()))&&!(kbar.getOpenPrice().equals(kbar.getAdjOpenPrice()))){
            reason = kbar.getAdjOpenPrice().divide(kbar.getOpenPrice(),4,BigDecimal.ROUND_HALF_UP);
        }
        if(reason==null){
            return avgPrice;
        }else{
            BigDecimal bigDecimal = avgPrice.multiply(reason).setScale(2, BigDecimal.ROUND_HALF_UP);
            return bigDecimal;
        }
    }



    public List<StockKbar> getStockKbarsByKBarDate(String kbarDate){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setKbarDate(kbarDate);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)){
                return null;
            }
            return stockKbars;
        }catch (Exception e){
            return null;
        }
    }



    public List<String> getHistoryBlockInfos(String blockCode,String tradeDateStr){
        List<String> list = Lists.newArrayList();
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode(blockCode);
        query.setTradeDate(tradeDateStr);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        if(CollectionUtils.isEmpty(historyBlockStocks)){
            return list;
        }
        HistoryBlockStocks blockStocks = historyBlockStocks.get(0);
        if(StringUtils.isBlank(blockStocks.getStocks())){
            return list;
        }
        String stocksStr = blockStocks.getStocks();
        List<String> stocks = Lists.newArrayList(stocksStr.split(","));
        return stocks;
    }


}
