package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlocKFollowStaticBuyDTO;
import com.bazinga.dto.HighMarketExplorDTO;
import com.bazinga.dto.TenDayExplorDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
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
public class TenDayExplorComponent {
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



    public void tenDayExplor(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        List<TenDayExplorDTO> buys = marketBuy(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for (TenDayExplorDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getMarketValue());
            list.add(dto.getEndRate());
            list.add(dto.getLevel());
            list.add(dto.getLowRate());
            list.add(dto.getExchangeRate());
            list.add(dto.getAmountRate());
            list.add(dto.getTenDayMaxRate());
            list.add(dto.getBeforeRate3());
            list.add(dto.getBeforeRate5());
            list.add(dto.getBeforeRate10());
            list.add(dto.getHighDay7());
            list.add(dto.getLowDay7());
            list.add(dto.getEndDay7());
            list.add(dto.getHighDayInt());
            list.add(dto.getLowDayInt());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","市值","买入日收盘涨幅","买入日收盘涨幅排名","买入日最低涨幅","买入日换手","成交金额倍数",
                "10日内最大涨幅","3日涨幅","5日涨幅","10日涨幅","买入后7天最大涨幅","买入后7天最小涨幅","买入后7天后收盘涨幅","买入后7天后最大涨幅日期","买入后7天后最小涨幅日期"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("10日密码",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("10日密码");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public List<TenDayExplorDTO> marketBuy(List<CirculateInfo> circulateInfos){
        Map<String, String> stInfoMap = getStInfo();
        Map<String, List<TenDayExplorDTO>> map = new HashMap<>();
        List<TenDayExplorDTO> buys = Lists.newArrayList();
        int i =0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(circulateInfo.getStockCode()+"-----"+i);
            /*if(map.size()>=10){
                break;
            }*/
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue12 = new LimitQueue<>(12);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue12.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20180101", DateUtil.yyyyMMdd))){
                    preKbar = stockKbar;
                    continue;
                }
                if(!date.before(DateUtil.parseDate("20200101", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(preKbar!=null) {
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                    BigDecimal lowRate = PriceUtil.getPricePercentRate(stockKbar.getAdjLowPrice().subtract(preKbar.getAdjClosePrice()),preKbar.getAdjClosePrice());
                    BigDecimal marketValue = (new BigDecimal(circulateInfo.getCirculate()).multiply(preKbar.getClosePrice())).divide(new BigDecimal("100000000"),2,BigDecimal.ROUND_HALF_UP);
                    BigDecimal amountRate = getBuyReason(limitQueue12, stockKbar);
                    if(amountRate!=null && endRate.compareTo(new BigDecimal("3"))==1&&!highUpper){
                        TenDayExplorDTO buyDTO = new TenDayExplorDTO();
                        buyDTO.setStockCode(circulateInfo.getStockCode());
                        buyDTO.setStockName(circulateInfo.getStockName());
                        buyDTO.setTradeDate(stockKbar.getKbarDate());
                        buyDTO.setMarketValue(marketValue);
                        buyDTO.setEndRate(endRate);
                        buyDTO.setLowRate(lowRate);
                        BigDecimal exchangeRate = new BigDecimal(stockKbar.getTradeQuantity()*100).divide(new BigDecimal(circulateInfo.getCirculate()), 4, BigDecimal.ROUND_HALF_UP);
                        buyDTO.setExchangeRate(exchangeRate);
                        BigDecimal highEndRate = getHighEndRate(limitQueue12);
                        buyDTO.setTenDayMaxRate(highEndRate);
                        buyDTO.setAmountRate(amountRate);
                        getBeforeRateInfo(stockKbar,buyDTO,stockKbars);
                        getNextRateInfo(stockKbar,buyDTO,stockKbars);
                        List<TenDayExplorDTO> dtos = map.get(buyDTO.getTradeDate());
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
            List<TenDayExplorDTO> dtos = map.get(tradeDate);
            List<TenDayExplorDTO> sortDtos = TenDayExplorDTO.endRateSort(dtos);
            List<TenDayExplorDTO> reverse = Lists.reverse(sortDtos);
            int level = 0;
            for (TenDayExplorDTO rev:reverse){
                level++;
                rev.setLevel(level);
            }
            buys.addAll(reverse);
        }
        return buys;
    }

    public void  getBeforeRateInfo(StockKbar buyKbar,TenDayExplorDTO dto,List<StockKbar> stockKbars){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i = 0;
        StockKbar end = null;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==1){
                end = stockKbar;
            }
            if(i==4){
                BigDecimal rate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRate3(rate);
            }
            if(i==6){
                BigDecimal rate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRate5(rate);
            }
            if(i==11){
                BigDecimal rate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRate10(rate);
                return;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
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

    public void  getNextRateInfo(StockKbar buyKbar,TenDayExplorDTO dto,List<StockKbar> stockKbars){
        boolean flag = false;
        int i = 0;
        BigDecimal highestRate = null;
        BigDecimal lowestRate = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i>=1 && i<=7){
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal lowRate = PriceUtil.getPricePercentRate(stockKbar.getAdjLowPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                if(highestRate==null||highRate.compareTo(highestRate)==1){
                    highestRate = highRate;
                    dto.setHighDayInt(i);
                }
                if(lowestRate==null||lowRate.compareTo(lowestRate)==-1){
                    lowestRate = lowRate;
                    dto.setLowDayInt(i);
                }
                dto.setHighDay7(highestRate);
                dto.setLowDay7(lowestRate);
            }
            if(i==7){
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                dto.setEndDay7(endRate);
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




    public BigDecimal getBuyReason(LimitQueue<StockKbar> limitQueue, StockKbar stockKbar){
        if(limitQueue.size()<12){
            return null;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar preKbar = null;
        int i = 0;
        BigDecimal totalMoney = BigDecimal.ZERO;
        while (iterator.hasNext()){
            i++;
            StockKbar next = iterator.next();
            if(preKbar!=null){
                boolean upperFlag = PriceUtil.isHistoryUpperPrice(next.getStockCode(), next.getHighPrice(), preKbar.getClosePrice(), next.getKbarDate());
                if(upperFlag){
                    return null;
                }
                if(i<=11){
                    totalMoney = totalMoney.add(next.getTradeAmount());
                    if(next.getAdjHighPrice().compareTo(stockKbar.getAdjClosePrice())==1){
                        return null;
                    }
                    if(next.getTradeAmount().compareTo(stockKbar.getTradeAmount())==1){
                        return null;
                    }
                }

            }
            preKbar = next;
        }
        BigDecimal avgMoney = totalMoney.divide(new BigDecimal(10), 2, BigDecimal.ROUND_HALF_UP);
        BigDecimal amountRate = stockKbar.getTradeAmount().divide(avgMoney, 3, BigDecimal.ROUND_HALF_UP);
        if(amountRate.compareTo(new BigDecimal("1.5"))==1){
            return amountRate;
        }
        return null;
    }

    public BigDecimal getHighEndRate(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<12){
            return null;
        }
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar preKbar = null;
        int i = 0;
        BigDecimal highEndRate = null;
        while (iterator.hasNext()){
            i++;
            StockKbar next = iterator.next();
            if(preKbar!=null){
                if(i<=11){
                    BigDecimal rate = PriceUtil.getPricePercentRate(next.getAdjClosePrice().subtract(preKbar.getAdjClosePrice()), preKbar.getAdjClosePrice());
                    if(highEndRate==null||rate.compareTo(highEndRate)==1){
                        highEndRate = rate;
                    }
                }

            }
            preKbar = next;
        }
        return highEndRate;
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
