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
public class PlankAmountSuperTestComponent {
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



    public void plankAmountInfo(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        Map<String, PlankSuperTestDTO> highPlankInfoMap = getHighPlankInfo(circulateInfos);
        List<Object[]> datas = Lists.newArrayList();
        for (String tradeDate:highPlankInfoMap.keySet()) {
            PlankSuperTestDTO dto = highPlankInfoMap.get(tradeDate);
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getPlankAmount());
            list.add(dto.getNoPlankAmount());
            list.add(dto.getPlankCount());
            list.add(dto.getEndPlankCount());

            list.add(dto.getFirstPlankAmount());
            list.add(dto.getFirstNoPlankAmount());
            list.add(dto.getFirstPlankCount());
            list.add(dto.getFirstEndPlankCount());

            list.add(dto.getHighPlankAmount());
            list.add(dto.getHighNoPlankAmount());
            list.add(dto.getHighPlankCount());
            list.add(dto.getHighEndPlankCount());

            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","日期","板价成交金额","非板价成交金额","板数量","封住数量","首板板价成交金额","首板非板价成交金额","首板板数量","首板封住数量","连板板价成交金额","连板非板价成交金额","连板板数量","连板封住数量"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("板上成交额",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("板上成交额");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }


    public Map<String, PlankSuperTestDTO> getHighPlankInfo(List<CirculateInfo> circulateInfos){
        Map<String, PlankSuperTestDTO> map = new HashMap<>();
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
                if(date.before(DateUtil.parseDate("20220501", DateUtil.yyyyMMdd))){
                    continue;
                }
                if(preKbar!=null) {
                    int planks = calPlanks(limitQueue);
                    boolean highUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(!highUpper){
                        highUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getAdjHighPrice(), preKbar.getAdjClosePrice(), stockKbar.getKbarDate());
                    }

                    boolean endUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getClosePrice(), preKbar.getClosePrice(), stockKbar.getKbarDate());
                    if(!endUpper){
                        endUpper = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getAdjClosePrice(), preKbar.getAdjClosePrice(), stockKbar.getKbarDate());
                    }

                    if(highUpper){
                        PlankSuperTestDTO dto = map.get(stockKbar.getKbarDate());
                        if(dto==null){
                            dto = new PlankSuperTestDTO();
                            dto.setTradeDate(stockKbar.getKbarDate());
                            map.put(stockKbar.getKbarDate(),dto);
                        }
                        getStockAmountInfo(circulateInfo.getStockCode(), stockKbar.getKbarDate(), preKbar, stockKbar, dto,planks);
                        dto.setPlankCount(dto.getPlankCount()+1);
                        if(endUpper) {
                            dto.setEndPlankCount(dto.getEndPlankCount() + 1);
                        }
                        if(planks==1){
                            dto.setFirstPlankCount(dto.getFirstPlankCount()+1);
                            if(endUpper){
                                dto.setFirstEndPlankCount(dto.getFirstEndPlankCount()+1);
                            }
                        }else{
                            dto.setHighPlankCount(dto.getHighPlankCount()+1);
                            if(endUpper){
                                dto.setHighEndPlankCount(dto.getHighPlankCount()+1);
                            }
                        }
                    }

                }
                preKbar = stockKbar;
            }
        }
        return map;
    }



    public static void main(String[] args) {

    }

    public void getStockAmountInfo(String stockCode,String tradeDate,StockKbar preStockKbar,StockKbar stockKbar,PlankSuperTestDTO buyDTO,int planks){
        List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, tradeDate);
        for (ThirdSecondTransactionDataDTO data : datas) {
            BigDecimal tradePrice = data.getTradePrice();
            BigDecimal amount = tradePrice.multiply(new BigDecimal(data.getTradeQuantity() * 100));
            boolean historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, preStockKbar.getClosePrice(), tradeDate);
            if(!historyUpperPrice){
                BigDecimal zeroPrice = chuQuanAvgPrice(preStockKbar.getClosePrice(), stockKbar);
                historyUpperPrice = PriceUtil.isHistoryUpperPrice(stockCode, tradePrice, zeroPrice, tradeDate);
            }
            if(historyUpperPrice){
                BigDecimal totalAmount = buyDTO.getPlankAmount().add(amount);
                buyDTO.setPlankAmount(totalAmount);
            }else{
                BigDecimal totalAmount = buyDTO.getNoPlankAmount().add(amount);
                buyDTO.setNoPlankAmount(totalAmount);
            }
            if(planks>1){
                if(historyUpperPrice){
                    BigDecimal totalAmount = buyDTO.getHighPlankAmount().add(amount);
                    buyDTO.setHighPlankAmount(totalAmount);
                }else{
                    BigDecimal totalAmount = buyDTO.getHighNoPlankAmount().add(amount);
                    buyDTO.setHighNoPlankAmount(totalAmount);
                }
            }else{
                if(historyUpperPrice){
                    BigDecimal totalAmount = buyDTO.getFirstPlankAmount().add(amount);
                    buyDTO.setFirstPlankAmount(totalAmount);
                }else{
                    BigDecimal totalAmount = buyDTO.getFirstNoPlankAmount().add(amount);
                    buyDTO.setFirstNoPlankAmount(totalAmount);
                }
            }
        }
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
            reason = kbar.getAdjOpenPrice().divide(kbar.getOpenPrice(),10,BigDecimal.ROUND_HALF_UP);
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
                if(!endUpper){
                    endUpper = PriceUtil.isHistoryUpperPrice(stockKbar.getStockCode(), nextStockKbar.getAdjClosePrice(), stockKbar.getAdjClosePrice(), nextStockKbar.getKbarDate());
                }
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
