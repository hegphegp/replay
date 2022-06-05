package com.bazinga.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
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

    public void blockBuys(List<HistoryBlockInfo> blockInfos,List<PlankTimePairDTO> pairs,String tradeDate){
        List<StockKbar> stockKbars = getStockKbarsByKBarDate(tradeDate);
        List<StockKbar> nextStockKbars = getStockKbarsByKBarDate(tradeDate);
        Map<String, StockKbar> stockMap = new HashMap<>();
        Map<String, StockKbar> nestStockMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            stockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:nextStockKbars){
            nestStockMap.put(stockKbar.getStockCode(),stockKbar);
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
            List<PlankTimePairDTO> plankTens = judgePlanks100000(blockPairs);
            if(plankTens.size()<3){
                continue;
            }


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
        if(CollectionUtils.isEmpty(list)||list.size()<3){
            return map;
        }
        List<MarketMoneyDTO> marketMoneyDTOS = MarketMoneyDTO.marketLevelSort(list);

        if(marketMoneyDTOS.size()>60){
            List<MarketMoneyDTO> marketMoneyDTOS1 = marketMoneyDTOS.subList(0, 20);
            List<MarketMoneyDTO> marketMoneyDTOS3 = marketMoneyDTOS.subList(marketMoneyDTOS.size()-20,marketMoneyDTOS.size());
        }
        return map;
    }

    public Map<String,List<Integer>> getLevelIndex(int size){
        Map<String, List<Integer>> map = new HashMap<>();
        if(size<=3){
            map.put("frist",Lists.newArrayList(0,0));
            map.put("frist",Lists.newArrayList(1,1));
            map.put("frist",Lists.newArrayList(2,2));
        }
        Integer levelSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.3")).setScale(2, BigDecimal.ROUND_HALF_UP).toString());
        map.put("frist",Lists.newArrayList(0,(levelSize-1)));
        return map;
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

    public static void main(String[] args) {
        ArrayList<Integer> integers = Lists.newArrayList(0, 1, 2, 3, 4, 5, 6, 7, 8);
        List<Integer> integers1 = integers.subList(0, 1);
        List<Integer> integers2 = integers.subList(6, integers.size());
        System.out.println(111111);
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
