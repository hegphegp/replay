package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlocKFollowStaticBuyDTO;
import com.bazinga.dto.LowEndHighTestDTO;
import com.bazinga.dto.TwoToThreeTestDTO;
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
public class BuyTwoToThreeComponent {
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



    public void buyTwoThree(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<TwoToThreeTestDTO> buys = getPlankTimePairs(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for (TwoToThreeTestDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getTFlag());
            list.add(dto.getOpenRate());
            list.add(dto.getEndFlag());
            list.add(dto.getBuyTime());
            list.add(dto.getGatherAmount());
            list.add(dto.getPreAmount());
            list.add(dto.getCirculateZ());
            list.add(dto.getCirculate());
            list.add(dto.getPlankPrice());
            list.add(dto.getTenDayPlanks());
            list.add(dto.getProfit());
            list.add(dto.getEndNoPlankSellProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","是否是t板（0不是 1是）","开盘涨幅","尾盘是否封住（0 不是 1是）"
                ,"买入时间","集合成交额","昨日成交额","流通z","总流通","板价","前10天板数","次日盈利","尾盘不板卖出盈利" };
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("二进三",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("二进三");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public List<TwoToThreeTestDTO> getPlankTimePairs(List<CirculateInfo> circulateInfos){
        List<TwoToThreeTestDTO> buys = Lists.newArrayList();
        int i =0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(circulateInfo.getStockCode()+"-----"+i);
            if(buys.size()>=10){
                return buys;
            }
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue12 = new LimitQueue<>(12);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue12.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20220101", DateUtil.yyyyMMdd))){
                    continue;
                }
                /*if(date.after(DateUtil.parseDate("20220401", DateUtil.yyyyMMdd))){
                    continue;
                }*/
                if(preKbar!=null) {
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(highUpper){
                        BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                        TwoToThreeTestDTO testDTO = new TwoToThreeTestDTO();
                        String buyTime = getBuyTime(circulateInfo.getStockCode(), stockKbar.getKbarDate(), preKbar.getClosePrice(), testDTO);
                        if(StringUtils.isNotBlank(buyTime)) {
                            testDTO.setStockCode(stockKbar.getStockCode());
                            testDTO.setStockName(stockKbar.getStockName());
                            testDTO.setTradeDate(stockKbar.getKbarDate());
                            testDTO.setCirculate(circulateInfo.getCirculate());
                            testDTO.setCirculateZ(circulateInfo.getCirculateZ());
                            testDTO.setPlankPrice(stockKbar.getHighPrice());
                            testDTO.setOpenRate(openRate);
                            int tenDayPlanks = tenDayPlanks(limitQueue12);
                            testDTO.setTenDayPlanks(tenDayPlanks);
                            testDTO.setPreAmount(preKbar.getTradeAmount());
                            if(endUpper) {
                                testDTO.setEndFlag(1);
                            }

                            BigDecimal profit = calProfit(stockKbars, stockKbar);
                            BigDecimal endNoPlankProfit = calEndNoPlankProfit(stockKbars, stockKbar);
                            testDTO.setProfit(profit);
                            testDTO.setEndNoPlankSellProfit(endNoPlankProfit);
                        }

                    }

                }
                preKbar = stockKbar;
            }
        }
        return buys;
    }


    public String getBuyTime(String stockCode,String tradeDate,BigDecimal preEndPrice,TwoToThreeTestDTO testDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        boolean prePlankFlag = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            Integer tradeType = data.getTradeType();
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preEndPrice, tradeDate);
            if(data.getTradeTime().equals("09:25")){
                BigDecimal gatherAmount = new BigDecimal(data.getTradeQuantity() * 100).multiply(data.getTradePrice());
                testDTO.setGatherAmount(gatherAmount);
                if(upperPrice) {
                    testDTO.setTFlag(1);
                    prePlankFlag = true;
                }
                continue;
            }
            if(tradeType!=1||tradeType!=0){
                continue;
            }
            boolean plankFlag = false;
            if(tradeType==1&&upperPrice){
                plankFlag = true;
            }
            if(plankFlag&&!prePlankFlag){
                testDTO.setBuyTime(data.getTradeTime());
                return data.getTradeTime();
            }
            if(plankFlag){
                prePlankFlag = true;
            }else{
                prePlankFlag = false;
            }
        }
        return null;
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




    public BigDecimal getStockBuyPrice(String stockCode,String tradeDate,String buyTime,StockKbar preStockKbar,BlocKFollowStaticBuyDTO buyDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        long buyTimeInt = timeToLong(buyTime);
        String preMin = "09:25";
        Integer index = -1;
        BigDecimal tradeAmount = BigDecimal.ZERO;
        for (ThirdSecondTransactionDataDTO data : datas) {
            BigDecimal tradePrice = data.getTradePrice();
            Integer tradeType = data.getTradeType();
            String tradeTime = data.getTradeTime();
            BigDecimal amount = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100));
            if(tradeAmount!=null){
                tradeAmount = tradeAmount.add(amount);
            }
            boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preStockKbar.getClosePrice(), tradeDate);
            if (tradeTime.equals(preMin)) {
                index++;
            } else {
                preMin = tradeTime;
                index = 0;
            }
            long timeLong = timeToLong(tradeTime, index);
            if (timeLong >= buyTimeInt) {
                if (historyUpperPrice && tradeType == 1) {
                    return null;
                }
                BigDecimal amountRate = tradeAmount.divide(preStockKbar.getTradeAmount(), 8, BigDecimal.ROUND_HALF_UP);
                buyDTO.setAmountRate(amountRate);
                return tradePrice;
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
                BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjOpenPrice()), buyStockKbar.getAdjOpenPrice());
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
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<12){
            return 0;
        }
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        int planks = 0;
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
    public int tenDayPlanks(LimitQueue<StockKbar> limitQueue){
        List<StockKbar> stockKbars = limitQueueToList(limitQueue);
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<3){
            return 0;
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
