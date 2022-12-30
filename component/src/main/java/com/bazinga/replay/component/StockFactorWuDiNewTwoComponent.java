package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.MarketMoneyDTO;
import com.bazinga.replay.dto.StockFactorLevelTestDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockFactorWuDiNewTwoComponent {
    @Autowired
    private ThsCirculateInfoService thsCirculateInfoService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private ThsStockKbarService thsStockKbarService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
    @Autowired
    private StockFactorService stockFactorService;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public static String leveStockCode = "600896,600555,600385,600090,300202,000673,300312,600870,300038,300367,600146,000613,300023,002464,300325,000611,000502,002447,002684,300064,002147,002770,600093,600275,002473,600209,600856,300178,002618,600652,600890,600091,002260,000687,600291,600695,603157,000585,603996,000835,600145,002619,000780,600723,600068,300362,002359,000760,600634,600614,002711,600485,002450,600891,002071,600701,000662,600247,600978,600677,600086,600687,600317";
    //public static String leveStockCode = "1111";


    public void factorTest(){
        List<TradeDatePool> tradeDatePools = getTradeDatePools();
        List<StockFactorLevelTestDTO> buys = getPlankTimePairs(tradeDatePools);
        List<Object[]> datas = Lists.newArrayList();
        for (StockFactorLevelTestDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getMarketValue());
            list.add(dto.getEndRate());
            list.add(dto.getNextDayOpenRate());
            list.add(dto.getLevel());
            list.add(dto.getMarketValueLevel());
            list.add(dto.getPlanks());
            list.add(dto.getAmount());
            list.add(dto.getPreAmount());
            list.add(dto.getPreIndex2a());
            list.add(dto.getIndex2a());
            list.add(dto.getBeforeRateDay3());
            list.add(dto.getBeforeRateDay5());
            list.add(dto.getBeforeRateDay10());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getNextDayLowRate());
            list.add(dto.getNextDayHighRate());
            list.add(dto.getBlockCount());
            list.add(dto.getBlockLevel());
            list.add(dto.getProfitAvgPrice());
            list.add(dto.getProfitEndPrice());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","市值","因子日收盘涨幅","交易日开盘涨幅","排名","市值排名","连板高度","因子日成交额","因子前一日成交额","前一日因子",
                "当日因子","3日涨幅","5日涨幅","10日涨幅","行业代码","行业名称","买入日低点","买入日高点","板块数量","板块排名","均价卖出","尾盘卖出"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("美国往事",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("美国往事");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public List<TradeDatePool> getTradeDatePools(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }

    public List<StockFactorLevelTestDTO> getPlankTimePairs(List<TradeDatePool> tradeDatePools){
        List<StockFactorLevelTestDTO> buys = Lists.newArrayList();
        int i =0;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            if(tradeDatePool.getTradeDate().before(DateUtil.parseDate("20220601", DateUtil.yyyyMMdd))){
                continue;
            }
            if(!tradeDatePool.getTradeDate().before(DateUtil.parseDate("20220610", DateUtil.yyyyMMdd))){
                continue;
            }
            List<StockFactorLevelTestDTO> dayBuys = Lists.newArrayList();
            i++;
            String dateyyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            String dateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            System.out.println(dateyyyyMMdd+"-----"+i);
            ThsCirculateInfoQuery circulateInfoQuery = new ThsCirculateInfoQuery();
            circulateInfoQuery.setTradeDate(dateyyyyMMdd);
            List<ThsCirculateInfo> circulateInfos = thsCirculateInfoService.listByCondition(circulateInfoQuery);
            Map<String, ThsCirculateInfo> circulateInfoMap = circulateInfos.stream().collect(Collectors.toMap(ThsCirculateInfo::getStockCode, circulateInfo -> circulateInfo));
            if(CollectionUtils.isEmpty(circulateInfos)){
                continue;
            }
            Date nextDate = commonComponent.afterTradeDate(tradeDatePool.getTradeDate());
            Date preDate = commonComponent.preTradeDate(tradeDatePool.getTradeDate());
            List<StockFactor> stockFactors200 = getStockFactors200(dateStr);
            Map<String, StockFactor> preFactorMap = indexFactorMap(DateUtil.format(preDate, DateUtil.yyyy_MM_dd));
            if(CollectionUtils.isEmpty(stockFactors200)){
                continue;
            }
            Map<String, Integer> marketSortMap = getMarketBigStocks(circulateInfoMap, dateyyyyMMdd);
            int level = 0;
            for (StockFactor stockFactor:stockFactors200){
                ThsCirculateInfo circulateInfo = circulateInfoMap.get(stockFactor.getStockCode());
                if(circulateInfo==null){
                    continue;
                }
                boolean stStock = isBlockStStock(circulateInfo.getStockCode(), dateyyyyMMdd);
                if(stStock){
                    continue;
                }
                List<ThsStockKbar> stockKbars = getStockKBarsDelete30Days(stockFactor.getStockCode());
                if(CollectionUtils.isEmpty(stockKbars)){
                    continue;
                }
                LimitQueue<ThsStockKbar> limitQueue = new LimitQueue<>(10);
                ThsStockKbar preKbar = null;
                for (ThsStockKbar stockKbar:stockKbars){
                    limitQueue.offer(stockKbar);
                    if(stockKbar.getKbarDate().equals(dateyyyyMMdd)&&preKbar!=null) {
                        BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                        int planks = calPlanks(limitQueue);
                        StockFactor preStockFactor = preFactorMap.get(stockKbar.getStockCode());
                        StockFactorLevelTestDTO buyDTO = new StockFactorLevelTestDTO();
                        buyDTO.setStockCode(circulateInfo.getStockCode());
                        buyDTO.setStockName(circulateInfo.getStockName());
                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                        buyDTO.setPlanks(planks);
                        buyDTO.setEndRate(endRate);
                        buyDTO.setMarketValue(stockKbar.getMarketValue());
                        buyDTO.setIndex2a(stockFactor.getIndex1());
                        buyDTO.setAmount(stockKbar.getTradeAmount());
                        buyDTO.setPreAmount(preKbar.getTradeAmount());
                        if(preStockFactor!=null){
                            buyDTO.setPreIndex2a(preStockFactor.getIndex1());
                        }
                        buyDTO.setMarketValueLevel(marketSortMap.get(circulateInfo.getStockCode()));
                        ThsStockKbar nextKbar = thsStockKbarService.getByUniqueKey(circulateInfo.getStockCode() + "_" + DateUtil.format(nextDate, DateUtil.yyyyMMdd));
                        if(nextKbar!=null){
                            level++;
                            buyDTO.setLevel(level);
                            String format = DateUtil.format(nextDate, DateUtil.yyyyMMdd);
                            buyDTO.setTradeDate(format);
                            calBeforeRate(stockKbars,buyDTO);
                            //getLowAndHighRate(circulateInfo.getStockCode(),nextKbar.getKbarDate(),stockKbar,nextKbar,buyDTO);
                            BigDecimal nextOpenRate = PriceUtil.getPricePercentRate(nextKbar.getAdjOpenPrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                            buyDTO.setNextDayOpenRate(nextOpenRate);
                            BigDecimal allProfit = calAllProfit(nextKbar, stockKbars);
                            BigDecimal endProfit = calEndNoPlankProfit(stockKbars,nextKbar);
                            buyDTO.setProfitAvgPrice(allProfit);
                            buyDTO.setProfitEndPrice(endProfit);
                            getBlockInfo(circulateInfo.getStockCode(),stockKbar.getKbarDate(),buyDTO);
                            dayBuys.add(buyDTO);
                        }
                        break;
                    }
                    preKbar = stockKbar;
                }
            }
            calBlockLeve(dayBuys);
            buys.addAll(dayBuys);
        }
        return buys;
    }

    public BigDecimal  calAllProfit(ThsStockKbar buyKbar, List<ThsStockKbar> stockKbars){
        int allSell = 0;
        boolean flag = false;
        for (ThsStockKbar stockKbar:stockKbars){
            if(flag){
                if(!stockKbar.getHighPrice().equals(stockKbar.getLowPrice())){
                    allSell++;
                }
            }
            if(allSell==1){
                if(stockKbar.getTradeAmount()!=null&&stockKbar.getTradeQuantity()!=null&&stockKbar.getTradeQuantity()!=0) {
                    BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100), 2, BigDecimal.ROUND_HALF_UP);
                    BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                    BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyKbar.getAdjOpenPrice()), buyKbar.getAdjOpenPrice());
                    return rate;
                }
                return null;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
    }

    public BigDecimal  calEndProfit(ThsStockKbar buyKbar, List<ThsStockKbar> stockKbars){
        int endSell = 0;
        boolean flag = false;
        ThsStockKbar preStockKbar = null;
        for (ThsStockKbar stockKbar:stockKbars){
            if(flag){
                if(!stockKbar.getHighPrice().equals(stockKbar.getLowPrice())) {
                    boolean endUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if (!endUpperPrice) {
                        endSell++;
                    }
                }
            }
            if(endSell==1){
                BigDecimal chuQuanAvgPrice = stockKbar.getAdjClosePrice();
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyKbar.getAdjOpenPrice()), buyKbar.getAdjOpenPrice());
                return rate;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
            preStockKbar = stockKbar;
        }
        return null;
    }

    public Map<String, Integer> getMarketBigStocks(Map<String, ThsCirculateInfo> circulateInfoMap, String tradeDate){
        Map<String,Integer> map = new HashMap<>();
        ThsStockKbarQuery query = new ThsStockKbarQuery();
        query.setKbarDate(tradeDate);
        List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockKbars)){
            return map;
        }
        List<MarketMoneyDTO> list = Lists.newArrayList();
        for (ThsStockKbar stockKbar:stockKbars){
            ThsCirculateInfo circulateInfo = circulateInfoMap.get(stockKbar.getStockCode());
            if(circulateInfo!=null) {
                MarketMoneyDTO marketMoneyDTO = new MarketMoneyDTO();
                marketMoneyDTO.setStockCode(circulateInfo.getStockCode());
                marketMoneyDTO.setStockName(circulateInfo.getStockName());
                marketMoneyDTO.setCirculate(circulateInfo.getCirculateZ());
                marketMoneyDTO.setMarketMoney(stockKbar.getMarketValue());
                list.add(marketMoneyDTO);
            }
        }
        List<MarketMoneyDTO> marketMoneyDTOS = MarketMoneyDTO.marketLevelSort(list);
        List<MarketMoneyDTO> reverse = Lists.reverse(marketMoneyDTOS);
        int level = 0;
        for (MarketMoneyDTO marketMoneyDTO:reverse){
            level++;
            map.put(marketMoneyDTO.getStockCode(),level);
        }
        return map;
    }

    public void getBlockInfo(String stockCode,String tradeDate,StockFactorLevelTestDTO buy){
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        for (HistoryBlockStocks blockStocks:historyBlockStocks){
            if(!blockStocks.getBlockCode().startsWith("881")){
                continue;
            }
            String stocks = blockStocks.getStocks();
            if(stocks.contains(stockCode)){
                buy.setBlockName(blockStocks.getBlockName());
                buy.setBlockCode(blockStocks.getBlockCode());
                return;
            }
        }
    }
    public void calBlockLeve(List<StockFactorLevelTestDTO> buys){
        if(CollectionUtils.isEmpty(buys)){
            return;
        }
        Map<String,Integer> map = new HashMap<>();
        for (StockFactorLevelTestDTO dto:buys){
            if(StringUtils.isBlank(dto.getBlockCode())){
                continue;
            }
            Integer count = map.get(dto.getBlockCode());
            if(count==null){
                count = 0;
            }
            count = count + 1;
            map.put(dto.getBlockCode(),count);
        }
        List<Integer> counts = Lists.newArrayList();
        for (String blockCode:map.keySet()){
            Integer count = map.get(blockCode);
            if(!counts.contains(count)) {
                counts.add(count);
            }
        }
        Collections.sort(counts);
        List<Integer> reverse = Lists.reverse(counts);
        Map<Integer, Integer> levelMap = new HashMap<>();
        Integer i = 1;
        for (Integer count:reverse){
            levelMap.put(count,i);
            i = i+1;
        }
        Map<String, Integer> blockLevel = new HashMap<>();
        for (String blockCode:map.keySet()){
            Integer count = map.get(blockCode);
            Integer level = levelMap.get(count);
            blockLevel.put(blockCode,level);
        }
        for (StockFactorLevelTestDTO dto:buys){
            if(StringUtils.isBlank(dto.getBlockCode())) {
                continue;
            }
            Integer level = blockLevel.get(dto.getBlockCode());
            dto.setBlockLevel(level);
            dto.setBlockCount(map.get(dto.getBlockCode()));
        }

    }




    public void calBeforeRate(List<ThsStockKbar> stockKbars,StockFactorLevelTestDTO buy){
        List<ThsStockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i=0;
        ThsStockKbar endStockKbar = null;
        for (ThsStockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                endStockKbar = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buy.setBeforeRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buy.setBeforeRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buy.setBeforeRateDay10(rate);
            }
            if(buy.getTradeDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
    }


    public void  getLowAndHighRate(String stockCode,String tradeDate,ThsStockKbar preStockKbar,ThsStockKbar stockKbar,StockFactorLevelTestDTO buyDTO){
        if(leveStockCode.contains(stockCode)){
            return;
        }
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        BigDecimal lowPrice = null;
        BigDecimal highPrice = null;
        Date signDate = DateUtil.parseDate("09:35", DateUtil.HH_MM);
        for (ThirdSecondTransactionDataDTO data:datas){
            Date date = DateUtil.parseDate(data.getTradeTime(), DateUtil.HH_MM);
            if(!date.before(signDate)){
                break;
            }
            if(lowPrice==null||data.getTradePrice().compareTo(lowPrice)==-1){
                lowPrice = data.getTradePrice();
            }
            if(highPrice==null||data.getTradePrice().compareTo(highPrice)==1){
                highPrice = data.getTradePrice();
            }
        }
        if(lowPrice!=null){
            BigDecimal chuQuanLowPrice = chuQuanAvgPrice(lowPrice, stockKbar);
            BigDecimal lowRate = PriceUtil.getPricePercentRate(chuQuanLowPrice.subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
            buyDTO.setNextDayLowRate(lowRate);
        }
        if(highPrice!=null){
            BigDecimal chuQuanHighPrice = chuQuanAvgPrice(highPrice, stockKbar);
            BigDecimal highRate = PriceUtil.getPricePercentRate(chuQuanHighPrice.subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
            buyDTO.setNextDayHighRate(highRate);
        }
    }







    public List<TradeDatePool> getTradeDates(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd));
        /*query.setTradeDateTo(DateUtil.parseDate("20220501",DateUtil.yyyyMMdd));*/
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


    public List<ThsStockKbar> limitQueueToList(LimitQueue<ThsStockKbar> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        List<ThsStockKbar> list = Lists.newArrayList();
        Iterator<ThsStockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            ThsStockKbar next = iterator.next();
            list.add(next);
        }
        return list;
    }






    public BigDecimal calProfit(List<ThsStockKbar> stockKbars,ThsStockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (ThsStockKbar stockKbar:stockKbars){
            if(flag){
                if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())!=0) {
                    i++;
                }
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getTradeAmount().divide(new BigDecimal(stockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
                avgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjHighPrice()), buyStockKbar.getAdjHighPrice());
                return profit;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
    }

    public BigDecimal calEndNoPlankProfit(List<ThsStockKbar> stockKbars,ThsStockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        ThsStockKbar preKbar = null;
        for (ThsStockKbar stockKbar:stockKbars){
            if(flag){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                if(!endUpper){
                    endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getAdjClosePrice(), preKbar.getAdjClosePrice(), stockKbar.getKbarDate());
                }
                if(!endUpper) {
                    i++;
                }
            }
            if(i==1){
                BigDecimal avgPrice = stockKbar.getAdjClosePrice();
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjOpenPrice()), buyStockKbar.getAdjOpenPrice());
                return profit;
            }
            if(buyStockKbar.getKbarDate().equals(stockKbar.getKbarDate())){
                flag = true;
            }
            preKbar = stockKbar;
        }

        return null;
    }




    public List<ThsStockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            ThsStockKbarQuery query = new ThsStockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(query);
            List<ThsStockKbar> result = Lists.newArrayList();
            for (ThsStockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<ThsStockKbar> best = deleteNewStockTimes(stockKbars, 2000);
            return best;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
    public List<ThsStockKbar> deleteNewStockTimes(List<ThsStockKbar> list, int size){
        List<ThsStockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        ThsStockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (ThsStockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }

    public List<StockFactor> getStockFactors200(String tradeDateString){
        try {
            StockFactorQuery query = new StockFactorQuery();
            query.setKbarDate(tradeDateString);
            query.addOrderBy("index1", Sort.SortType.DESC);
            query.setLimit(200);
            List<StockFactor> stockFactors = stockFactorService.listByCondition(query);
            return stockFactors;
        }catch (Exception e){
            return null;
        }
    }

    public Map<String,StockFactor> indexFactorMap(String tradeDateString){
            StockFactorQuery query = new StockFactorQuery();
            query.setKbarDate(tradeDateString);
            List<StockFactor> stockFactors = stockFactorService.listByCondition(query);
            Map<String, StockFactor> map = new HashMap<>();
            if(CollectionUtils.isEmpty(stockFactors)){
                return map;
            }
            for (StockFactor stockFactor:stockFactors){
                map.put(stockFactor.getStockCode(),stockFactor);
            }
            return map;

    }


    public BigDecimal chuQuanAvgPrice(BigDecimal avgPrice,ThsStockKbar kbar){
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

    public int calPlanks(LimitQueue<ThsStockKbar> limitQueue){
        List<ThsStockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<3){
            return 0;
        }
        List<ThsStockKbar> reverse = Lists.reverse(stockKbars);
        int planks = 0;
        int i=0;
        ThsStockKbar nextStockKbar = null;
        for (ThsStockKbar stockKbar:reverse){
            i++;
            if(i>=2){
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


    public boolean isFirstPlank(LimitQueue<ThsStockKbar> limitQueue){
        List<ThsStockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<3){
            return false;
        }
        List<ThsStockKbar> reverse = Lists.reverse(stockKbars);
        int i=0;
        ThsStockKbar nextStockKbar = null;
        for (ThsStockKbar stockKbar:reverse){
            i++;
            if(i==3){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), nextStockKbar.getClosePrice(), stockKbar.getClosePrice(), nextStockKbar.getKbarDate());
                if(endUpper){
                    return false;
                }else {
                    return true;
                }
            }
            nextStockKbar = stockKbar;
        }
        return false;
    }


    public List<ThsStockKbar> getStockKbarsByKBarDate(String kbarDate){
        try {
            ThsStockKbarQuery query = new ThsStockKbarQuery();
            query.setKbarDate(kbarDate);
            List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(query);
            if(CollectionUtils.isEmpty(stockKbars)){
                return null;
            }
            return stockKbars;
        }catch (Exception e){
            return null;
        }
    }



    public void getHistoryBlockInfoTest(){
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(new HistoryBlockInfoQuery());
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            if(!historyBlockInfo.getBlockCode().startsWith("885")){
                continue;
            }
            HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
            query.setBlockCode(historyBlockInfo.getBlockCode());
            query.addOrderBy("trade_date", Sort.SortType.ASC);
            List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
            String  blockCodeStr = historyBlockInfo.getBlockCode()+"_"+historyBlockInfo.getMarketDate();
            if(!CollectionUtils.isEmpty(historyBlockStocks)){
               /* String tradeDate = historyBlockStocks.get(0).getTradeDate();
                if(!tradeDate.equals(historyBlockInfo.getMarketDate())){
                    System.out.println(blockCodeStr);
                }*/
            }else{
                System.out.println(blockCodeStr);
            }

            /*String startTradeDate = historyBlockStocks.get(0).getTradeDate();
            String endTradeDate = historyBlockStocks.get(historyBlockStocks.size()-1).getTradeDate();
            blockCodeStr = blockCodeStr+"==="+startTradeDate+"==="+endTradeDate;
            System.out.println(blockCodeStr);*/


        }
    }
    /**
     * 使用同花顺板块st内查询
     * @param tradeDate
     * @return
     */
    public boolean isBlockStStock(String stockCode,String tradeDate) {
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode("885699");
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        if(CollectionUtils.isEmpty(historyBlockStocks)){
            return false;
        }
        HistoryBlockStocks blockStocks = historyBlockStocks.get(0);
        if(StringUtils.isBlank(blockStocks.getStocks())){
            return false;
        }
        String stocks = blockStocks.getStocks();
        if(stocks.contains(stockCode)){
            return true;
        }
        return false;
    }


}
