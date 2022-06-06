package com.bazinga.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.BlocKFollowBuyDTO;
import com.bazinga.dto.MarketMoneyDTO;
import com.bazinga.dto.PlankTimePairDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.sun.deploy.net.proxy.WFirefoxProxyConfig;
import jnr.ffi.annotations.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.sql.rowset.FilteredRowSet;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BlockFollowComponent {
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
    public void relativeWithSZInfo(){
        List<TradeDatePool> tradeDates = getTradeDates();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, CirculateInfo> circulateInfoMap = getCirculateInfoMap(circulateInfos);
        List<HistoryBlockInfo> blockInfos = getHistoryBlockInfo();
        Map<String, List<PlankTimePairDTO>> pairs = getPlankTimePairs(circulateInfos);


       /* Map<String, List<RelativeWithSzBuyDTO>> map = raiseInfo();
        List<Object[]> datas = Lists.newArrayList();

        for (String tradeDate : map.keySet()) {
            List<RelativeWithSzBuyDTO> relativeWithSzBuyDTOS = map.get(tradeDate);
            for (RelativeWithSzBuyDTO dto:relativeWithSzBuyDTOS) {
                List<Object> list = new ArrayList<>();
                list.add(dto.getStockCode());
                list.add(dto.getStockCode());
                list.add(dto.getStockName());
                list.add(dto.getTradeDate());
                list.add(dto.getCirculate());
                list.add(dto.getCirculateZ());
                list.add(dto.getBuyPrice());
                list.add(dto.getRateDay1());
                list.add(dto.getRateDay2());
                list.add(dto.getRateDay3());
                list.add(dto.getRateDay3Total());
                list.add(dto.getLevel());
                list.add(dto.getLowAddHigh());
                list.add(dto.getBeforeRateDay3());
                list.add(dto.getBeforeRateDay5());
                list.add(dto.getBeforeRateDay10());
                list.add(dto.getBeforeRateDay60());
                list.add(dto.getProfit());
                list.add(dto.getProfitEnd());
                Object[] objects = list.toArray();
                datas.add(objects);
            }
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","总股本","流通z","买入价格","第一天","第二天","第三天","3天总背离值","排名","最高加最低","3日涨幅","5日涨幅","10日涨幅","60日涨幅","板高","收盘盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("背离买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("背离买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }*/
    }


    public Map<String, List<PlankTimePairDTO>> getPlankTimePairs(List<CirculateInfo> circulateInfos){
        Map<String, List<PlankTimePairDTO>> map = new HashMap<>();
        int m = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            m++;
            System.out.println(circulateInfo.getStockCode());
            /*if(!circulateInfo.getStockCode().equals("605319")){
                continue;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))){
                    continue;
                }
                /*if(date.after(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))){
                    continue;
                }*/
                List<String> olds = Lists.newArrayList();
                RedisMonior redisMonior = redisMoniorService.getByRedisKey(circulateInfo.getStockCode());
                if(redisMonior!=null&&!redisMonior.getRedisValue().equals("test")){
                    String[] split = redisMonior.getRedisValue().split(",");
                    List<String> strings = Arrays.asList(split);
                    olds.addAll(strings);
                }

                if(preKbar!=null) {
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(highUpper){
                        int planks = calPlanks(limitQueue);
                        List<PlankTimePairDTO> plankPairs = getPlankPairs(stockKbar, preKbar.getClosePrice(),planks);
                        if(!CollectionUtils.isEmpty(plankPairs)){
                            List<PlankTimePairDTO> pairs = map.get(stockKbar.getKbarDate());
                            if(pairs==null){
                                pairs = Lists.newArrayList();
                                map.put(stockKbar.getKbarDate(),pairs);
                            }
                            pairs.addAll(plankPairs);
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return map;
    }

    public List<HistoryBlockInfo> getHistoryBlockInfo(){
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        return historyBlockInfos;
    }

    public void blockBuys(List<HistoryBlockInfo> blockInfos,List<PlankTimePairDTO> pairs,Map<String, CirculateInfo> circulateInfoMap,String tradeDate){
        Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        Date nextTradeDate = commonComponent.afterTradeDate(DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        List<StockKbar> stockKbars = getStockKbarsByKBarDate(tradeDate);
        List<StockKbar> nextStockKbars = getStockKbarsByKBarDate(DateUtil.format(nextTradeDate,DateUtil.yyyyMMdd));
        List<StockKbar> preStockKbars = getStockKbarsByKBarDate(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd));

        Map<String, StockKbar> stockMap = new HashMap<>();
        Map<String, StockKbar> nestStockMap = new HashMap<>();
        Map<String, StockKbar> preStockMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            stockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:nextStockKbars){
            nestStockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:preStockKbars){
            preStockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (HistoryBlockInfo blockInfo:blockInfos){
            List<PlankTimePairDTO> blockPairs = Lists.newArrayList();
            List<String> stocks = getBlockStocks(blockInfo.getBlockCode(), tradeDate);
            if(CollectionUtils.isEmpty(stocks)){
                continue;
            }
            for (PlankTimePairDTO pair:pairs){
                if(stocks.contains(pair.getStockCode())){
                    blockPairs.add(pair);
                }
            }
            if(blockPairs.size()<3){
                continue;
            }
            Map<String, List<MarketMoneyDTO>> threeBuyLevelStocks = getThreeBuyLevelStocks(stocks, circulateInfoMap, stockMap);
            List<PlankTimePairDTO> plankTens = judgePlanks100000(blockPairs);
            if(plankTens.size()<3){
                continue;
            }
            BlocKFollowBuyDTO blocKFollowBuyDTO = new BlocKFollowBuyDTO();
            BigDecimal firstProfit = getBuysProfit(threeBuyLevelStocks.get("first"), "100000", stockMap, nestStockMap, preStockMap);
            BigDecimal twoProfit = getBuysProfit(threeBuyLevelStocks.get("two"), "100000", stockMap, nestStockMap, preStockMap);
            BigDecimal threeProfit = getBuysProfit(threeBuyLevelStocks.get("three"), "100000", stockMap, nestStockMap, preStockMap);




        }
    }

    public List<PlankTimePairDTO> judgePlanks100000(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        for (PlankTimePairDTO pair:pairs){
            if(pair.getStart()<=100000&&(pair.getEnd()==null||pair.getEnd()>100000)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }

    public Map<String, List<MarketMoneyDTO>> getThreeBuyLevelStocks(List<String> stocks,Map<String,CirculateInfo> circulateInfoMap,Map<String, StockKbar> stockKbarMap){
        Map<String, List<MarketMoneyDTO>> map = new HashMap<>();
        List<MarketMoneyDTO> list = Lists.newArrayList();
        for (String stock:stocks){
            CirculateInfo circulateInfo = circulateInfoMap.get(stock);
            StockKbar stockKbar = stockKbarMap.get(stock);
            if(stockKbar!=null&&circulateInfo!=null) {
                BigDecimal marketMoney = new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbar.getOpenPrice()).setScale(2,BigDecimal.ROUND_HALF_UP);
                MarketMoneyDTO marketMoneyDTO = new MarketMoneyDTO();
                marketMoneyDTO.setStockCode(stock);
                marketMoneyDTO.setStockName(circulateInfo.getStockName());
                marketMoneyDTO.setCirculate(circulateInfo.getCirculateZ());
                marketMoneyDTO.setMarketMoney(marketMoney);
                list.add(marketMoneyDTO);
            }
        }
        if(CollectionUtils.isEmpty(list)||list.size()<=3){
            return map;
        }
        List<MarketMoneyDTO> marketMoneyDTOS = MarketMoneyDTO.marketLevelSort(list);
        Map<String, List<Integer>> levelIndex = getLevelIndex(marketMoneyDTOS.size());
        List<Integer> firsts = levelIndex.get("first");
        List<Integer> twos = levelIndex.get("two");
        List<Integer> threes = levelIndex.get("three");
        List<MarketMoneyDTO> firstStocks = marketMoneyDTOS.subList(0, firsts.get(1));
        List<MarketMoneyDTO> twoStocks = marketMoneyDTOS.subList(twos.get(0)-1, twos.get(1));
        List<MarketMoneyDTO> threeStocks = marketMoneyDTOS.subList(threes.get(0)-1, firsts.get(1));
        map.put("first",firstStocks);
        map.put("two",twoStocks);
        map.put("three",threeStocks);
        return map;
    }
    public BigDecimal getBuysProfit(List<MarketMoneyDTO> marketDtos,String tradeTime,Map<String, StockKbar> stockKbarMap,Map<String, StockKbar> nextStockKbarMap,Map<String, StockKbar> preStockKbarMap){
        BigDecimal totalProfit = BigDecimal.ZERO;
        int count = 0;
        for (MarketMoneyDTO dto:marketDtos){
            StockKbar stockKbar = stockKbarMap.get(dto.getStockCode());
            StockKbar nextStockKbar = nextStockKbarMap.get(dto.getStockCode());
            StockKbar preStockKbar = preStockKbarMap.get(dto.getStockCode());
            if(stockKbar==null||preStockKbar==null||nextStockKbar==null){
                continue;
            }
            BigDecimal stockBuyPrice = getStockBuyPrice(dto.getStockCode(), stockKbar.getKbarDate(), tradeTime, preStockKbar);
            BigDecimal sellPrice = nextStockKbar.getTradeAmount().divide(new BigDecimal(nextStockKbar.getTradeQuantity() * 100));
            BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(stockBuyPrice, stockKbar);
            BigDecimal chuQuanSellPrice = chuQuanAvgPrice(sellPrice, stockKbar);
            BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanSellPrice.subtract(chuQuanBuyPrice), chuQuanBuyPrice);
            count++;
            totalProfit = totalProfit.add(profit);
        }
        if(count>0){
            return null;
        }
        return null;
    }

    public BigDecimal getStockBuyPrice(String stockCode,String tradeDate,String buyTime,StockKbar preStockKbar){
        long buyTimeInt = timeToLong(buyTime);
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        String preMin = "09:25";
        Integer index = -1;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            Integer tradeType = data.getTradeType();
            String tradeTime = data.getTradeTime();
            boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preStockKbar.getClosePrice(), tradeDate);
            if(tradeTime.equals(preMin)){
                index++;
            }else{
                preMin = tradeTime;
                index = 0;
            }
            long timeLong = timeToLong(tradeTime, index);
            if(timeLong>=buyTimeInt){
                if(historyUpperPrice&&tradeType==1){
                    return null;
                }
                return tradePrice;
            }
        }
        return null;
    }


    public static Map<String,List<Integer>> getLevelIndex(int size){
        Map<String, List<Integer>> map = new HashMap<>();
        if(size<=4){
            map.put("first",Lists.newArrayList(1,1));
            map.put("two",Lists.newArrayList(2,2));
            map.put("three",Lists.newArrayList(size,size));
            return map;
        }
        Integer levelSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.3")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        Integer halfSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        if(size%2==1) {
            halfSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_UP).toString());
        }
        Integer handleSize = Integer.valueOf(new BigDecimal(levelSize).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        if(size>=60){
            levelSize = 20;
            handleSize = 10;
        }
        map.put("first",Lists.newArrayList(1,levelSize));
        if(handleSize>0) {
            if(levelSize%2==0) {
                map.put("two", Lists.newArrayList(halfSize - handleSize + 1, halfSize + handleSize));
            }else{
                map.put("two", Lists.newArrayList(halfSize - handleSize, halfSize + handleSize));
            }
        }else{
            map.put("two", Lists.newArrayList(halfSize, halfSize));
        }
        map.put("three",Lists.newArrayList(size-levelSize+1,size));
        return map;
    }

    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList();
        for (int i=0 ; i<=99 ;i++) {
            list.add(i);
        }
        for (int i=3 ; i<=100 ;i++) {
            Map<String, List<Integer>> levelIndex = getLevelIndex(i);
            System.out.println(levelIndex);
            List<Integer> firsts = list.subList(0, levelIndex.get("first").get(1));
            List<Integer> twos = list.subList(levelIndex.get("two").get(0)-1, levelIndex.get("two").get(1));
            List<Integer> threes = list.subList(levelIndex.get("three").get(0)-1, levelIndex.get("three").get(1));
            System.out.println(firsts);
            System.out.println(twos);
            System.out.println(threes);
            System.out.println(i+"======================");

        }

        List<Integer> subs = list.subList(2, 3);
        System.out.println(subs);
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
        query.setTradeDateFrom(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate("20220501",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }





    public List<String> getBlockStocks(String blockCode,String tradeDate){
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


    public List<PlankTimePairDTO> getPlankPairs(StockKbar stockKbar,BigDecimal preEndPrice,int planks){
        List<PlankTimePairDTO> pairs = Lists.newArrayList();
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return pairs;
        }
        int i = 0;
        int index = 0;
        String preMin = "09:25";
        boolean gatherUpper = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            if(data.getTradeTime().equals(preMin)){
                index++;
            }else{
                preMin = data.getTradeTime();
                index = 0;
            }
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice,stockKbar.getKbarDate());
            if(data.getTradeTime().equals("09:25")&&upperPrice){
                gatherUpper = true;
            }
            if(data.getTradeType()!=0&&data.getTradeType()!=1){
                continue;
            }
            Integer tradeType = data.getTradeType();
            i++;
            if(i==1&&gatherUpper&&upperPrice&&tradeType==1){
                PlankTimePairDTO pairDTO = new PlankTimePairDTO();
                pairDTO.setStockCode(stockKbar.getStockCode());
                pairDTO.setPlanks(planks);
                pairDTO.setStart(92500l);
                pairs.add(pairDTO);
                continue;
            }
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            PlankTimePairDTO pair = null;
            if(pairs.size()>0){
                pair = pairs.get(pairs.size()-1);
            }
            if(i>=1){
                if(pair==null||pair.getEnd()!=null){
                    if(upperPrice&&tradeType==1) {
                        PlankTimePairDTO pairDTO = new PlankTimePairDTO();
                        pairDTO.setPlanks(planks);
                        pairDTO.setStockCode(stockKbar.getStockCode());
                        long start = timeToLong(data.getTradeTime(), index);
                        pairDTO.setStart(start);
                        pairs.add(pairDTO);
                    }
                }
                if (pair!=null&&pair.getEnd()==null){
                    if(!upperPrice||tradeType!=1){
                        long end = timeToLong(data.getTradeTime(), index);
                        pair.setEnd(end);
                    }
                }
            }
        }
        return pairs;
    }

    public static long timeToLong(String time,int index){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        int second = index * 3;
        if(second<10) {
            timeStr = timeStr + "0" +index;
        }else{
            timeStr = time+second;
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }

    public static long timeToLong(String time){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }


    public void calProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                /*if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }*/
                i++;
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjClosePrice()), buyStockKbar.getAdjClosePrice());
                return;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }




    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            /*query.setKbarDateFrom("");
            query.setKbarDateTo("");*/
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
           if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
           // stockKbars = stockKbars.subList(20, stockKbars.size());
            List<StockKbar> result = Lists.newArrayList();
            for (StockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<StockKbar> best = commonComponent.deleteNewStockTimes(stockKbars, 2000);
            return best;
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

    public int calPlanks(LimitQueue<StockKbar> limitQueue){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)){
            return 1;
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        int planks = 1;
        int i=0;
        StockKbar nextStockKbar = null;
        for (StockKbar stockKbar:reverse){
            i++;
            if(i>=3){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), nextStockKbar.getClosePrice(), stockKbar.getClosePrice(), nextStockKbar.getKbarDate());
                if(endUpper){
                    planks++;
                }else {
                    return planks;
                }
            }
            nextStockKbar = stockKbar;
        }
        return planks;
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

}
