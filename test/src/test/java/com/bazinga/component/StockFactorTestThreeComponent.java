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
import org.apache.commons.lang3.time.DateUtils;
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
public class StockFactorTestThreeComponent {
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
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<StockFactorThreeTestDTO> buys = factorBigChange(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for (StockFactorThreeTestDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getFactorDate());
            list.add(dto.getMarketValue());
            list.add(dto.getEndRate());
            list.add(dto.getOpenRate());
            list.add(dto.getFactorValue());
            list.add(dto.getPreFactorValue());
            list.add(dto.getPlanks());
            list.add(dto.getBeforeRateDay3());
            list.add(dto.getBeforeRateDay5());
            list.add(dto.getBeforeRateDay10());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","因子巨变日期","市值","收盘涨幅","开盘涨幅","因子值","前一日因子值","连板","3日涨幅","5日涨幅","10日涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("因子巨变",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("因子巨变");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }
    public List<StockFactorThreeTestDTO> factorBigChange(List<CirculateInfo> circulateInfos){
        List<StockFactorThreeTestDTO> list = Lists.newArrayList();
        int h = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            h++;
            System.out.println(circulateInfo.getStockCode()+"==="+h);
            List<StockFactorThreeTestDTO> buys = Lists.newArrayList();
            List<StockFactor> stockFactors = getStockFactor(circulateInfo.getStockCode());
            BigDecimal preFactorValue = null;
            for (StockFactor factor:stockFactors){
                if(preFactorValue!=null&&factor.getIndex6()!=null&&factor.getIndex6().subtract(new BigDecimal("0.0005")).compareTo(preFactorValue)>=0){
                    StockFactorThreeTestDTO dto = new StockFactorThreeTestDTO();
                    dto.setStockCode(factor.getStockCode());
                    dto.setStockName(factor.getStockName());
                    dto.setFactorDate(factor.getKbarDate());
                    dto.setFactorValue(factor.getIndex6());
                    dto.setPreFactorValue(preFactorValue);
                    Date factorDate = DateUtil.parseDate(dto.getFactorDate(), DateUtil.yyyy_MM_dd);
                    Date firstDate = DateUtil.parseDate("20210101", DateUtil.yyyyMMdd);
                    if(!factorDate.before(firstDate)) {
                        buys.add(dto);
                    }
                }
                preFactorValue = factor.getIndex6();
            }
            if(buys.size()==0){
                continue;
            }
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            getStockRateInfo(circulateInfo,buys,stockKbars);
            for (StockFactorThreeTestDTO buy:buys){
                if(buy.getTradeDate()!=null) {
                    list.add(buy);
                }
            }

        }
        return list;
    }

    public void getStockRateInfo(CirculateInfo circulateInfo,List<StockFactorThreeTestDTO> buys,List<StockKbar> stockKbars){
        for(StockFactorThreeTestDTO buy:buys){
            String yyyymmdd = DateUtil.dateStringFormat(buy.getFactorDate(), DateUtil.yyyy_MM_dd, DateUtil.yyyyMMdd);
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            StockKbar preKbar = null;
            boolean flag = false;
            int days = 0;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                if(flag){
                    days++;
                }
                if(days==1){
                    boolean openUpperFlag = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getOpenPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(!openUpperFlag) {
                        buy.setTradeDate(stockKbar.getKbarDate());
                        BigDecimal profit = calEndNoPlankProfit(stockKbars, stockKbar);
                        buy.setProfit(profit);
                        break;
                    }
                }
                if(stockKbar.getKbarDate().equals(yyyymmdd)&&preKbar!=null) {
                    BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                    BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                    BigDecimal marketValue = new BigDecimal(circulateInfo.getCirculate()).multiply(stockKbar.getClosePrice()).setScale(2, BigDecimal.ROUND_HALF_UP).divide(new BigDecimal("100000000"),2,BigDecimal.ROUND_HALF_UP);
                    int planks = calPlanks(limitQueue);
                    buy.setEndRate(endRate);
                    buy.setOpenRate(openRate);
                    buy.setMarketValue(marketValue);
                    buy.setPlanks(planks);
                    calBeforeRate(stockKbars,buy);
                    flag = true;
                }
                preKbar  = stockKbar;
            }
        }
    }

    public List<TradeDatePool> getTradeDatePools(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
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

    public void calBeforeRate(List<StockKbar> stockKbars,StockFactorThreeTestDTO buy){
        String yyyymmdd = DateUtil.dateStringFormat(buy.getFactorDate(), DateUtil.yyyy_MM_dd, DateUtil.yyyyMMdd);
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i=0;
        StockKbar endStockKbar = null;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==3){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buy.setBeforeRateDay3(rate);
            }
            if(i==5){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buy.setBeforeRateDay5(rate);
            }
            if(i==10){
                BigDecimal rate = PriceUtil.getPricePercentRate(endStockKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                buy.setBeforeRateDay10(rate);
            }
            if(yyyymmdd.equals(stockKbar.getKbarDate())){
                flag = true;
                endStockKbar = stockKbar;
            }
        }
    }

    public BigDecimal calEndNoPlankProfit(List<StockKbar> stockKbars,StockKbar buyStockKbar){
        boolean flag = false;
        int i=0;
        StockKbar preKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
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

    public List<StockFactor> getStockFactor(String stockCode){
        StockFactorQuery query = new StockFactorQuery();
        query.setStockCode(stockCode);
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockFactor> stockFactors = stockFactorService.listByCondition(query);
        return stockFactors;
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
