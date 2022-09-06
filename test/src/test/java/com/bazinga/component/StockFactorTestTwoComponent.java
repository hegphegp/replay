package com.bazinga.component;

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
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockFactorTestTwoComponent {
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
    @Autowired
    private StockFactorService stockFactorService;

    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");



    public void factorTest(){
        List<TradeDatePool> tradeDatePools = getTradeDatePools();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, CirculateInfo> circulateInfoMap = circulateInfos.stream().collect(Collectors.toMap(CirculateInfo::getStockCode, circulateInfo -> circulateInfo));
        List<StockFactorLevelTestDTO> buys = getPlankTimePairs(circulateInfoMap,tradeDatePools);
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
            list.add(dto.getNextDayLowRate());
            list.add(dto.getNextDayHighRate());
            list.add(dto.getLevel());
            list.add(dto.getPlanks());
            list.add(dto.getIndex2a());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","市值","当日收盘涨幅","次日开盘涨幅","次日35之前最低涨幅","次日35之前最搞涨幅","排名","连板高度","index1"};
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

    public List<StockFactorLevelTestDTO> getPlankTimePairs(Map<String, CirculateInfo> circulateInfoMap,List<TradeDatePool> tradeDatePools){
        List<StockFactorLevelTestDTO> buys = Lists.newArrayList();
        int i =0;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            if(tradeDatePool.getTradeDate().before(DateUtil.parseDate("20210501", DateUtil.yyyyMMdd))){
                continue;
            }
            /*if(tradeDatePool.getTradeDate().after(DateUtil.parseDate("20220621", DateUtil.yyyyMMdd))){
                continue;
            }*/
            i++;
            String dateyyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            String dateStr = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            Date nextDate = commonComponent.afterTradeDate(tradeDatePool.getTradeDate());
            System.out.println(dateyyyyMMdd+"-----"+i);
            List<StockFactor> stockFactors200 = getStockFactors200(dateStr);
            if(CollectionUtils.isEmpty(stockFactors200)){
                continue;
            }
            List<String> marketBigStocks = getMarketBigStocks(circulateInfoMap, dateyyyyMMdd);
            int level = 0;
            for (StockFactor stockFactor:stockFactors200){
                CirculateInfo circulateInfo = circulateInfoMap.get(stockFactor.getStockCode());
                if(circulateInfo==null){
                    continue;
                }
                List<StockKbar> stockKbars = getStockKBarsDelete30Days(stockFactor.getStockCode());
                if(CollectionUtils.isEmpty(stockKbars)){
                    continue;
                }
                LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
                StockKbar preKbar = null;
                for (StockKbar stockKbar:stockKbars){
                    limitQueue.offer(stockKbar);
                    if(stockKbar.getKbarDate().equals(dateyyyyMMdd)&&preKbar!=null) {
                        BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                        BigDecimal marketValue = new BigDecimal(circulateInfo.getCirculateZ()).multiply(stockKbar.getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("100000000"),2,BigDecimal.ROUND_HALF_UP);
                        if(CollectionUtils.isEmpty(marketBigStocks)||!marketBigStocks.contains(circulateInfo.getStockCode())){
                            int planks = calPlanks(limitQueue);
                            StockFactorLevelTestDTO buyDTO = new StockFactorLevelTestDTO();
                            buyDTO.setStockCode(circulateInfo.getStockCode());
                            buyDTO.setStockName(circulateInfo.getStockName());
                            buyDTO.setTradeDate(stockKbar.getKbarDate());
                            buyDTO.setPlanks(planks);
                            buyDTO.setEndRate(endRate);
                            buyDTO.setMarketValue(marketValue);
                            buyDTO.setIndex2a(stockFactor.getIndex1());
                            StockKbar nextKbar = stockKbarService.getByUniqueKey(circulateInfo.getStockCode() + "_" + DateUtil.format(nextDate, DateUtil.yyyyMMdd));
                            if(nextKbar!=null){
                                level++;
                                buyDTO.setLevel(level);
                                BigDecimal nextOpenRate = PriceUtil.getPricePercentRate(nextKbar.getAdjOpenPrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                                buyDTO.setNextDayOpenRate(nextOpenRate);
                                getLowAndHighRate(circulateInfo.getStockCode(),nextKbar.getKbarDate(),stockKbar,nextKbar,buyDTO);
                                buys.add(buyDTO);
                            }
                        }
                        break;
                    }
                    preKbar = stockKbar;
                }
            }
        }
        return buys;
    }

    public List<String> getMarketBigStocks(Map<String,CirculateInfo> circulateInfoMap,String tradeDate){
        List<String> stocks = Lists.newArrayList();
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDate(tradeDate);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockKbars)){
            return stocks;
        }
        List<MarketMoneyDTO> list = Lists.newArrayList();
        for (StockKbar stockKbar:stockKbars){
            CirculateInfo circulateInfo = circulateInfoMap.get(stockKbar.getStockCode());
            if(circulateInfo!=null) {
                BigDecimal marketMoney = new BigDecimal(circulateInfo.getCirculate()).multiply(stockKbar.getClosePrice()).setScale(4,BigDecimal.ROUND_HALF_UP);
                MarketMoneyDTO marketMoneyDTO = new MarketMoneyDTO();
                marketMoneyDTO.setStockCode(circulateInfo.getStockCode());
                marketMoneyDTO.setStockName(circulateInfo.getStockName());
                marketMoneyDTO.setCirculate(circulateInfo.getCirculateZ());
                marketMoneyDTO.setMarketMoney(marketMoney);
                if(marketMoney.compareTo(new BigDecimal("20000000000"))==1) {
                    list.add(marketMoneyDTO);
                }
            }
        }
        if(CollectionUtils.isEmpty(list)){
            return stocks;
        }
        List<MarketMoneyDTO> marketMoneyDTOS = MarketMoneyDTO.marketLevelSort(list);
        List<MarketMoneyDTO> reverse = Lists.reverse(marketMoneyDTOS);
        if(reverse.size()>200){
            reverse = reverse.subList(0,200);
        }
        for (MarketMoneyDTO marketMoneyDTO:reverse){
            stocks.add(marketMoneyDTO.getStockCode());
        }
        return stocks;
    }



    public static void main(String[] args) {
        BlocKFollowStaticBuyDTO buyDTO1 = new BlocKFollowStaticBuyDTO();
        buyDTO1.setAmountRate(new BigDecimal(10));
        BlocKFollowStaticBuyDTO buyDTO2 = new BlocKFollowStaticBuyDTO();
        buyDTO2.setAmountRate(new BigDecimal(20));
        BlocKFollowStaticBuyDTO buyDTO3 = new BlocKFollowStaticBuyDTO();
        buyDTO3.setAmountRate(new BigDecimal(30));
        BlocKFollowStaticBuyDTO buyDTO4 = new BlocKFollowStaticBuyDTO();
        buyDTO4.setAmountRate(new BigDecimal(40));
        BlocKFollowStaticBuyDTO buyDTO5 = new BlocKFollowStaticBuyDTO();
        buyDTO5.setAmountRate(null);
        List<BlocKFollowStaticBuyDTO> buys = Lists.newArrayList(buyDTO3, buyDTO4, buyDTO1,buyDTO2);
        List<BlocKFollowStaticBuyDTO> buysAmountSorts = buys.stream().sorted(Comparator.comparing(BlocKFollowStaticBuyDTO::getAmountRate)).collect(Collectors.toList());

        List<BlocKFollowStaticBuyDTO> blocKFollowStaticBuyDTOS = BlocKFollowStaticBuyDTO.amountRateSort(buys);
        System.out.println(blocKFollowStaticBuyDTOS);
    }




    public void  getLowAndHighRate(String stockCode,String tradeDate,StockKbar preStockKbar,StockKbar stockKbar,StockFactorLevelTestDTO buyDTO){
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

    public String firstPlankTime(String stockCode,StockKbar preStockKbar,String tradeDate,List<ThirdSecondTransactionDataDTO> datas){
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        String preMin = "09:25";
        Integer index = -1;
        boolean preIsUpper = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            Integer tradeType = data.getTradeType();
            String tradeTime = data.getTradeTime();
            boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preStockKbar.getClosePrice(), tradeDate);
            if (tradeTime.equals(preMin)) {
                index++;
            } else {
                preMin = tradeTime;
                index = 0;
            }
            if(tradeTime.equals("09:25")&&historyUpperPrice){
                preIsUpper = true;
            }
            if (tradeType!=0 && tradeType != 1) {
                continue;
            }
            String timeString = timeToSecond(tradeTime, index);
            if(historyUpperPrice && !preIsUpper){
                return timeString;
            }
            if(historyUpperPrice&&tradeType==1){
                preIsUpper = true;
            }else{
                preIsUpper = false;
            }
        }
        return null;
    }


    public void getRateSpeed(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigDecimal preEndPrice,StrongPlankDefineTestDTO buyDTO){
        if(limitQueue.size()<2){
            return;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        BigDecimal lowPrice = null;
        int i = 0;
        int index = 0;
        while (iterator.hasNext()){
            index++;
            i++;
            ThirdSecondTransactionDataDTO next = iterator.next();
            if(index==limitQueue.size()){
                BigDecimal plankSpeed = PriceUtil.getPricePercentRate(next.getTradePrice().subtract(lowPrice), preEndPrice);
                buyDTO.setHeartTimes(i);
                buyDTO.setSpeedRate(plankSpeed);
            }
            if(lowPrice==null || next.getTradePrice().compareTo(lowPrice)<=0){
                lowPrice = next.getTradePrice();
                i=0;
            }
        }
    }

    public void getRateSpeed5(LimitQueue<ThirdSecondTransactionDataDTO> limitQueue,BigDecimal preEndPrice,StrongPlankDefineTestDTO buyDTO){
        if(limitQueue.size()<2){
            return;
        }
        Iterator<ThirdSecondTransactionDataDTO> iterator = limitQueue.iterator();
        BigDecimal lowPrice = null;
        BigDecimal first = null;
        int i = 0;
        int index = 0;
        while (iterator.hasNext()){
            index++;
            i++;
            ThirdSecondTransactionDataDTO next = iterator.next();
            if(i==1){
                first = next.getTradePrice();
            }
            if(index==limitQueue.size()){
                BigDecimal plankSpeed = PriceUtil.getPricePercentRate(next.getTradePrice().subtract(lowPrice), preEndPrice);
                BigDecimal plankSpeed5Min = PriceUtil.getPricePercentRate(next.getTradePrice().subtract(first), preEndPrice);
                buyDTO.setSpeedRate5(plankSpeed);
                buyDTO.setSpeedRate5Min(plankSpeed5Min);
            }
            if(lowPrice==null || next.getTradePrice().compareTo(lowPrice)<=0){
                lowPrice = next.getTradePrice();
                i=0;
            }
        }
    }

    public Integer interSecond(String start,String end){
        if(StringUtils.isBlank(start)||StringUtils.isBlank(end)){
            return null;
        }
        Date startTime = DateUtil.parseDate(start, DateUtil.HHmmss_DEFALT);
        Date endTime = DateUtil.parseDate(end, DateUtil.HHmmss_DEFALT);
        Date morningStart = DateUtil.parseDate("09:30:00", DateUtil.HHmmss_DEFALT);
        Date morningEnd = DateUtil.parseDate("11:30:00", DateUtil.HHmmss_DEFALT);
        Date afterStart = DateUtil.parseDate("13:00:00", DateUtil.HHmmss_DEFALT);
        if(!endTime.after(startTime)){
            return null;
        }
        for (int i = 1;i<=100000;i++){
            if(startTime.before(morningStart)){
                startTime = morningStart;
            }
            if((!startTime.before(morningEnd))&&startTime.before(afterStart)){
                startTime = afterStart;
            }
            startTime = DateUtil.addSeconds(startTime, 1);
            if(!startTime.before(endTime)){
                return i;
            }
        }
        return null;
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


    public boolean gatherIsSudden(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)||datas.size()<2){
            return false;
        }
        ThirdSecondTransactionDataDTO data1 = datas.get(0);
        ThirdSecondTransactionDataDTO data2 = datas.get(1);
        if(data1==null||data2==null||data2.getTradeType()!=0){
            return false;
        }
        boolean suddenGatherPrice = PriceUtil.isHistorySuddenPrice(stockKbar.getStockCode(), data1.getTradePrice(), preEndPrice,stockKbar.getKbarDate());
        boolean suddenOnePrice = PriceUtil.isHistorySuddenPrice(stockKbar.getStockCode(), data2.getTradePrice(), preEndPrice,stockKbar.getKbarDate());
        if(suddenGatherPrice&&suddenOnePrice&&data2.getTradeType()==0){
            return true;
        }
        return false;
    }

    public static long timeToLong(String time,int index){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        int second = index * 3;
        if(second<10) {
            timeStr = timeStr + "0" +second;
        }else{
            timeStr = timeStr+second;
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }

    public static String timeToSecond(String time,int index){
        int second = index * 3;
        if(second<10) {
            time = time + ":0" +second;
        }else{
            time = time+":"+second;
        }
        return time;
    }

    public static long timeToLong(String time){
        String timeStr = time.replace(":", "");
        if(timeStr.startsWith("09")){
            timeStr = timeStr.substring(1);
        }
        Long timeLong = Long.valueOf(timeStr);
        return timeLong;
    }


    public BigDecimal calProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        for (StockKbar stockKbar:stockKbars){
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

    public BigDecimal calEndNoPlankProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        StockKbar preKbar = null;
        for (StockKbar stockKbar:stockKbars){
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




    public List<StockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            StockKbarQuery query = new StockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
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
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<3){
            return 0;
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        int planks = 0;
        int i=0;
        StockKbar nextStockKbar = null;
        for (StockKbar stockKbar:reverse){
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


    public boolean isFirstPlank(LimitQueue<StockKbar> limitQueue){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<3){
            return false;
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        int i=0;
        StockKbar nextStockKbar = null;
        for (StockKbar stockKbar:reverse){
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
    public void tenDayPlanks(LimitQueue<StockKbar> limitQueue,LowEndHighTestDTO testDTO){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<12){
            return;
        }
        int planks = 0;
        StockKbar preStockKbar = null;
        int i= 0;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(preStockKbar!=null&&i<stockKbars.size()){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), preStockKbar.getKbarDate());
                if(endUpper){
                    planks++;
                }
            }
            preStockKbar = stockKbar;
        }
        testDTO.setTenDayPlanks(planks);
        StockKbar first = stockKbars.get(0);
        StockKbar last = stockKbars.get(stockKbars.size() - 2);
        BigDecimal tenDatRate = PriceUtil.getPricePercentRate(last.getAdjClosePrice().subtract(first.getAdjClosePrice()), first.getAdjClosePrice());
        testDTO.setTenDayRate(tenDatRate);
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



}
