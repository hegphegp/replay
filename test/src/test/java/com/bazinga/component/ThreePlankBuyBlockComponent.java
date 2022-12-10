package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CirculateInfoComponent;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.HistoryBlockInfo;
import com.bazinga.replay.model.HistoryBlockStocks;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.HistoryBlockInfoQuery;
import com.bazinga.replay.query.HistoryBlockStocksQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import jodd.util.StringUtil;
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
public class ThreePlankBuyBlockComponent {
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
    public static final List<String> BLOCK_NAME_FILTER_LIST_NO_USE = Lists.newArrayList("沪股通","深股通","标普道琼斯","新股","次新"
            ,"创业板重组松绑","高送转","填权","共同富裕示范区","融资融券","MSCI","ST");


    public void threePlankBuyTest(){
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
            list.add(dto.getBlockNameLevel());
            list.add(dto.getAllDayProfit());
            list.add(dto.getEndProfit());


            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","开盘涨幅","尾盘是否封住","买入时间","板块排名信息","均价卖出","尾盘卖出"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("3板买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("3板买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<ThreePlankBuyDTO> stockReplayDaily(){
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
            StockKbar preStockKbar = null;
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            for (StockKbar stockKbar:stockKBars){
                limitQueue.offer(stockKbar);
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(date.before(DateUtil.parseDate("20221101", DateUtil.yyyyMMdd))){
                    preStockKbar = stockKbar;
                    continue;
                }
                /*if(date.after(DateUtil.parseDate("20210101", DateUtil.yyyyMMdd))){
                    continue;
                }*/
                int prePlanks = preDayContinuePlanks(limitQueue, circulateInfo);
                BigDecimal openRate = null;
                boolean highPlank = false;
                PlankTUThreePlankBuyDTO plankPair = null;
                if(preStockKbar!=null){
                    highPlank = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(preStockKbar.getAdjClosePrice()),preStockKbar.getAdjClosePrice());
                    if(highPlank){
                        plankPair = getPlankPair(stockKbar, preStockKbar.getClosePrice());
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
                if(prePlanks==2&&openRate!=null&&openRate.compareTo(new BigDecimal("5"))>=0&&highPlank){
                    boolean endPlankFlag = PriceUtil.isHistoryUpperPrice(stockCode, stockKbar.getClosePrice(), preStockKbar.getClosePrice(), stockKbar.getKbarDate());
                    ThreePlankBuyDTO buyDTO = new ThreePlankBuyDTO();
                    buyDTO.setStockCode(stockCode);
                    buyDTO.setStockName(stockName);
                    buyDTO.setTradeDate(stockKbar.getKbarDate());
                    buyDTO.setOpenRate(openRate);
                    buyDTO.setEndPlank(endPlankFlag);
                    buyDTO.setBuyTime(plankPair.getBuyTime());
                    buyDTO.setBuyTimeLong(plankPair.getFirstBuyTime());
                    BigDecimal allDayProfit = calAllProfit(stockKbar, stockKBars);
                    BigDecimal endProfit = calEndProfit(stockKbar, stockKBars);
                    buyDTO.setAllDayProfit(allDayProfit);
                    buyDTO.setEndProfit(endProfit);
                    if(buyDTO.getBuyTime()!=null) {
                        list.add(buyDTO);
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        blockLevelInfo(list,map);
        return list;
    }

    public void blockLevelInfo(List<ThreePlankBuyDTO> buyDTOS,Map<String,List<PlankTUThreePlankBuyDTO>> plankMaps){
        List<HistoryBlockInfo> historyBlockInfos = getHistoryBlockInfo();
        Map<String,HistoryBlockInfo> historyBlockInfosMap = new HashMap<>();
        for (HistoryBlockInfo blockInfo:historyBlockInfos){
            historyBlockInfosMap.put(blockInfo.getBlockCode(),blockInfo);
        }
        for (ThreePlankBuyDTO buyDTO:buyDTOS){
            Map<String, List<PlankTUThreePlankBuyDTO>> beforeMap = new HashMap<>();
            Map<String, List<PlankTUThreePlankBuyDTO>> gatherPlankMap = new HashMap<>();
            List<PlankTUThreePlankBuyDTO> plankTUThreePlankBuyDTOS = plankMaps.get(buyDTO.getTradeDate());
            Map<String, HistoryBlockStocks> blockStocksMap = getHistoryBlockStocks(buyDTO.getTradeDate());
            if(CollectionUtils.isEmpty(blockStocksMap)){
                continue;
            }
            for (HistoryBlockInfo historyBlockInfo:historyBlockInfos){
                HistoryBlockStocks blockStocks = blockStocksMap.get(historyBlockInfo.getBlockCode());
                if(blockStocks==null){
                    continue;
                }
                String stocks = blockStocks.getStocks();
                if(StringUtil.isBlank(stocks)||!stocks.contains(buyDTO.getStockCode())){
                    continue;
                }
                List<PlankTUThreePlankBuyDTO> gatherPlanks = gatherPlankMap.get(historyBlockInfo.getBlockCode());
                if(gatherPlanks==null){
                    gatherPlanks = Lists.newArrayList();
                    gatherPlankMap.put(historyBlockInfo.getBlockCode(),gatherPlanks);
                }
                List<PlankTUThreePlankBuyDTO> beforePairs = beforeMap.get(historyBlockInfo.getBlockCode());
                if(beforePairs==null){
                    beforePairs = Lists.newArrayList();
                    beforeMap.put(historyBlockInfo.getBlockCode(),beforePairs);
                }

                for (PlankTUThreePlankBuyDTO pair:plankTUThreePlankBuyDTOS){
                    if(!stocks.contains(pair.getStockCode())){
                        continue;
                    }
                    if(pair.getFirstPlankTime().equals(92500l)){
                        gatherPlanks.add(pair);
                    }
                    if(pair.getStockCode().equals(buyDTO.getStockCode())){
                        continue;
                    }
                    if(pair.getFirstPlankTime()<buyDTO.getBuyTimeLong()){
                        beforePairs.add(pair);
                    }
                }
            }
            if(gatherPlankMap.size()==0||beforeMap.size()==0){
                continue;
            }
            List<String> gatherHighBlocks = getGatherLevelInfo(gatherPlankMap);
            String nameLevel = getBeforeCanBuy(beforeMap, gatherHighBlocks,historyBlockInfosMap);
            buyDTO.setBlockNameLevel(nameLevel);
        }
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

    public String getBeforeCanBuy(Map<String, List<PlankTUThreePlankBuyDTO>> beforeMap,List<String> gatherList,Map<String,HistoryBlockInfo> historyBlockInfosMap){
        String nameLevel = null;
        for (String blockCode:gatherList){
            List<PlankTUThreePlankBuyDTO> beforeDtos = beforeMap.get(blockCode);
            HistoryBlockInfo historyBlockInfo = historyBlockInfosMap.get(blockCode);
            int level = beforeDtos.size() + 1;
            if(level<=5){
                if(nameLevel==null){
                    nameLevel = historyBlockInfo.getBlockName()+"="+level;
                }else{
                    nameLevel = nameLevel+"|"+historyBlockInfo.getBlockName()+"="+level;
                }
            }
        }
        return nameLevel;
    }

    //判断连板天数
    public int preDayContinuePlanks(LimitQueue<StockKbar> limitQueue, CirculateInfo circulateInfo){
        if(limitQueue.size()<3){
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
                BigDecimal avgPrice = historyTransactionDataComponent.calAvgPrice(stockKbar.getStockCode(), DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd));
                BigDecimal chuQuanAvgPrice = chuQuanAvgPrice(avgPrice, stockKbar);
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(buyKbar.getAdjHighPrice()), buyKbar.getAdjHighPrice());
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
                BigDecimal rate = PriceUtil.getPricePercentRate(chuQuanAvgPrice.subtract(stockKbar.getAdjHighPrice()), stockKbar.getAdjHighPrice());
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
                if(pairDTO.getFirstPlankTime()==null){
                    pairDTO.setFirstPlankTime(firstBuyTime);
                    pairDTO.setBuyTime(data.getTradeTime());
                }
                return pairDTO;
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

    public List<StockKbar> getStockKBars(String stockCode){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(stockCode);
        kbarQuery.setKbarDateFrom("20171201");
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        List<StockKbar> stockKbarDeleteNew = commonComponent.deleteNewStockTimes(stockKbars, 1000);
        return stockKbarDeleteNew;
    }


}
