package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.GatherAmountLevelBuyDTO;
import com.bazinga.dto.ReplayFenBanDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CirculateInfoComponent;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.RedisMonior;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.RedisMoniorQuery;
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

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class GatherAmountComponent {
    @Autowired
    private RedisMoniorService redisMoniorService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    public static final ExecutorService THREAD_POOL_QUOTE_GATHER_AMOUNT = ThreadPoolUtils.create(4, 8, 512, "QuoteThreadPool");

    public void gatherAmountBuy(){
        getGatherAmountLevel();


        /*List<GatherAmountLevelBuyDTO> buys = stockGatherAmountDate();
        List<Object[]> datas = Lists.newArrayList();

        for (GatherAmountLevelBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getGatherAmount());
            list.add(dto.getGatherLevel());
            list.add(dto.getBuyAmount());
            list.add(dto.getBuyRate());
            list.add(dto.getMorProfit());
            list.add(dto.getProfitEnd());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","集合成交额","集合成交额排名","买入时成交额","买入时候涨幅","早盘卖出利润","尾盘卖出利润"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("集合排名买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("集合排名买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }*/
    }

    public List<GatherAmountLevelBuyDTO> stockGatherAmountDate(){
        Map<String, List<GatherAmountLevelBuyDTO>> map = new HashMap<>();
        RedisMoniorQuery redisMoniorQuery = new RedisMoniorQuery();
        List<RedisMonior> redisMoniors = redisMoniorService.listByCondition(redisMoniorQuery);
        for (RedisMonior redisMonior:redisMoniors){
            String redisValue = redisMonior.getRedisValue();
            List<GatherAmountLevelBuyDTO> buyDays = JSONObject.parseArray(redisValue, GatherAmountLevelBuyDTO.class);
            for (GatherAmountLevelBuyDTO dto:buyDays){
                List<GatherAmountLevelBuyDTO> stockDtos = map.get(dto.getStockCode());
                if(stockDtos==null){
                    stockDtos = Lists.newArrayList();
                    map.put(dto.getStockCode(),stockDtos);
                }
                stockDtos.add(dto);
            }
        }
        List<GatherAmountLevelBuyDTO> list = Lists.newArrayList();
        int count  = 0;
        for (String stockCode:map.keySet()){
            count++;
            System.out.println(stockCode+"------"+count);
            List<StockKbar> stockKBars = getStockKBars(stockCode);
            if (CollectionUtils.isEmpty(stockKBars)) {
                continue;
            }
            List<GatherAmountLevelBuyDTO> buys = map.get(stockCode);
            for (GatherAmountLevelBuyDTO buy:buys) {
                getBuyRateInfo(buy,stockKBars);
                calMorProfit(buy,stockKBars);
                calEndProfit(buy,stockKBars);
                list.add(buy);
            }
        }
        return list;
    }

    public void getGatherAmountLevel(){
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20210518",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            /*THREAD_POOL_QUOTE_GATHER_AMOUNT.execute(() ->{
                getGatherAmountLevelOneDay(tradeDatePool,circulateInfos);
            });*/
            getGatherAmountLevelOneDay(tradeDatePool,circulateInfos);
        }
        System.out.println("===========进入睡眠时间了=============");
        try {
            Thread.sleep(1000000000l);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void getGatherAmountLevelOneDay(TradeDatePool tradeDatePool,List<CirculateInfo> circulateInfos){
        List<GatherAmountLevelBuyDTO> list = Lists.newArrayList();
        String dateyyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
        System.out.println(dateyyyyMMdd);
        RedisMonior byRedisKey = redisMoniorService.getByRedisKey(dateyyyyMMdd + "_gatherAmount");
        if(byRedisKey!=null){
            return;
        }
        Date start = new Date();
        Map<String, StockKbar> stockKbarMap = getStockKbarMap(dateyyyyMMdd);
        int index = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            index++;
            StockKbar stockKbar = stockKbarMap.get(circulateInfo.getStockCode());
            if(stockKbar==null||stockKbar.getTradeAmount()==null||stockKbar.getTradeAmount().compareTo(new BigDecimal("40000000"))==-1){
                continue;
            }
            //System.out.println(circulateInfo.getStockCode()+"===="+index);
            GatherAmountLevelBuyDTO buyDTO = new GatherAmountLevelBuyDTO();
            buyDTO.setStockCode(circulateInfo.getStockCode());
            buyDTO.setStockName(circulateInfo.getStockName());
            buyDTO.setTradeDate(dateyyyyMMdd);
            buyInfo(circulateInfo.getStockCode(),dateyyyyMMdd,buyDTO);
            if(buyDTO.getGatherAmount().compareTo(BigDecimal.ZERO)==1) {
                list.add(buyDTO);
            }
        }
        long l = new Date().getTime() - start.getTime();
        System.out.println(dateyyyyMMdd+"====="+l);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        List<GatherAmountLevelBuyDTO> alls = GatherAmountLevelBuyDTO.gatherAmountSort(list);
        if(alls.size()>200){
            alls = alls.subList(0,200);
        }
        int i=0;
        for (GatherAmountLevelBuyDTO dto:alls){
            i++;
            dto.setGatherLevel(i);

        }
        RedisMonior redisMonior = new RedisMonior();
        redisMonior.setRedisKey(dateyyyyMMdd+"_gatherAmount");
        redisMonior.setRedisValue(JSONObject.toJSONString(alls));
        redisMonior.setCreateTime(new Date());
        redisMoniorService.save(redisMonior);
    }

    public Map<String,StockKbar> getStockKbarMap(String tradeDate){
        Map<String, StockKbar> map = new HashMap<>();
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDate(tradeDate);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        if(CollectionUtils.isEmpty(stockKbars)){
            return map;
        }
        for (StockKbar kbar:stockKbars){
            map.put(kbar.getStockCode(),kbar);
        }
        return map;
    }

    public void buyInfo(String stockCode,String tradeDate,GatherAmountLevelBuyDTO buyDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        if(CollectionUtils.isEmpty(datas)){
            return;
        }
        Date signDate = DateUtil.parseDate("09:36", DateUtil.HH_MM);
        BigDecimal buyAmount = BigDecimal.ZERO;
        for (ThirdSecondTransactionDataDTO dataDTO:datas){
            Integer tradeQuantity = dataDTO.getTradeQuantity();
            BigDecimal tradePrice = dataDTO.getTradePrice();
            if(tradePrice==null||tradeQuantity==null){
                continue;
            }
            if(dataDTO.getTradeTime().equals("09:25")){
                BigDecimal amount = tradePrice.multiply(new BigDecimal(tradeQuantity * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                buyDTO.setGatherAmount(amount);
            }
            Date dataDate = DateUtil.parseDate(dataDTO.getTradeTime(), DateUtil.HH_MM);
            if(dataDate.before(signDate)){
                dataDTO.setTradePrice(dataDTO.getTradePrice());
                BigDecimal amount = tradePrice.multiply(new BigDecimal(tradeQuantity * 100)).setScale(2, BigDecimal.ROUND_HALF_UP);
                buyAmount = buyAmount.add(amount);
                buyDTO.setBuyAmount(buyAmount);
                buyDTO.setRealBuyPrice(tradePrice);
                buyDTO.setRealBuyDataType(dataDTO.getTradeType());
            }else{
                return;
            }
        }
    }



    public void  getBuyRateInfo(GatherAmountLevelBuyDTO dto, List<StockKbar> stockKbars){
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(stockKbar.getKbarDate().equals(dto.getTradeDate())){
                BigDecimal chuQuanPrice = chuQuanAvgPrice(dto.getRealBuyPrice(), stockKbar);
                if(preStockKbar!=null){
                    BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanPrice.subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    dto.setBuyRate(rate);
                    boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), dto.getRealBuyPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(historyUpperPrice&&dto.getRealBuyDataType()==1){
                        dto.setBuyIsPlank(true);
                    }
                }
                return;
            }
            preStockKbar = stockKbar;
        }
    }

    public void  calMorProfit(GatherAmountLevelBuyDTO dto, List<StockKbar> stockKbars){
        int morSell = 0;
        boolean flag = false;
        StockKbar buyStockKbar = null;
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                boolean highUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                boolean lowUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getLowPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                if(!(highUpperPrice&&lowUpperPrice)){
                   morSell++;
                }
            }
            if(morSell==1){
                BigDecimal avgPriceTen = historyTransactionDataComponent.calTenAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPriceTen, stockKbar);
                BigDecimal buyChuQuanPrice = chuQuanAvgPrice(dto.getRealBuyPrice(), buyStockKbar);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyChuQuanPrice), buyChuQuanPrice);
                dto.setMorProfit(rate);
                return;
            }
            if(stockKbar.getKbarDate().equals(dto.getTradeDate())){
                flag = true;
                buyStockKbar = stockKbar;
            }
            preStockKbar = stockKbar;
        }
    }
    public void  calEndProfit(GatherAmountLevelBuyDTO dto, List<StockKbar> stockKbars){
        int endSell = 0;
        boolean flag = false;
        StockKbar buyStockKbar = null;
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                boolean endUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                if(!endUpperPrice){
                    endSell++;
                }
            }
            if(endSell==1){
               BigDecimal chuQuanAvgPrice = stockKbar.getAdjClosePrice();
                BigDecimal buyChuQuanPrice = chuQuanAvgPrice(dto.getRealBuyPrice(), buyStockKbar);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyChuQuanPrice), buyChuQuanPrice);
                dto.setProfitEnd(rate);
                return;
            }
            if(stockKbar.getKbarDate().equals(dto.getTradeDate())){
                flag = true;
                buyStockKbar = stockKbar;
            }
            preStockKbar = stockKbar;
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

    public String getStockPlankType(PlankTypeDTO plankTypeDTO){
        int currentPlanks = plankTypeDTO.getPlanks();
        int beforePlanks = plankTypeDTO.getBeforePlanks();
        int space = plankTypeDTO.getSpace();
        int breakDays = space-1;
        if(beforePlanks==0){
            breakDays   = 0;
        }
        if(breakDays==0){
          String type = currentPlanks+"连板";
          return type;
        }else{
            int totalPlanks = currentPlanks + beforePlanks;
            int totalDays = currentPlanks + beforePlanks+1;
            String type = totalDays+"天"+totalPlanks+"板";
            return type;
        }
    }


    //判断连板天数
    public PlankTypeDTO continuePlankTypeDto(LimitQueue<StockKbar> limitQueue, CirculateInfo circulateInfo){
        List<StockKbar> kbars = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar kbar = iterator.next();
            kbars.add(kbar);
        }
        PlankTypeDTO plankTypeDTO = new PlankTypeDTO();
        plankTypeDTO.setPlank(false);
        plankTypeDTO.setEndPlank(false);
        if(kbars.size()<2){
            return plankTypeDTO;
        }
        List<StockKbar> reverse = Lists.reverse(kbars);
        StockKbar preStockKbar = null;
        int space = 0;
        int current = 0;
        int before = 0;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(preStockKbar!=null) {
                BigDecimal endPrice = stockKbar.getClosePrice();
                boolean highPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getHighPrice(),endPrice,preStockKbar.getKbarDate());
                boolean endPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getClosePrice(),endPrice,preStockKbar.getKbarDate());
                if(i==2){
                    if(highPlank){
                        /*List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(circulateInfo.getStockCode(),preStockKbar.getKbarDate());
                        int transactionDataPlank = thirdSecondTransactionDataPlank(endPrice, circulateInfo.getStockCode(), list);
                        if(transactionDataPlank==1){
                            current++;
                            plankTypeDTO.setThirdSecondTransactionIsPlank(true);
                        }*/
                        current++;
                        plankTypeDTO.setThirdSecondTransactionIsPlank(true);
                        plankTypeDTO.setEndPlank(endPlank);
                    }
                    if(!plankTypeDTO.isThirdSecondTransactionIsPlank()) {
                        return plankTypeDTO;
                    }
                }
                if(i>2) {
                    if (endPlank) {
                        if (space == 0) {
                            current++;
                        }
                        if (space == 1) {
                            before++;
                        }
                    } else {
                        space++;
                    }
                    if (space >= 2) {
                        break;
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        plankTypeDTO.setPlanks(current);
        plankTypeDTO.setBeforePlanks(before);
        plankTypeDTO.setSpace(space);
        return plankTypeDTO;
    }


    /**
     *
     * @param yesterdayPrice
     * @param stockCode
     * @param list
     * @return 0 未上板 1 上板  -1 没有拿到数据无法判断
     */
    public int thirdSecondTransactionDataPlank(BigDecimal yesterdayPrice,String stockCode,List<ThirdSecondTransactionDataDTO> list){
        try {
            if(CollectionUtils.isEmpty(list)){
                log.error("查询不到当日分时成交数据计算上板时间 stockCode:{}",stockCode);
                return -1;
            }
            for (ThirdSecondTransactionDataDTO dto:list){
                BigDecimal tradePrice = dto.getTradePrice();
                Integer tradeType = dto.getTradeType();
                boolean isUpperPrice = PriceUtil.isUpperPrice(stockCode,tradePrice,yesterdayPrice);
                boolean isSell = false;
                if(tradeType==null || tradeType!=0){
                    isSell = true;
                }
                if(isSell&&isUpperPrice){
                    return 1;
                }
            }
            return 0;
        }catch (Exception e){
            log.error("分时成交统计数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return 0;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(stockCode);
        kbarQuery.setKbarDateFrom("20201101");
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        return stockKbars;
    }


}
