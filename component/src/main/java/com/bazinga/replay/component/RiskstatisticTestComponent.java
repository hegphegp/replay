package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.RistStatisticTestDTO;
import com.bazinga.replay.dto.StrongerStockTestDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.RiskStatisticQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import lombok.extern.slf4j.Slf4j;
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
public class RiskstatisticTestComponent {
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
    @Autowired
    private RedisMoniorService redisMoniorService;
    @Autowired
    private ThsCirculateInfoComponent thsCirculateInfoComponent;
    @Autowired
    private RiskStatisticService riskStatisticService;
    @Autowired
    private IndexDetailComponent indexDetailComponent;
    @Autowired
    private StockKbarService stockKbarService;
    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();
    public static final ExecutorService THREAD_POOL_QUOTE_FACTOR = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");

    public static String leveStockCode = "600896,600555,600385,600090,300202,000673,300312,600870,300038,300367,600146,000613,300023,002464,300325,000611,000502,002447,002684,300064,002147,002770,600093,600275,002473,600209,600856,300178,002618,600652,600890,600091,002260,000687,600291,600695,603157,000585,603996,000835,600145,002619,000780,600723,600068,300362,002359,000760,600634,600614,002711,600485,002450,600891,002071,600701,000662,600247,600978,600677,600086,600687,600317";
    //public static String leveStockCode = "1111";


    public void riskStatisticTest(){
        List<RistStatisticTestDTO> buys = getRiskCom();
        List<Object[]> datas = Lists.newArrayList();
        for (RistStatisticTestDTO dto:buys) {
            List<Object> list = new ArrayList<>();

            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getV1real());
            list.add(dto.getV2());
            list.add(dto.getV3());
            list.add(dto.getV1RealSuccessRate());
            list.add(dto.getV2SuccessRate());
            list.add(dto.getV3SuccessRate());
            list.add(dto.getResult());
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","交易日期","V1Real","V2","V3","V1Real成功率","V2成功率","V3成功率","实际买入方向","实际盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("多策略回归",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("多策略回归");
        }catch (Exception e){
            log.info(e.getMessage());
        }

    }


    public List<RistStatisticTestDTO> getRiskCom(){
        List<RistStatisticTestDTO> list = Lists.newArrayList();
        Map<String, BigDecimal> huShenRateMap = getIfHuShenProfit();
        List<TradeDatePool> tradeDatePools = getTradeDatePools();
        int size = 7;
        LimitQueue<RiskStatistic> limitQueuev1 = new LimitQueue<>(size);
        LimitQueue<RiskStatistic> limitQueuev2 = new LimitQueue<>(size);
        LimitQueue<RiskStatistic> limitQueuev3 = new LimitQueue<>(size);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            RiskStatistic riskStatisticV1 = getRiskStatistic(yyyyMMdd, 1);
            RiskStatistic riskStatisticV2 = getRiskStatistic(yyyyMMdd, 2);
            RiskStatistic riskStatisticV3 = getRiskStatistic(yyyyMMdd, 3);
            if(riskStatisticV1==null||riskStatisticV2==null||riskStatisticV3==null){
                continue;
            }
            limitQueuev1.offer(riskStatisticV1);
            limitQueuev2.offer(riskStatisticV2);
            limitQueuev3.offer(riskStatisticV3);
            BigDecimal successRateV1 = calSuccessRate(limitQueuev1, true, size,new BigDecimal(61.5), huShenRateMap);
            BigDecimal successRateV2 = calSuccessRate(limitQueuev2, false, size,new BigDecimal(57.33), huShenRateMap);
            BigDecimal successRateV3 = calSuccessRate(limitQueuev3, false, size, new BigDecimal(57.50),huShenRateMap);
            RistStatisticTestDTO dto = new RistStatisticTestDTO();
            dto.setTradeDate(yyyyMMdd);
            dto.setV1real(riskStatisticV1.getRealRiskType());
            dto.setV2(riskStatisticV2.getCalRiskType());
            dto.setV3(riskStatisticV3.getCalRiskType());
            dto.setV1RealSuccessRate(successRateV1);
            dto.setV2SuccessRate(successRateV2);
            dto.setV3SuccessRate(successRateV3);
            getResultSelf(dto,huShenRateMap,new BigDecimal(-30));
            list.add(dto);
        }
        return list;

    }

    public void getResultSelf(RistStatisticTestDTO dto,Map<String,BigDecimal> hsRateMap,BigDecimal dropRate ){
        if(dto.getV1RealSuccessRate()==null||dto.getV2SuccessRate()==null||dto.getV3SuccessRate()==null){
            return;
        }
        Integer type = dto.getV1real();
        BigDecimal low  = dto.getV1RealSuccessRate();
        if(dto.getV2SuccessRate().compareTo(low)==-1){
            low = dto.getV2SuccessRate();
            type = dto.getV2();
        }
        if(dto.getV3SuccessRate().compareTo(low)==-1){
            low = dto.getV3SuccessRate();
            type = dto.getV3();
        }
        int buyType = dto.getV2();
        if(low.compareTo(dropRate)<=0){
            buyType = type;
        }
        if(buyType==0){
            buyType = -1;
        }
        BigDecimal hsRate = hsRateMap.get(dto.getTradeDate());
        if(hsRate==null){
            return;
        }
        BigDecimal multiply = hsRate.multiply(new BigDecimal(buyType));
        dto.setResult(buyType);
        dto.setProfit(multiply);
    }

    public void getResult(RistStatisticTestDTO dto,Map<String,BigDecimal> hsRateMap ){
        if(dto.getV1RealSuccessRate()==null||dto.getV2SuccessRate()==null||dto.getV3SuccessRate()==null){
            return;
        }
        Integer type = dto.getV1real();
        BigDecimal high  = dto.getV1RealSuccessRate();
        if(dto.getV2SuccessRate().compareTo(high)==1){
            high = dto.getV2SuccessRate();
        }
        if(dto.getV3SuccessRate().compareTo(high)==1){
            high = dto.getV3SuccessRate();
        }

        BigDecimal low  = dto.getV1RealSuccessRate();
        if(dto.getV2SuccessRate().compareTo(low)==-1){
            low = dto.getV2SuccessRate();
            type = dto.getV2();
        }
        if(dto.getV3SuccessRate().compareTo(low)==-1){
            low = dto.getV3SuccessRate();
            type = dto.getV3();
        }
        int buyType = dto.getV2();
        BigDecimal subtract = high.subtract(low);
        if(subtract.compareTo(new BigDecimal(30))>=0){
            buyType = type;
        }
        if(buyType==0){
            buyType = -1;
        }
        BigDecimal hsRate = hsRateMap.get(dto.getTradeDate());
        if(hsRate==null){
            return;
        }
        BigDecimal multiply = hsRate.multiply(new BigDecimal(buyType));
        dto.setResult(buyType);
        dto.setProfit(multiply);
    }


    public BigDecimal calSuccessRate(LimitQueue<RiskStatistic> limitQueue,boolean realFlag,int size,BigDecimal avgRate,Map<String,BigDecimal> hsRateMap){
        if(limitQueue.size()<size){
            return null;
        }
        List<RiskStatistic> riskStatistics = limitQueueToList(limitQueue);
        List<RiskStatistic> uses = riskStatistics.subList(0, size-2);
        int successCount = 0;
        int defeatCount = 0;
        for (RiskStatistic riskStatistic:uses){
            int type = 1;
            if(realFlag){
                type = riskStatistic.getRealRiskType();
            }else{
                type = riskStatistic.getCalRiskType();
            }
            if(type==0){
                type = -1;
            }
            BigDecimal hsRate = hsRateMap.get(riskStatistic.getTradeDate());
            BigDecimal profit = hsRate.multiply(new BigDecimal(type));
            if(profit.compareTo(new BigDecimal(0))==1){
                successCount++;
            }else{
                defeatCount++;
            }
        }
        if(successCount+defeatCount>0){
            BigDecimal divide = new BigDecimal(successCount).divide(new BigDecimal(successCount + defeatCount), 4, BigDecimal.ROUND_HALF_UP).
                    multiply(new BigDecimal(100)).setScale(2,BigDecimal.ROUND_HALF_UP);
            BigDecimal subtract = divide.subtract(avgRate);
            return subtract;
        }
        return null;

    }

    public Map<String,BigDecimal> getIfHuShenProfit(){
        Map<String, BigDecimal> map = new HashMap<>();
        StockKbarQuery query = new StockKbarQuery();
        query.setStockCode("IFZL");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(2);
        for (StockKbar stockKbar:stockKbars){
            limitQueue.offer(stockKbar);
            calRate(limitQueue,map);
        }
        return map;
    }

    public RiskStatistic getRiskStatistic(String tradeDate,int strategyType){
        RiskStatisticQuery query = new RiskStatisticQuery();
        query.setTradeDate(tradeDate);
        query.setStrategyType(strategyType);
        List<RiskStatistic> riskStatistics = riskStatisticService.listByCondition(query);
        if(CollectionUtils.isEmpty(riskStatistics)){
            return null;
        }
        return riskStatistics.get(0);
    }

    public void calRate(LimitQueue<StockKbar> limitQueue,Map<String,BigDecimal> rateMap){
       if(limitQueue.size()<2){
           return;
       }
       StockKbar first = null;
       StockKbar last = null;
       Iterator<StockKbar> iterator = limitQueue.iterator();
       int i = 0;
       while (iterator.hasNext()){
           i++;
           StockKbar stockKbar = iterator.next();
           if(i==1){
               first = stockKbar;
           }
           if(i==2){
               last = stockKbar;
           }
       }
       BigDecimal rate = PriceUtil.getPricePercentRate(last.getClosePrice().subtract(first.getOpenPrice()), first.getOpenPrice());
       rateMap.put(first.getKbarDate(),rate);
    }

    public List<TradeDatePool> getTradeDatePools(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        query.setTradeDateTo(DateUtil.parseDate("20230224",DateUtil.yyyyMMdd));
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }

    public List<RiskStatistic> limitQueueToList(LimitQueue<RiskStatistic> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        List<RiskStatistic> list = Lists.newArrayList();
        Iterator<RiskStatistic> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            RiskStatistic next = iterator.next();
            list.add(next);
        }
        return list;
    }





}
