package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.PlankTUThreePlankBuyDTO;
import com.bazinga.dto.ThreePlankBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CirculateInfoComponent;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.query.RedisMoniorQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThreePlankBuyHotBlockComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;
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
    @Autowired
    private RedisMoniorService redisMoniorService;
    public static final List<String> BLOCK_NAME_FILTER_LIST_NO_USE = Lists.newArrayList("沪股通","深股通","标普道琼斯","新股","次新","摘帽","三季报预增"
            ,"创业板重组松绑","高送转","填权","共同富裕示范区","融资融券","MSCI","ST","国企改革");


    public void threePlankHotBlockBuyTest(){
        List<ThreePlankBuyDTO> buys = stockReplayDaily();
        List<Object[]> datas = Lists.newArrayList();

        for (ThreePlankBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getOpenRate());
            list.add(dto.isEndPlank());
            list.add(dto.getBuyTime());
            list.add(dto.getBeforePlanks());
            list.add(dto.getRaiseRate());
            list.add(dto.getSzDevRate());
            list.add(dto.getHs300DevRate());
            list.add(dto.getAllDayProfit());
            list.add(dto.getEndProfit());


            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","开盘涨幅","尾盘是否封住","买入时间","3板前15天连板数量","3板前前15日涨幅","相对上证开盘超额","相对沪深300开盘超额","均价卖出","尾盘卖出"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("独立3板买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("独立3板买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<ThreePlankBuyDTO> stockReplayDaily(){
        Map<String, BigDecimal> szIndexMap = indexRateMap("999999");
        Map<String, BigDecimal> hs300IndexMap = indexRateMap("399300");
        Map<String, List<PlankTUThreePlankBuyDTO>> map = new HashMap<>();
        List<ThreePlankBuyDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        int count  = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            count++;
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            System.out.println(stockCode+"------"+count);
            List<StockKbar> stockKBars = getStockKBars(stockCode);
            if (CollectionUtils.isEmpty(stockKBars)) {
                log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                continue;
            }
            /*if(!circulateInfo.getStockCode().equals("002095")){
                continue;
            }*/
            StockKbar preStockKbar = null;
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            LimitQueue<StockKbar> limitQueue19 = new LimitQueue<>(19);
            for (StockKbar stockKbar:stockKBars){
                limitQueue.offer(stockKbar);
                limitQueue19.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    preStockKbar = stockKbar;
                    continue;
                }
                /*if(date.after(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }*/
                int currentDayContinuePlanks = currentDayContinuePlanks(limitQueue, circulateInfo);
                boolean continueBeautifulPlank = isContinueBeautifulPlank(limitQueue, circulateInfo);
                BigDecimal openRate = null;
                boolean highPlank = false;
                PlankTUThreePlankBuyDTO plankPair = null;
                if(preStockKbar!=null&&currentDayContinuePlanks>=3){
                    highPlank = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()),preStockKbar.getAdjClosePrice());
                    if(highPlank){
                        plankPair = getPlankPair(stockKbar, preStockKbar.getClosePrice());
                        plankPair.setContinueBeautifulPlank(continueBeautifulPlank);
                        plankPair.setPlanks(currentDayContinuePlanks);
                        List<PlankTUThreePlankBuyDTO> plankTUThreePlankBuyDTOS = map.get(stockKbar.getKbarDate());
                        if(plankTUThreePlankBuyDTOS==null){
                            plankTUThreePlankBuyDTOS = Lists.newArrayList();
                            map.put(stockKbar.getKbarDate(),plankTUThreePlankBuyDTOS);
                        }
                        if(plankPair.getFirstPlankTime()!=null) {
                            plankTUThreePlankBuyDTOS.add(plankPair);
                        }
                    }
                }
                if(currentDayContinuePlanks==3&&plankPair!=null&&plankPair.getFirstPlankTime()!=null&&plankPair.getFirstBuyTime()!=null){
                    boolean endPlankFlag = PriceUtil.isHistoryUpperPrice(stockCode, stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    BigDecimal hs300Rate = hs300IndexMap.get(stockKbar.getKbarDate());
                    BigDecimal szRate = szIndexMap.get(stockKbar.getKbarDate());
                    System.out.println(stockCode+"=="+openRate+"=="+hs300Rate);
                    BigDecimal hs300Sub = openRate.subtract(hs300Rate);
                    BigDecimal szSub = openRate.subtract(szRate);
                    ThreePlankBuyDTO buyDTO = new ThreePlankBuyDTO();
                    buyDTO.setStockCode(stockCode);
                    buyDTO.setStockName(stockName);
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    buyDTO.setOpenRate(openRate);
                    buyDTO.setSzDevRate(szSub);
                    buyDTO.setHs300DevRate(hs300Sub);
                    buyDTO.setEndPlank(endPlankFlag);;
                    buyDTO.setBuyTime(plankPair.getBuyTime());
                    buyDTO.setBuyTimeLong(plankPair.getFirstBuyTime());
                    BigDecimal allDayProfit = calAllProfit(stockKbar, stockKBars);
                    BigDecimal endProfit = calEndProfit(stockKbar, stockKBars);
                    buyDTO.setAllDayProfit(allDayProfit);
                    buyDTO.setEndProfit(endProfit);
                    getBeforeKbarInfo(limitQueue19,buyDTO);
                    if(buyDTO.getBuyTime()!=null) {
                        list.add(buyDTO);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        List<ThreePlankBuyDTO> realBuys = blockLevelInfo(list, map);
        return realBuys;
    }
    public void getBeforeKbarInfo(LimitQueue<StockKbar> limitQueue,ThreePlankBuyDTO buyDTO){
        if(limitQueue==null||limitQueue.size()<=4){
            return;
        }
        List<StockKbar> list = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockKbar next = iterator.next();
            list.add(next);
        }
        List<StockKbar> stockKbars = list.subList(0, list.size() - 3);
        int highPlank = 0;
        int currentPlank = 0;
        BigDecimal raiseRate = null;
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(preStockKbar!=null){
                boolean upperFlag = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getAdjClosePrice(), stockKbar.getKbarDate());
                if(upperFlag){
                    currentPlank++;
                }else{
                    currentPlank = 0;
                }
                if(currentPlank>highPlank){
                    highPlank = currentPlank;
                }
            }
            preStockKbar = stockKbar;
        }
        StockKbar first = stockKbars.get(0);
        StockKbar end = stockKbars.get(stockKbars.size() - 1);
        if(stockKbars.size()>1){
            raiseRate = PriceUtil.getPricePercentRate(end.getAdjClosePrice().subtract(first.getAdjClosePrice()), first.getAdjClosePrice());
        }
        buyDTO.setRaiseRate(raiseRate);
        buyDTO.setBeforePlanks(highPlank);
    }

    public static void main(String[] args) {
        List<Integer> list = Lists.newArrayList(1,2,3,4);
        List<Integer> integers = list.subList(0, list.size() - 3);
        System.out.println(integers);
    }

    public void getAlLPlanks(){
        Map<String,List<String>> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        int count  = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            count++;
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            System.out.println(stockCode+"------"+count);
            List<StockKbar> stockKBars = getStockKBars(stockCode);
            if (CollectionUtils.isEmpty(stockKBars)) {
                log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                continue;
            }
            StockKbar preStockKbar = null;
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            for (StockKbar stockKbar:stockKBars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    preStockKbar = stockKbar;
                    continue;
                }
                /*if(date.after(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }*/
                if(preStockKbar!=null){
                    boolean endPlank = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(endPlank){
                        List<String> plankStocks = map.get(stockKbar.getKbarDate());
                        if(plankStocks==null){
                            plankStocks = Lists.newArrayList();
                            map.put(stockKbar.getKbarDate(),plankStocks);
                        }
                        plankStocks.add(stockCode);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        Map<String, Map<String, Integer>> stringMapMap = blockPlanksInfo(map);
        for (String tradeDateStr:stringMapMap.keySet()){
            Map<String, Integer> stringIntegerMap = stringMapMap.get(tradeDateStr);
            if(CollectionUtils.isEmpty(stringIntegerMap)){
                continue;
            }
            for (String blockCode:stringIntegerMap.keySet()) {
                if(stringIntegerMap.get(blockCode)==null||stringIntegerMap.get(blockCode)==0){
                    continue;
                }
                RedisMonior redisMonior = new RedisMonior();
                redisMonior.setRedisKey(tradeDateStr + "_"+blockCode);
                redisMonior.setRedisValue(stringIntegerMap.get(blockCode).toString());
                redisMoniorService.save(redisMonior);
            }
        }

    }

    public Map<String, Map<String,Integer>> blockPlanksInfo(Map<String,List<String>> planksMap){
        Map<String, Map<String,Integer>> map = new HashMap<>();
        List<HistoryBlockInfo> historyBlockInfos = getHistoryBlockInfo();
        Map<String,HistoryBlockInfo> historyBlockInfosMap = new HashMap<>();
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            historyBlockInfosMap.put(blockInfo.getBlockCode(),blockInfo);
        }
        for (String tradeDateStr:planksMap.keySet()){
            List<String> planks = planksMap.get(tradeDateStr);
            Map<String, HistoryBlockStocks> blockStocksMap = getHistoryBlockStocks(tradeDateStr);
            if(CollectionUtils.isEmpty(blockStocksMap)){
                continue;
            }
            for (HistoryBlockInfo historyBlockInfo:historyBlockInfos) {
                HistoryBlockStocks blockStocks = blockStocksMap.get(historyBlockInfo.getBlockCode());
                if (blockStocks == null) {
                    continue;
                }
                String stocks = blockStocks.getStocks();
                if (StringUtil.isBlank(stocks)) {
                    continue;
                }
                for (String stockCode : planks) {
                    if (stocks.contains(stockCode)) {
                        Map<String, Integer> tradeDateBlockPlanksMap = map.get(tradeDateStr);
                        if (tradeDateBlockPlanksMap == null) {
                            tradeDateBlockPlanksMap = new HashMap<>();
                            map.put(tradeDateStr, tradeDateBlockPlanksMap);
                        }
                        Integer count = tradeDateBlockPlanksMap.get(historyBlockInfo.getBlockCode());
                        if (count == null) {
                            count = 0;
                        }
                        count = count + 1;
                        tradeDateBlockPlanksMap.put(historyBlockInfo.getBlockCode(), count);
                    }
                }
            }
        }
        return map;
    }


    public List<ThreePlankBuyDTO> blockLevelInfo(List<ThreePlankBuyDTO> buyDTOS,Map<String,List<PlankTUThreePlankBuyDTO>> plankMaps){
        List<ThreePlankBuyDTO> realBuys = Lists.newArrayList();
        List<HistoryBlockInfo> historyBlockInfos = getHistoryBlockInfo();
        Map<String,HistoryBlockInfo> historyBlockInfosMap = new HashMap<>();
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            historyBlockInfosMap.put(blockInfo.getBlockCode(),blockInfo);
        }
        for (ThreePlankBuyDTO buyDTO:buyDTOS){
            if(buyDTO.getStockCode().equals("002095")){
                System.out.println("");
            }
            Map<String, List<PlankTUThreePlankBuyDTO>> beforeMap = new HashMap<>();
            List<PlankTUThreePlankBuyDTO> plankTUThreePlankBuyDTOS = plankMaps.get(buyDTO.getTradeDate());
            Map<String, HistoryBlockStocks> blockStocksMap = getHistoryBlockStocks(buyDTO.getTradeDate());
            if(CollectionUtils.isEmpty(blockStocksMap)){
                continue;
            }
            int count = 0;
            for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
                HistoryBlockStocks blockStocks = blockStocksMap.get(historyBlockInfo.getBlockCode());
                if(blockStocks==null){
                    continue;
                }
                String stocks = blockStocks.getStocks();
                if(StringUtil.isBlank(stocks)||!stocks.contains(buyDTO.getStockCode())){
                    continue;
                }
                List<PlankTUThreePlankBuyDTO> beforePairs = beforeMap.get(historyBlockInfo.getBlockCode());
                if(beforePairs==null){
                    beforePairs = Lists.newArrayList();
                    beforeMap.put(historyBlockInfo.getBlockCode(),beforePairs);
                }
                Date preTradeDate = commonComponent.preTradeDate(DateUtil.parseDate(buyDTO.getTradeDate(), DateUtil.yyyyMMdd));
                String preDateStr = DateUtil.format(preTradeDate, DateUtil.yyyyMMdd);
                RedisMonior byRedisKey = redisMoniorService.getByRedisKey(preDateStr + "_" + historyBlockInfo.getBlockCode());
                if(byRedisKey!=null&& !StringUtils.isBlank(byRedisKey.getRedisValue())){
                    String redisValue = byRedisKey.getRedisValue();
                    int blockCount = Integer.valueOf(redisValue);
                    if(blockCount>=count){
                        count = blockCount;
                    }
                }
                for (PlankTUThreePlankBuyDTO pair:plankTUThreePlankBuyDTOS){
                    if(!stocks.contains(pair.getStockCode())){
                        continue;
                    }
                    if(pair.getStockCode().equals(buyDTO.getStockCode())){
                        continue;
                    }
                    if(pair.isContinueBeautifulPlank()){
                        continue;
                    }
                    if(pair.getFirstPlankTime()<buyDTO.getBuyTimeLong()){
                        beforePairs.add(pair);
                    }
                }
            }
            if(count>=10){
                continue;
            }
            if(beforeMap.size()==0){
                realBuys.add(buyDTO);
                continue;
            }
            boolean beforeCanBuy = getBeforeCanBuy(beforeMap);
            if(beforeCanBuy){
                realBuys.add(buyDTO);
            }
        }
        return realBuys;
    }



    public List<String> getGatherLevelInfo(Map<String, List<PlankTUThreePlankBuyDTO>> gatherMap){
        List<String> list = Lists.newArrayList();
        int maxCounts = 0;
        for (String blockCode:gatherMap.keySet()){
            List<PlankTUThreePlankBuyDTO> dtos = gatherMap.get(blockCode);
            if(dtos.size()==maxCounts){
                list.add(blockCode);
            }else if(dtos.size()<maxCounts){
                continue;
            }else {
                list.clear();
                list.add(blockCode);
                maxCounts = dtos.size();
            }
        }
        return list;
    }

    public boolean getBeforeCanBuy(Map<String, List<PlankTUThreePlankBuyDTO>> beforeMap){
        for (String blockCode:beforeMap.keySet()){
            List<PlankTUThreePlankBuyDTO> beforeDtos = beforeMap.get(blockCode);
            if(beforeDtos.size()>=2){
                return false;
            }
            for (PlankTUThreePlankBuyDTO beforeDto:beforeDtos){
                if(beforeDto.getPlanks()>3){
                     return false;
                }
            }
        }
        return true;
    }

    //判断连板天数
    public int preDayContinuePlanks(LimitQueue<StockKbar> limitQueue, CirculateInfo circulateInfo){
        if(limitQueue.size()<2){
            return 0;
        }
        List<StockKbar> kbars = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar kbar = iterator.next();
            kbars.add(kbar);
        }
        List<StockKbar> reverse = Lists.reverse(kbars);
        StockKbar preStockKbar = null;
        int current = 0;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(preStockKbar!=null) {
                BigDecimal endPrice = stockKbar.getClosePrice();
                boolean endPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getClosePrice(),endPrice,preStockKbar.getKbarDate());
                if (i>2){
                    if (endPlank) {
                        current++;
                    } else {
                        return current;
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        return current;
    }

    public int currentDayContinuePlanks(LimitQueue<StockKbar> limitQueue, CirculateInfo circulateInfo){
        if(limitQueue.size()<2){
            return 0;
        }
        List<StockKbar> kbars = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar kbar = iterator.next();
            kbars.add(kbar);
        }
        List<StockKbar> reverse = Lists.reverse(kbars);
        StockKbar preStockKbar = null;
        int current = 0;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(preStockKbar!=null) {
                BigDecimal endPrice = stockKbar.getClosePrice();
                boolean endPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getClosePrice(),endPrice,preStockKbar.getKbarDate());
                boolean highPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getHighPrice(),endPrice,preStockKbar.getKbarDate());
                if(i==2){
                    if(highPlank){
                        current++;
                    }else{
                        return current;
                    }
                }
                if(i>2) {
                    if (endPlank) {
                        current++;
                    } else {
                        return current;
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        return current;
    }
    public boolean isContinueBeautifulPlank(LimitQueue<StockKbar> limitQueue, CirculateInfo circulateInfo){
        if(limitQueue.size()<3){
            return false;
        }
        List<StockKbar> kbars = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar kbar = iterator.next();
            kbars.add(kbar);
        }
        List<StockKbar> reverse = Lists.reverse(kbars);
        StockKbar preStockKbar = null;
        int current = 0;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(preStockKbar!=null) {
                BigDecimal endPrice = stockKbar.getClosePrice();
                boolean highPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getHighPrice(),endPrice,preStockKbar.getKbarDate());
                boolean openPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getOpenPrice(),endPrice,preStockKbar.getKbarDate());
                if(i==2){
                    if(!openPlank){
                        return false;
                    }
                }
                if(i==3) {
                    if(!(highPlank && preStockKbar.getHighPrice().equals(preStockKbar.getLowPrice()))){
                        return false;
                    }

                }
            }
            preStockKbar = stockKbar;
        }
        return true;
    }

    public BigDecimal  calAllProfit(StockKbar buyKbar, List<StockKbar> stockKbars){
        int allSell = 0;
        boolean flag = false;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                if(!stockKbar.getHighPrice().equals(stockKbar.getLowPrice())){
                    allSell++;
                }
            }
            if(allSell==1){
                BigDecimal rate = null;
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                if(avgPrice!=null) {
                    BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                    rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyKbar.getAdjHighPrice()), buyKbar.getAdjHighPrice());
                }
                return rate;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
        return null;
    }

    public BigDecimal  calEndProfit(StockKbar buyKbar, List<StockKbar> stockKbars){
        int endSell = 0;
        boolean flag = false;
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
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
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyKbar.getAdjHighPrice()), buyKbar.getAdjHighPrice());
                return rate;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
            preStockKbar = stockKbar;
        }
        return null;
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

    public List<HistoryBlockInfo> getHistoryBlockInfo(){
        HistoryBlockInfoQuery query = new HistoryBlockInfoQuery();
        query.setBlockType(1);
        List<HistoryBlockInfo> historyBlockInfos = historyBlockInfoService.listByCondition(query);
        List<HistoryBlockInfo> list = Lists.newArrayList();
        for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
            boolean flag = false;
            for (String blockName:BLOCK_NAME_FILTER_LIST_NO_USE){
                if(historyBlockInfo.getBlockName().contains(blockName)){
                    flag = true;
                }
            }
            if(!flag){
                list.add(historyBlockInfo);
            }
        }
        return list;
    }
    public Map<String,HistoryBlockStocks> getHistoryBlockStocks(String tradeDate){
        Map<String, HistoryBlockStocks> map = new HashMap<>();
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStockes = historyBlockStocksService.listByCondition(query);
        List<HistoryBlockInfo> list = Lists.newArrayList();
        for (HistoryBlockStocks historyBlockStocks:historyBlockStockes){
           map.put(historyBlockStocks.getBlockCode(),historyBlockStocks);
        }
        return map;
    }


    public PlankTUThreePlankBuyDTO getPlankPair(StockKbar stockKbar, BigDecimal preEndPrice){
        PlankTUThreePlankBuyDTO pairDTO = new PlankTUThreePlankBuyDTO();
        pairDTO.setTradeDate(stockKbar.getKbarDate());
        pairDTO.setStockCode(stockKbar.getStockCode());
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
        if(CollectionUtils.isEmpty(datas)){
            return pairDTO;
        }
        boolean preIsUpper = false;
        int i = 0;
        int index = 0;
        String preMin = "09:25";
        boolean gatherUpper = false;
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
                gatherUpper = true;
            }
            Integer tradeType = data.getTradeType();
            i++;
            if(i==2&&gatherUpper&&upperPrice&&tradeType==1){
                pairDTO.setStockCode(stockKbar.getStockCode());
                pairDTO.setFirstPlankTime(92500l);
                preIsUpper = true;
            }
            if(tradeType!=0&&tradeType!=1){
                continue;
            }
            if(upperPrice&&tradeType==1&&!preIsUpper){
                long firstBuyTime = timeToLong(data.getTradeTime(), index);
                pairDTO.setFirstBuyTime(firstBuyTime);
                pairDTO.setBuyTime(data.getTradeTime());
                if(pairDTO.getFirstPlankTime()==null){
                    pairDTO.setFirstPlankTime(firstBuyTime);
                    pairDTO.setBuyTime(data.getTradeTime());
                }
                return pairDTO;
            }
            if((!upperPrice)||tradeType!=1){
                preIsUpper = false;
            }
        }
        return pairDTO;
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
    public Map<String,BigDecimal> indexRateMap(String indexCode){
        Map<String, BigDecimal> map = new HashMap<>();
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        kbarQuery.setStockCode(indexCode);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        StockKbar preKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(preKbar!=null){
                BigDecimal rate = PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(preKbar.getClosePrice()), preKbar.getClosePrice());
                map.put(stockKbar.getKbarDate(),rate);
            }
            preKbar = stockKbar;
        }
        return map;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(stockCode);
        kbarQuery.setKbarDateFrom("20171201");
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        List<StockKbar> stockKbarDeleteNew = commonComponent.deleteNewStockTimes(stockKbars, 2000);
        return stockKbarDeleteNew;
    }


}
