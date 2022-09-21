package com.bazinga.component;

import com.bazinga.base.Sort;
import com.bazinga.dto.BlocKFollowStaticBuyDTO;
import com.bazinga.dto.HighMarketExplorDTO;
import com.bazinga.dto.RealMoneyDTO;
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
public class RealMoneyComponent {
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



    public void realMoneyExplor(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, RealMoneyDTO> map = marketBuy(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for (String tradeDate:map.keySet()) {
            RealMoneyDTO dto = map.get(tradeDate);
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getStockCount());
            list.add(dto.getTotalAmount());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","日期","数量","成交额"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("真钱2",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("真钱2");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public Map<String, RealMoneyDTO> marketBuy(List<CirculateInfo> circulateInfos){
        Map<String, String> stInfoMap = getStInfo();
        Map<String, RealMoneyDTO> map = new HashMap<>();
        int i =0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(circulateInfo.getStockCode()+"-----"+i);
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(circulateInfo.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            LimitQueue<StockKbar> limitQueue10 = new LimitQueue<>(11);
            StockKbar preKbar = null;
            for (StockKbar stockKbar:stockKbars){
                limitQueue10.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20180101", DateUtil.yyyyMMdd))){
                    preKbar = stockKbar;
                    continue;
                }
                if(preKbar!=null) {
                    BigDecimal highMoney = getHighMoneyTwo(limitQueue10);
                    if(highMoney!=null){
                        String stString = stInfoMap.get(stockKbar.getKbarDate());
                        if(!stString.contains(stockKbar.getStockCode())) {
                            RealMoneyDTO realMoneyDTO = map.get(stockKbar.getKbarDate());
                            if(realMoneyDTO==null){
                                realMoneyDTO = new RealMoneyDTO();
                                realMoneyDTO.setTradeDate(stockKbar.getKbarDate());
                                map.put(stockKbar.getKbarDate(),realMoneyDTO);
                            }
                            realMoneyDTO.setStockCount(realMoneyDTO.getStockCount()+1);
                            realMoneyDTO.setTotalAmount(realMoneyDTO.getTotalAmount().add(highMoney));
                        }
                    }
                }
                preKbar = stockKbar;
            }
        }
        return map;
    }

    public BigDecimal getHighMoney(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<11){
            return null;
        }
        int size = limitQueue.size();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        int i = 0;
        StockKbar first = null;
        BigDecimal rate   = null;
        BigDecimal highMoney = null;
        while (iterator.hasNext()){
            i++;
            StockKbar next = iterator.next();
            if(i==1){
                first = next;
            }
            if(i==11){
                rate = PriceUtil.getPricePercentRate(next.getAdjClosePrice().subtract(first.getAdjClosePrice()), first.getAdjClosePrice());
            }
            if(size>=2){
                BigDecimal tradeAmount = next.getTradeAmount();
                if(highMoney==null||tradeAmount.compareTo(highMoney)==1){
                    highMoney = tradeAmount;
                }
            }
        }
        if(rate.compareTo(new BigDecimal("30"))>0){
            return highMoney;
        }
        return null;
    }

    public BigDecimal getHighMoneyTwo(LimitQueue<StockKbar> limitQueue){
        if(limitQueue.size()<11){
            return null;
        }
        int size = limitQueue.size();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        int i = 0;
        StockKbar low = null;
        BigDecimal rate   = null;
        BigDecimal highMoney = null;
        while (iterator.hasNext()){
            i++;
            StockKbar next = iterator.next();
            if(i<11&&i>=2){
                if(low==null||next.getAdjLowPrice().compareTo(low.getAdjLowPrice())==-1){
                    low = next;
                }
            }
            if(i==11){
                rate = PriceUtil.getPricePercentRate(next.getAdjClosePrice().subtract(low.getAdjLowPrice()), low.getAdjLowPrice());
            }
            if(size>=2){
                BigDecimal tradeAmount = next.getTradeAmount();
                if(highMoney==null||tradeAmount.compareTo(highMoney)==1){
                    highMoney = tradeAmount;
                }
            }
        }
        if(rate.compareTo(new BigDecimal("30"))>0){
            return highMoney;
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
