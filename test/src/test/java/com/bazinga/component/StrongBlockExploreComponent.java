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
import jnr.ffi.annotations.In;
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
public class StrongBlockExploreComponent {
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



    public void highLowPlank(){
        Map<String, String> nextTradeDateMap = getNextTradeDateMap();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String,List<SBEHighTimeDTO>> highMapInfo = new HashMap<>();
        Map<String, List<String>> map = getEndPlanks(circulateInfos,highMapInfo,nextTradeDateMap);
        List<StrongBlockBuyDTO> buys = blockPlankRate(map,highMapInfo);
        List<Object[]> datas = Lists.newArrayList();
        for (StrongBlockBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getBlockCode());
            list.add(dto.getBlockName());
            list.add(dto.getStockCount());

            list.add(dto.getBlockCount1());
            list.add(dto.getMarketCount1());
            list.add(dto.getRate1());


            list.add(dto.getBlockCount2());
            list.add(dto.getMarketCount2());
            list.add(dto.getRate2());

            list.add(dto.getBlockCount3());
            list.add(dto.getMarketCount3());
            list.add(dto.getRate3());

            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getPlankTime());
            list.add(dto.getPlanks());
            list.add(dto.getProfit());

            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","日期","板块代码","板块名称","股票数","板块封住1","市场封住1","比例1","板块封住2","市场封住2","比例2","板块封住3","市场封住3","比例3","股票名称","股票代码","上板时间","买入前一日板高","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("热点板块",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("热点板块");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<StrongBlockBuyDTO> blockPlankRate(Map<String,List<String>> map,Map<String,List<SBEHighTimeDTO>> dtoMap){
        List<StrongBlockBuyDTO> buys = Lists.newArrayList();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        Map<String, StrongPercentDTO> strongMap = new HashMap<>();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            List<String> planks = map.get(tradeDate);
            List<SBEHighTimeDTO> sbeHighTimeDTOS = dtoMap.get(tradeDate);
            getBlockPlankPercent(tradeDate, planks,strongMap,sbeHighTimeDTOS);
        }
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        LimitQueue<String> limitQueue = new LimitQueue<>(2);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String tradeDate = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            limitQueue.offer(tradeDate);
            if(limitQueue.size()<2){
                continue;
            }
            List<String> tradeDates = Lists.newArrayList();
            Iterator<String> iterator = limitQueue.iterator();
            while (iterator.hasNext()){
                tradeDates.add(iterator.next());
            }
            for (HistoryBlockInfo blockInfo:historyBlockInfos){
                StrongPercentDTO strongPercentDTO1 = strongMap.get(blockInfo.getBlockCode() + "_" + tradeDates.get(0));
                StrongPercentDTO strongPercentDTO2 = strongMap.get(blockInfo.getBlockCode() + "_" + tradeDates.get(1));
                //StrongPercentDTO strongPercentDTO3 = strongMap.get(blockInfo.getBlockCode() + "_" + tradeDates.get(2));
                if(strongPercentDTO1!=null&&strongPercentDTO2!=null){
                    StrongBlockBuyDTO strongBlockBuyDTO = new StrongBlockBuyDTO();
                    strongBlockBuyDTO.setBlockCode(blockInfo.getBlockCode());
                    strongBlockBuyDTO.setBlockName(blockInfo.getBlockName());
                    strongBlockBuyDTO.setTradeDate(tradeDate);
                    strongBlockBuyDTO.setMarketCount1(strongPercentDTO1.getMarketCount());
                    strongBlockBuyDTO.setMarketCount2(strongPercentDTO2.getMarketCount());
                    //strongBlockBuyDTO.setMarketCount3(strongPercentDTO3.getMarketCount());
                    strongBlockBuyDTO.setBlockCount1(strongPercentDTO1.getBlockCount());
                    strongBlockBuyDTO.setBlockCount2(strongPercentDTO2.getBlockCount());
                    //strongBlockBuyDTO.setBlockCount3(strongPercentDTO3.getBlockCount());
                    strongBlockBuyDTO.setRate1(strongPercentDTO1.getRate());
                    strongBlockBuyDTO.setRate2(strongPercentDTO2.getRate());
                    //strongBlockBuyDTO.setRate3(strongPercentDTO3.getRate());
                    List<String> stocks = getBlockStocks(blockInfo.getBlockCode(), tradeDate);
                    strongBlockBuyDTO.setStockCount(stocks.size());
                    if(strongPercentDTO2.getBuyDTO()!=null){
                        SBEHighTimeDTO buyDTO = strongPercentDTO2.getBuyDTO();
                        strongBlockBuyDTO.setStockCode(buyDTO.getStockCode());
                        strongBlockBuyDTO.setStockName(buyDTO.getStockName());
                        strongBlockBuyDTO.setPlanks(buyDTO.getPlanks());
                        strongBlockBuyDTO.setPlankTime(buyDTO.getPlankTime());
                        strongBlockBuyDTO.setProfit(buyDTO.getProfit());
                    }
                    buys.add(strongBlockBuyDTO);
                }
            }
        }
        return buys;
    }

    public void getBlockPlankPercent(String tradeDate,List<String> planks,Map<String,StrongPercentDTO> strongMap,List<SBEHighTimeDTO> sbeHighTimeDTOS){
        HistoryBlockStocksQuery stocksQuery = new HistoryBlockStocksQuery();
        stocksQuery.setTradeDate(tradeDate);
        List<HistoryBlockStocks> stocks = historyBlockStocksService.listByCondition(stocksQuery);
        if(CollectionUtils.isEmpty(stocks)||CollectionUtils.isEmpty(planks)){
            return;
        }
        Map<String, SBEHighTimeDTO> dtoMap = new HashMap<>();
        if(!CollectionUtils.isEmpty(sbeHighTimeDTOS)) {
            for (SBEHighTimeDTO timeDTO : sbeHighTimeDTOS) {
                dtoMap.put(timeDTO.getStockCode(), timeDTO);
            }
        }
        for (HistoryBlockStocks historyBlockStocks:stocks){
            String stockStr = historyBlockStocks.getStocks();
            if(StringUtils.isBlank(stockStr)){
                continue;
            }
            int count = 0;
            SBEHighTimeDTO sbeDTO = null;
            for (String stockCode:planks){
                if(stockStr.contains(stockCode)){
                    count++;
                    SBEHighTimeDTO timeDTO = dtoMap.get(stockCode);
                    if(timeDTO!=null) {
                        if (sbeDTO == null) {
                            sbeDTO = timeDTO;
                        }else{
                            if(timeDTO.getPlanks()>sbeDTO.getPlanks()){
                                sbeDTO = timeDTO;
                            } else if(timeDTO.getPlanks()==sbeDTO.getPlanks()){
                                if(sbeDTO.getPlankTime()==null){
                                    sbeDTO = timeDTO;
                                }
                                if(timeDTO.getPlankTime()!=null&&timeDTO.getPlankTime()< sbeDTO.getPlankTime()){
                                    sbeDTO = timeDTO;
                                }
                            }
                        }
                    }
                }
            }
            BigDecimal percent = new BigDecimal(count).divide(new BigDecimal(planks.size()), 2, BigDecimal.ROUND_HALF_UP);
            if(percent.compareTo(new BigDecimal("0.1"))==1){
                StrongPercentDTO strongPercentDTO = new StrongPercentDTO();
                strongPercentDTO.setBlockCount(count);
                strongPercentDTO.setMarketCount(planks.size());
                strongPercentDTO.setRate(percent);
                strongPercentDTO.setBuyDTO(sbeDTO);
                strongMap.put(historyBlockStocks.getBlockCode()+"_"+historyBlockStocks.getTradeDate(),strongPercentDTO);
            }
        }
    }


    public Map<String, List<String>> getEndPlanks(List<CirculateInfo> circulateInfos,Map<String,List<SBEHighTimeDTO>> highMapInfo,Map<String,String> nextTradeDateMap){
        Map<String, List<String>> map = new HashMap<>();
        int i =0;
        for (CirculateInfo circulateInfo:circulateInfos){
            i++;
            System.out.println(circulateInfo.getStockCode()+"-----"+i);
           /* if(buys.size()>=10){
                return;
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
                if(date.before(DateUtil.parseDate("20180101", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(date.after(DateUtil.parseDate("20220701", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(preKbar!=null) {
                    boolean endUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(endUpper){
                        int planks = calPlanks(limitQueue);
                        List<String> lists = map.get(stockKbar.getKbarDate());
                        if(lists==null){
                            lists = Lists.newArrayList();
                            map.put(stockKbar.getKbarDate(),lists);
                        }
                        lists.add(stockKbar.getStockCode());
                        if(planks>=2){
                            List<SBEHighTimeDTO> sbeHighTimeDTOS = highMapInfo.get(stockKbar.getKbarDate());
                            if(sbeHighTimeDTOS==null){
                                sbeHighTimeDTOS = Lists.newArrayList();
                                highMapInfo.put(stockKbar.getKbarDate(),sbeHighTimeDTOS);
                            }
                            SBEHighTimeDTO sbeHighTimeDTO = new SBEHighTimeDTO();
                            sbeHighTimeDTO.setStockCode(circulateInfo.getStockCode());
                            sbeHighTimeDTO.setStockName(circulateInfo.getStockName());
                            sbeHighTimeDTO.setPlanks(planks);
                            getHighPlankInfo(nextTradeDateMap.get(stockKbar.getKbarDate()),stockKbars,sbeHighTimeDTO);
                            sbeHighTimeDTOS.add(sbeHighTimeDTO);
                        }
                    }

                }
                preKbar = stockKbar;
            }
        }
        return map;
    }
    public void getHighPlankInfo(String nextTradeDate,List<StockKbar> stockKbars,SBEHighTimeDTO sbeHighTimeDTO){
        int i = 0;
        StockKbar preKbar = null;
        StockKbar buyStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(i>0){
                if(sbeHighTimeDTO.getPlankTime()==null){
                    return;
                }
                boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(),stockKbar.getKbarDate());
                if(!historyUpperPrice) {
                    historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getAdjClosePrice(), preKbar.getAdjClosePrice(), stockKbar.getKbarDate());
                }

                if(!historyUpperPrice){
                    BigDecimal avgPrice = stockKbar.getAdjClosePrice();
                    BigDecimal profit = PriceUtil.getPricePercentRate(avgPrice.subtract(buyStockKbar.getAdjHighPrice()), buyStockKbar.getAdjHighPrice());
                    sbeHighTimeDTO.setProfit(profit);
                    return;
                }
            }
            if(stockKbar.getKbarDate().equals(nextTradeDate)){
                buyStockKbar = stockKbar;
                if(stockKbar.getHighPrice().compareTo(stockKbar.getLowPrice())==0){
                    return;
                }
                Long stockBuyTime = getStockBuyTime(stockKbar,preKbar.getClosePrice());
                sbeHighTimeDTO.setPlankTime(stockBuyTime);
                i++;
            }
            preKbar = stockKbar;
        }
    }


    public Long getStockBuyTime(StockKbar stockKbar,BigDecimal preEndPrice){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return null;
        }
        int index = 0;
        String preMin = "09:25";
        boolean preIsUpper = true;
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
                preIsUpper = true;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            if(upperPrice&&tradeType==1){
                if(!preIsUpper){
                    long buyTime = timeToLong(data.getTradeTime(), index);
                    return buyTime;
                }
                preIsUpper = true;
            }else{
                preIsUpper = false;
            }
        }
        return null;
    }

    public Map<String,String> getNextTradeDateMap(){
        Map<String, String> map = new HashMap<>();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        String preTradeDate = null;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            if(preTradeDate!=null){
                map.put(preTradeDate,format);
            }
            preTradeDate = format;
        }
        return map;
    }

    public Map<String,HistoryBlockInfo> getHistoryBlockInfo(){
        Map<String, HistoryBlockInfo> map = new HashMap<>();
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            map.put(blockInfo.getBlockCode(),blockInfo);
        }
        return map;
    }

    public BlocKFollowStaticTotalDTO blockBuys(List<HistoryBlockInfo> blockInfos,List<PlankTimePairDTO> pairs,Map<String, CirculateInfo> circulateInfoMap,String tradeDate){
        Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        Date nextTradeDate = commonComponent.afterTradeDate(DateUtil.parseDate(tradeDate, DateUtil.yyyyMMdd));
        List<StockKbar> stockKbars = getStockKbarsByKBarDate(tradeDate);
        List<StockKbar> nextStockKbars = getStockKbarsByKBarDate(DateUtil.format(nextTradeDate,DateUtil.yyyyMMdd));
        List<StockKbar> preStockKbars = getStockKbarsByKBarDate(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(stockKbars)||CollectionUtils.isEmpty(preStockKbars)||CollectionUtils.isEmpty(nextStockKbars)){
            return null;
        }
        Map<String, StockKbar> stockMap = new HashMap<>();
        Map<String, StockKbar> nestStockMap = new HashMap<>();
        Map<String, StockKbar> preStockMap = new HashMap<>();
        for (StockKbar stockKbar:stockKbars){
            stockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:nextStockKbars){
            nestStockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        for (StockKbar stockKbar:preStockKbars){
            preStockMap.put(stockKbar.getStockCode(),stockKbar);
        }
        List<BlocKFollowStaticBuyDTO> buyStocks = Lists.newArrayList();
        int planksBlockCount =0;
        int allBuyCount = 0;
        int oneBuyCount = 0;
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
            if(blockPairs.size()>0){
                planksBlockCount++;
            }
            if(blockPairs.size()<3){
                continue;
            }
            Map<String, List<MarketMoneyDTO>> threeBuyLevelStocks = getThreeBuyLevelStocks(stocks, circulateInfoMap, stockMap);
            List<MarketMoneyDTO> firsts = threeBuyLevelStocks.get("first");
            if(firsts==null||firsts.size()<=0){
                continue;
            }
            List<PlankTimePairDTO> plankTens = judgePlanks100000(blockPairs);
            Long threePlankTime = judgeThreePlanks(blockPairs);
            Long threeFirstPlankTime = judgeThreeFirstPlanks(blockPairs);
            if(plankTens.size()>=3){
                for (MarketMoneyDTO first:firsts){
                    BlocKFollowStaticBuyDTO buyDTO = new BlocKFollowStaticBuyDTO();
                    buyDTO.setStockCode(first.getStockCode());
                    buyDTO.setStockName(first.getStockName());
                    buyDTO.setBlockCode(blockInfo.getBlockCode());
                    buyStocks.add(buyDTO);
                }
            }
            boolean flagFristThreePlank = false;
            if(threeFirstPlankTime!=null&&threeFirstPlankTime<100000l){
                flagFristThreePlank = true;
            }
            boolean flagThreePlank = false;
            if(threePlankTime!=null&&threePlankTime<100000l){
                flagThreePlank = true;
            }

            if(plankTens.size()>=3&&flagThreePlank&&flagFristThreePlank){
                allBuyCount++;
            }
            if(plankTens.size()>=3||flagThreePlank||flagFristThreePlank){
                oneBuyCount++;
            }
        }
        List<BlocKFollowStaticBuyDTO> buys = getBuysProfit(buyStocks, "100000", stockMap, nestStockMap, preStockMap);
        if(CollectionUtils.isEmpty(buys)){
            return null;
        }
        List<BlocKFollowStaticBuyDTO> buysAmountSorts = BlocKFollowStaticBuyDTO.amountRateSort(buys);
       /* List<BlocKFollowStaticBuyDTO> buysAmountSorts = buys.stream().sorted(Comparator.comparing(BlocKFollowStaticBuyDTO::getAmountRate)).collect(Collectors.toList());
        List<BlocKFollowStaticBuyDTO> reverses = Lists.reverse(buysAmountSorts);*/
        List<BlocKFollowStaticBuyDTO> amountRateBuys = Lists.newArrayList();
        amountRateBuys.addAll(buysAmountSorts);
        if(amountRateBuys.size()>150){
            amountRateBuys = amountRateBuys.subList(0,150);
        }
        List<BlocKFollowStaticBuyDTO> buysRateSorts = BlocKFollowStaticBuyDTO.rateSort(buys);
        /*List<BlocKFollowStaticBuyDTO> buysRateSorts = buys.stream().sorted(Comparator.comparing(BlocKFollowStaticBuyDTO::getRate)).collect(Collectors.toList());
        List<BlocKFollowStaticBuyDTO> rateReverse = Lists.reverse(buysRateSorts);*/
        List<BlocKFollowStaticBuyDTO> rateBuys = Lists.newArrayList();
        rateBuys.addAll(buysRateSorts);
        if(rateBuys.size()>150){
            rateBuys = rateBuys.subList(0,150);
        }
        calTenSellAndEndSell(rateBuys,tradeDate);
        List<PlankTimePairDTO> planksTen = judgePlanks100000(pairs);
        BlocKFollowStaticTotalDTO staticDTO = new BlocKFollowStaticTotalDTO();
        staticDTO.setTradeDate(tradeDate);
        staticDTO.setPlanksTenCount(planksTen.size());
        staticDTO.setPlankBlockCount(planksBlockCount);
        staticDTO.setAllBuyCount(allBuyCount);
        staticDTO.setOneBuyCount(oneBuyCount);
        calTotalInfo(buys,staticDTO,1);
        calTotalInfo(amountRateBuys,staticDTO,2);
        calTotalInfo(rateBuys,staticDTO,3);
        return staticDTO;
    }

    public void calTenSellAndEndSell(List<BlocKFollowStaticBuyDTO> dtos,String tradeDate){
        for (BlocKFollowStaticBuyDTO dto:dtos){
            if(dto.getChuQuanBuyPrice()==null){
                continue;
            }
            List<StockKbar> stockKbars = getStockKBarsDelete30Days(dto.getStockCode());
            if(CollectionUtils.isEmpty(stockKbars)){
                continue;
            }
            StockKbar preStockKbar = null;
            boolean flag = false;
            for (StockKbar stockKbar:stockKbars){
                if(flag){
                    boolean endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(!endUpper){
                        BigDecimal profitEnd = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(dto.getChuQuanBuyPrice()), dto.getChuQuanBuyPrice());
                        dto.setProfitEnd(profitEnd);
                        break;
                    }
                }
                if(stockKbar.getKbarDate().equals(tradeDate)){
                    flag = true;
                }
                preStockKbar = stockKbar;
            }


            boolean flagTen = false;
            int days = 0;
            for (StockKbar stockKbar:stockKbars){
                if(flagTen){
                    if(!stockKbar.getHighPrice().equals(stockKbar.getLowPrice())) {
                        days++;
                    }
                }
                if(days==1){
                    BigDecimal tenAvgPrice = historyTransactionDataComponent.calTenAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                    BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(tenAvgPrice, stockKbar);
                    BigDecimal profitTen = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(dto.getChuQuanBuyPrice()), dto.getChuQuanBuyPrice());
                    dto.setProfitTen(profitTen);
                    break;
                }
                if(stockKbar.getKbarDate().equals(tradeDate)){
                    flagTen = true;
                }
            }
        }
    }


    public void calTotalInfo(List<BlocKFollowStaticBuyDTO> buys, BlocKFollowStaticTotalDTO staticDTO,Integer type){
        List<String> blockCodes = Lists.newArrayList();
        List<BlocKFollowStaticBuyDTO> noSames = Lists.newArrayList();
        BigDecimal totalProfit = BigDecimal.ZERO;
        for (BlocKFollowStaticBuyDTO buy:buys){
            if(!blockCodes.contains(buy.getBlockCode())){
                blockCodes.add(buy.getBlockCode());
            }
            totalProfit = totalProfit.add(buy.getProfit());
            boolean flag = true;
            for (BlocKFollowStaticBuyDTO noSame:noSames){
                if(noSame.getStockCode().equals(buy.getStockCode())){
                    flag = false;
                }
            }
            if(flag){
                noSames.add(buy);
            }
        }
        BigDecimal noSameTotalProfit = BigDecimal.ZERO;
        for (BlocKFollowStaticBuyDTO noSame:noSames){
            noSameTotalProfit = noSameTotalProfit.add(noSame.getProfit());
        }
        if(type==1){
            staticDTO.setBuyBlocks(blockCodes.size());
            staticDTO.setBuys(buys.size());
            staticDTO.setProfit(totalProfit);
            staticDTO.setNoSameBuys(noSames.size());
            staticDTO.setNoSameProfit(noSameTotalProfit);
        }
        if(type==2){
            staticDTO.setAmountBuys(buys.size());
            staticDTO.setAmountProfit(totalProfit);
            staticDTO.setNoSameAmountBuys(noSames.size());
            staticDTO.setNoSameAmountProfit(noSameTotalProfit);
        }
        if(type==3){
            staticDTO.setRateBuys(buys.size());
            staticDTO.setRateProfit(totalProfit);
            staticDTO.setNoSameRateBuys(noSames.size());
            staticDTO.setNoSameRateProfit(noSameTotalProfit);

            BigDecimal noSameTotalRateTen = BigDecimal.ZERO;
            BigDecimal noSameTotalRateEnd = BigDecimal.ZERO;
            for (BlocKFollowStaticBuyDTO noSame:noSames){
                if(noSame.getProfitTen()!=null) {
                    noSameTotalRateTen = noSameTotalRateTen.add(noSame.getProfitTen());
                }
                if(noSame.getProfitEnd()!=null) {
                    noSameTotalRateEnd = noSameTotalRateEnd.add(noSame.getProfitEnd());
                }
            }
            staticDTO.setNoSameRateProfitEnd(noSameTotalRateEnd);
            staticDTO.setNoSameRateProfitTen(noSameTotalRateTen);
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

    public List<PlankTimePairDTO> judgePlanks100000(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        if(CollectionUtils.isEmpty(pairs)){
            return planksTen;
        }
        for (PlankTimePairDTO pair:pairs){
            if(pair.getStart()<=100000&&(pair.getEnd()==null||pair.getEnd()>100000)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }
    public List<PlankTimePairDTO> judgeFirstPlanks100000(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        for (PlankTimePairDTO pair:pairs){
            if(pair.getPlanks()!=1){
                continue;
            }
            if(pair.getStart()<=100000&&(pair.getEnd()==null||pair.getEnd()>100000)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }

    public List<PlankTimePairDTO> judgeBeautifulPlanks(List<PlankTimePairDTO> pairs,Long timeLong){
        List<PlankTimePairDTO> planksTen = Lists.newArrayList();
        for (PlankTimePairDTO pair:pairs){
            if(pair.getStart()!=92500){
                continue;
            }
            if((pair.getEnd()==null||pair.getEnd()>timeLong)){
                planksTen.add(pair);
            }
        }
        return planksTen;
    }

    public Long judgeThreePlanks(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> pairsCopy = Lists.newArrayList();
        pairsCopy.addAll(pairs);
        for (PlankTimePairDTO pair:pairs){
            int i = 0;
            for (PlankTimePairDTO dto:pairsCopy) {
                if(!dto.getStockCode().equals(pair.getStockCode())) {
                    if (dto.getStart() <= pair.getStart() && (dto.getEnd() == null || dto.getEnd() > pair.getStart())) {
                        i++;
                        if(i>=2) {
                            return pair.getStart();
                        }
                    }
                }
            }
        }
        return null;
    }

    public Long judgeThreeFirstPlanks(List<PlankTimePairDTO> pairs){
        List<PlankTimePairDTO> pairsCopy = Lists.newArrayList();
        pairsCopy.addAll(pairs);
        for (PlankTimePairDTO pair:pairs){
            if(pair.getPlanks()!=1){
                continue;
            }
            int i = 0;
            for (PlankTimePairDTO dto:pairsCopy) {
                if((!dto.getStockCode().equals(pair.getStockCode()))&&dto.getPlanks()==1) {
                    if (dto.getStart() <= pair.getStart() && (dto.getEnd() == null || dto.getEnd() > pair.getStart())) {
                        i++;
                        if(i>=2) {
                            return pair.getStart();
                        }
                    }
                }
            }
        }
        return null;
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
        if(CollectionUtils.isEmpty(list)||list.size()<=3){
            return map;
        }
        List<MarketMoneyDTO> marketMoneyDTOS = MarketMoneyDTO.marketLevelSort(list);
        Map<String, List<Integer>> levelIndex = getLevelIndex(marketMoneyDTOS.size());
        List<Integer> firsts = levelIndex.get("first");
        List<Integer> twos = levelIndex.get("two");
        List<Integer> threes = levelIndex.get("three");
        List<MarketMoneyDTO> firstStocks = marketMoneyDTOS.subList(0, firsts.get(1));
        List<MarketMoneyDTO> twoStocks = marketMoneyDTOS.subList(twos.get(0)-1, twos.get(1));
        List<MarketMoneyDTO> threeStocks = marketMoneyDTOS.subList(threes.get(0)-1, threes.get(1));
        map.put("first",firstStocks);
        map.put("two",twoStocks);
        map.put("three",threeStocks);
        return map;
    }
    public List<BlocKFollowStaticBuyDTO> getBuysProfit(List<BlocKFollowStaticBuyDTO> buyDTOS,String tradeTime,Map<String, StockKbar> stockKbarMap,Map<String, StockKbar> nextStockKbarMap,Map<String, StockKbar> preStockKbarMap){
        List<BlocKFollowStaticBuyDTO> list = Lists.newArrayList();
        Map<String, BlocKFollowStaticBuyDTO> map = new HashMap<>();
        for (BlocKFollowStaticBuyDTO dto:buyDTOS){
            BlocKFollowStaticBuyDTO buyDTO = map.get(dto.getStockCode());
            if(buyDTO!=null){
                dto.setAmountRate(buyDTO.getAmountRate());
                dto.setProfit(buyDTO.getProfit());
                if(dto.getProfit()!=null){
                    list.add(dto);
                }
                continue;
            }
            StockKbar stockKbar = stockKbarMap.get(dto.getStockCode());
            StockKbar nextStockKbar = nextStockKbarMap.get(dto.getStockCode());
            StockKbar preStockKbar = preStockKbarMap.get(dto.getStockCode());
            if(stockKbar==null||preStockKbar==null||nextStockKbar==null){
                continue;
            }
            BigDecimal buyPrice = getStockBuyPrice(dto.getStockCode(), stockKbar.getKbarDate(), tradeTime, preStockKbar,dto);
            if(buyPrice!=null){
                dto.setBuyPrice(buyPrice);
                BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(buyPrice, stockKbar);
                dto.setChuQuanBuyPrice(chuQuanBuyPrice);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanBuyPrice.subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                dto.setRate(rate);
            }
            BigDecimal sellPrice = null;
            if(nextStockKbar.getTradeQuantity()!=null&&nextStockKbar.getTradeQuantity()!=0){
                sellPrice = nextStockKbar.getTradeAmount().divide(new BigDecimal(nextStockKbar.getTradeQuantity() * 100),2,BigDecimal.ROUND_HALF_UP);
            }
            if (buyPrice != null && sellPrice != null) {
                BigDecimal chuQuanBuyPrice = chuQuanAvgPrice(buyPrice, stockKbar);
                BigDecimal chuQuanSellPrice = chuQuanAvgPrice(sellPrice, stockKbar);
                BigDecimal profit = PriceUtil.getPricePercentRate(chuQuanSellPrice.subtract(chuQuanBuyPrice), chuQuanBuyPrice);
                dto.setProfit(profit);
                list.add(dto);
            }
            map.put(dto.getStockCode(),dto);
        }
        return list;
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


    public static Map<String,List<Integer>> getLevelIndex(int size){
        Map<String, List<Integer>> map = new HashMap<>();
        if(size<=4){
            map.put("first",Lists.newArrayList(1,1));
            map.put("two",Lists.newArrayList(2,2));
            map.put("three",Lists.newArrayList(size,size));
            return map;
        }
        Integer levelSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.3")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        Integer halfSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        if(size%2==1) {
            halfSize = Integer.valueOf(new BigDecimal(size).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_UP).toString());
        }
        Integer handleSize = Integer.valueOf(new BigDecimal(levelSize).multiply(new BigDecimal("0.5")).setScale(0, BigDecimal.ROUND_DOWN).toString());
        if(size>=60){
            levelSize = 20;
            handleSize = 10;
        }
        map.put("first",Lists.newArrayList(1,levelSize));
        if(handleSize>0) {
            if(levelSize%2==0) {
                map.put("two", Lists.newArrayList(halfSize - handleSize + 1, halfSize + handleSize));
            }else{
                map.put("two", Lists.newArrayList(halfSize - handleSize, halfSize + handleSize));
            }
        }else{
            map.put("two", Lists.newArrayList(halfSize, halfSize));
        }
        map.put("three",Lists.newArrayList(size-levelSize+1,size));
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


    public boolean isHighLowPlank(StockKbar stockKbar,BigDecimal preEndPrice,HighLowPlankDTO highLowPlankDTO){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return false;
        }
        boolean upperFlag = false;
        for (ThirdSecondTransactionDataDTO data:datas){
            BigDecimal tradePrice = data.getTradePrice();
            boolean upperPrice = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), tradePrice, preEndPrice,stockKbar.getKbarDate());
            boolean suddenPrice = PriceUtil.isHistorySuddenPrice(stockKbar.getStockCode(), tradePrice, preEndPrice,stockKbar.getKbarDate());
            if(data.getTradeTime().equals("09:25")&&upperPrice){
                upperFlag = true;
            }
            if(data.getTradeTime().equals("09:25")){
                continue;
            }
            Integer tradeType = data.getTradeType();
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            if(tradeType==1&&upperPrice){
                upperFlag=true;
            }
            if(suddenPrice&&tradeType==0){
                if(upperFlag){
                    highLowPlankDTO.setSuddenTime(data.getTradeTime());
                    return true;
                }
            }
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
           /*if(CollectionUtils.isEmpty(stockKbars)||stockKbars.size()<=20){
                return null;
            }
            stockKbars = stockKbars.subList(20, stockKbars.size());*/
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
