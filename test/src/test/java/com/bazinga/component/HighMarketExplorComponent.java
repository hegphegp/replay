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
public class HighMarketExplorComponent {
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



    public void bigMarketExplor(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<HighMarketExplorDTO> buys = marketBuy(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for (HighMarketExplorDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getEndRate());
            list.add(dto.getMarketValue());
            list.add(dto.isHighUpper());
            list.add(dto.getNextHighRate());
            list.add(dto.getNextLowRate());
            list.add(dto.getNextOpenRate());
            list.add(dto.getNextEndRate());
            list.add(dto.getBeforeRateDay3());
            list.add(dto.getBeforeRateDay5());
            list.add(dto.getBeforeRateDay10());
            list.add(dto.getHighRateBeforeDay5());
            list.add(dto.getHighDay3());
            list.add(dto.getLowDay3());
            list.add(dto.getEndDay3());
            list.add(dto.getLevel());
            list.add(dto.getProfit1());
            list.add(dto.getProfit2());
            list.add(dto.getProfit3());
            list.add(dto.getProfit4());
            list.add(dto.getProfit5());
            list.add(dto.getProfit6());
            list.add(dto.getProfit7());
            list.add(dto.getProfit8());
            list.add(dto.getProfit9());
            list.add(dto.getProfit10());
            list.add(dto.getProfit11());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","买入日收盘涨幅","市值","是否触碰涨停","次日最高涨幅","次日最低涨幅","次日开盘涨幅","次日收盘涨幅","前3日涨幅","前5日涨幅","前10日涨幅","前5日最大涨幅","后3日最高涨幅","后3日最低涨幅"
                ,"后3日收盘涨幅","level","1个点盈利","2个点盈利","3个点盈利","4个点盈利","5个点盈利","6个点盈利","7个点盈利","8个点盈利","9个点盈利","10个点盈利","11个点盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("大市值异动",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("大市值异动");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public List<HighMarketExplorDTO> marketBuy(List<CirculateInfo> circulateInfos){
        Map<String, String> stInfoMap = getStInfo();
        Map<String, List<HighMarketExplorDTO>> map = new HashMap<>();
        List<HighMarketExplorDTO> buys = Lists.newArrayList();
        int i =0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(circulateInfo.getStockCode()+"-----"+i);
            /*if(buys.size()>=100){
                return buys;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(3);
            LimitQueue<StockKbar> limitQueue4 = new LimitQueue<>(3);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue.offer(stockKbar);
                limitQueue4.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20180101", DateUtil.yyyyMMdd))){
                    preKbar = stockKbar;
                    continue;
                }
                if(preKbar!=null) {
                    boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                    BigDecimal marketValue = (new BigDecimal(circulateInfo.getCirculate()).multiply(preKbar.getClosePrice())).divide(new BigDecimal("100000000"),2,BigDecimal.ROUND_HALF_UP);
                    boolean buyReason = getBuyReason(limitQueue4);
                    if(buyReason && marketValue.compareTo(new BigDecimal("100"))>0){
                        HighMarketExplorDTO buyDTO = new HighMarketExplorDTO();
                        buyDTO.setStockCode(circulateInfo.getStockCode());
                        buyDTO.setStockName(circulateInfo.getStockName());
                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                        buyDTO.setMarketValue(marketValue);
                        buyDTO.setEndRate(endRate);
                        buyDTO.setHighUpper(highUpper);
                        getBeforeRateInfo(stockKbar,buyDTO,stockKbars);
                        getNextRateInfo(stockKbar,buyDTO,stockKbars);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("1"),1);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("2"),2);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("3"),3);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("4"),4);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("5"),5);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("6"),6);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("7"),7);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("8"),8);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("9"),9);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("10"),10);
                        getProfitInfo(stockKbar,buyDTO,stockKbars,new BigDecimal("11"),11);
                        List<HighMarketExplorDTO> dtos = map.get(buyDTO.getTradeDate());
                        if(dtos==null){
                            dtos = Lists.newArrayList();
                            map.put(buyDTO.getTradeDate(),dtos);
                        }
                        String stString = stInfoMap.get(stockKbar.getKbarDate());
                        if(!stString.contains(stockKbar.getStockCode())) {
                            dtos.add(buyDTO);
                        }else{
                            System.out.println(stockKbar.getStockCode()+"=========="+stockKbar.getKbarDate());
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        for (String tradeDate:map.keySet()){
            List<HighMarketExplorDTO> dtos = map.get(tradeDate);
            List<HighMarketExplorDTO> sortDtos = HighMarketExplorDTO.endRateSort(dtos);
            List<HighMarketExplorDTO> reverse = Lists.reverse(sortDtos);
            int level = 0;
            for (HighMarketExplorDTO rev:reverse){
                level++;
                rev.setLevel(level);
            }
            buys.addAll(reverse);
        }
        return buys;
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




    public void  getBeforeRateInfo(StockKbar buyKbar,HighMarketExplorDTO dto,List<StockKbar> stockKbars){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i = 0;
        StockKbar end = null;
        StockKbar nextKbar = null;
        BigDecimal highRate = null;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                end = stockKbar;
            }
            if(i>=2&&i<=6){
                BigDecimal rate = PriceUtil.getPricePercentRate(nextKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                if(highRate==null||rate.compareTo(highRate)==1){
                    highRate = rate;
                    dto.setHighRateBeforeDay5(highRate);
                }
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRateDay3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRateDay5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRateDay10(rate);
                return;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
            nextKbar = stockKbar;
        }
    }

    public void  getNextRateInfo(StockKbar buyKbar,HighMarketExplorDTO dto,List<StockKbar> stockKbars){
        boolean flag = false;
        int i = 0;
        BigDecimal highestRate = null;
        BigDecimal lowestRate = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal lowRate = PriceUtil.getPricePercentRate(stockKbar.getAdjLowPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                dto.setNextHighRate(highRate);
                dto.setNextLowRate(lowRate);
                dto.setNextOpenRate(openRate);
                dto.setNextEndRate(endRate);
            }
            if(i>=1 && i<=3){
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal lowRate = PriceUtil.getPricePercentRate(stockKbar.getAdjLowPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                if(highestRate==null||highRate.compareTo(highestRate)==1){
                    highestRate = highRate;
                }
                if(lowestRate==null||lowRate.compareTo(lowestRate)==-1){
                    lowestRate = lowRate;
                }
                dto.setHighDay3(highestRate);
                dto.setLowDay3(lowestRate);
            }
            if(i==3){
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                dto.setEndDay3(endRate);
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public void  getProfitInfo(StockKbar buyKbar,HighMarketExplorDTO dto,List<StockKbar> stockKbars,BigDecimal sellRate,int rateType){
        boolean flag = false;
        int i = 0;
        BigDecimal profit = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i>=1 && i<=3){
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                if(openRate.compareTo(sellRate)>=0){
                    profit = openRate;
                    break;
                }
                if(highRate.compareTo(sellRate)>=0){
                    profit = sellRate;
                    break;
                }
            }
            if(i==3){
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                profit = endRate;
                break;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
        if(profit!=null){
            if(rateType==1){
                dto.setProfit1(profit);
            }else if(rateType == 2){
                dto.setProfit2(profit);
            }else if(rateType == 3){
                dto.setProfit3(profit);
            }else if(rateType == 4){
                dto.setProfit4(profit);
            }else if(rateType == 5){
                dto.setProfit5(profit);
            }else if(rateType ==6){
                dto.setProfit6(profit);
            }else if(rateType == 7){
                dto.setProfit7(profit);
            }else if(rateType == 8){
                dto.setProfit8(profit);
            }else if(rateType ==9){
                dto.setProfit9(profit);
            }else if(rateType == 10){
                dto.setProfit10(profit);
            }else if(rateType == 11){
                dto.setProfit11(profit);
            }
        }
    }




    public boolean getBuyReason(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<3){
            return false;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar preKbar = null;
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            if(preKbar!=null){
                boolean upperFlag = PriceUtil.isHistoryUpperPrice(next.getStockCode(), next.getHighPrice(), preKbar.getClosePrice(), next.getKbarDate());
                BigDecimal endRate = PriceUtil.getPricePercentRate(next.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                if(upperFlag){
                    return false;
                }
                if(endRate.compareTo(new BigDecimal("2"))==-1){
                    return false;
                }
                if(endRate.compareTo(new BigDecimal("5"))==1){
                    return false;
                }
            }
            preKbar = next;
        }
        return true;
    }



    public List<TradeDatePool> getTradeDates(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20220101",DateUtil.yyyyMMdd));
        /*query.setTradeDateTo(DateUtil.parseDate("20220501",DateUtil.yyyyMMdd));*/
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }

    public Map<String,String> getStInfo(){
        Map<String, String> map = new HashMap<>();
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode("885699");
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        for (HistoryBlockStocks stocks:historyBlockStocks){
            map.put(stocks.getTradeDate(),stocks.getStocks());
        }
        return map;
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
        if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<10){
            return 0;
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
