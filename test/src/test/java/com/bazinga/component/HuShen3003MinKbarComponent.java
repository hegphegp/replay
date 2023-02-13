package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.HuShen300MacdBuyDTO;
import com.bazinga.dto.MacdBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.MacdIndexDTO;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.StockIndexQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsQuoteInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class HuShen3003MinKbarComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private HistoryBlockInfoService historyBlockInfoService;
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private StockKbarService stockKbarService;

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void macdExcel(){
        List<HuShen300MacdBuyDTO> buys = getMacdBuyDTOFour();
        List<Object[]> datas = Lists.newArrayList();

        for (HuShen300MacdBuyDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getRedirect());
            list.add(dto.getRedirect());
            Date buyDate = DateUtil.parseDate(dto.getBuyTime(), DateUtil.yyyyMMddHHmmss);
            Date sellDate = DateUtil.parseDate(dto.getSellTime(), DateUtil.yyyyMMddHHmmss);
            list.add(DateUtil.format(buyDate,DateUtil.yyyyMMdd));
            list.add(DateUtil.format(buyDate,DateUtil.HHmmss_DEFALT));
            list.add(DateUtil.format(sellDate,DateUtil.yyyyMMdd));
            list.add(DateUtil.format(sellDate,DateUtil.HHmmss_DEFALT));
            list.add(dto.getMacdBuy());
            list.add(dto.getBuyPrice());
            list.add(dto.getSellPrice());
            list.add(dto.getProfit());
            list.add(dto.getProfitValue());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","方向（1多 -1空）","买入日期","买入时间点","卖出日期","卖出时间点","买入macd","买入价格","卖出价格","利润","利润值"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("macd买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("macd买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<HuShen300MacdBuyDTO> getMacdBuyDTOFour(){
        List<HuShen300MacdBuyDTO> buys = Lists.newArrayList();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20230212",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("20230213",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<Date> kbarSeconds = getKbarSeconds(5);
        List<Long> preTradeDatePoints = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            List<Long> tradePoints = getTradeDateSecondPoint(yyyyMMdd,kbarSeconds);
            List<Long> alls = Lists.newArrayList();
            alls.addAll(preTradeDatePoints);
            alls.addAll(tradePoints);
            List<StockIndex> stockIndices = getCalStockIndex(alls.get(0), alls.get(alls.size() - 1));
            if(CollectionUtils.isEmpty(stockIndices)){
                continue;
            }
            System.out.println(yyyyMMdd);
            StockIndex preIndex = null;
            for (StockIndex stockIndex:stockIndices){
                if(stockIndex.getMacd().compareTo(BigDecimal.ZERO)==0){
                    continue;
                }
                if(preIndex!=null&&preIndex.getMacd()!=null&&stockIndex.getKbarDate().startsWith(yyyyMMdd)){
                    BigDecimal macd = stockIndex.getMacd();
                    int redirect = stockIndex.getMacd().compareTo(BigDecimal.ZERO);
                    int preRedirect = preIndex.getMacd().compareTo(BigDecimal.ZERO);
                    if(redirect!=preRedirect) {
                        HuShen300MacdBuyDTO buyDTO = new HuShen300MacdBuyDTO();
                        buyDTO.setRedirect(redirect);
                        buyDTO.setBuyStockIndex(stockIndex);
                        buyDTO.setPreBuyStockIndex(preIndex);
                        buyDTO.setMacdBuy(macd);
                        buyDTO.setBuyTime(stockIndex.getKbarDate());
                        buys.add(buyDTO);
                    }
                }
                preIndex = stockIndex;
            }
            preTradeDatePoints = tradePoints;
        }
        for (HuShen300MacdBuyDTO buy:buys){
            List<StockIndex> afterStockIndexes = getAfterStockIndex(Long.valueOf(buy.getBuyStockIndex().getKbarDate()));
            List<StockIndex> preIndexes = getPreStockIndex(Long.valueOf(buy.getPreBuyStockIndex().getKbarDate()));
            BigDecimal preArea = calArea(preIndexes);
            buy.setPreArea(preArea);
            calSellTime(afterStockIndexes,buy);
        }
        for (HuShen300MacdBuyDTO buy:buys){
            if(buy.getSellTime()==null){
                continue;
            }
            StockKbar buyKbar = stockKbarService.getByUniqueKey("IFZLCFE" + "_" + buy.getBuyTime());
            StockKbar sellKbar = stockKbarService.getByUniqueKey("IFZLCFE" + "_" + buy.getSellTime());
            if(buyKbar==null){
                continue;
            }
            buy.setBuyPrice(buyKbar.getClosePrice());
            if(sellKbar!=null){
                buy.setSellPrice(sellKbar.getClosePrice());
                BigDecimal profitValue = buy.getSellPrice().subtract(buy.getBuyPrice());
                BigDecimal rate = PriceUtil.getPricePercentRate(buy.getSellPrice().subtract(buy.getBuyPrice()), buy.getBuyPrice());
                BigDecimal profit = rate.multiply(new BigDecimal(buy.getRedirect())).setScale(4,BigDecimal.ROUND_HALF_UP);
                profitValue = profitValue.multiply(new BigDecimal(buy.getRedirect())).setScale(4,BigDecimal.ROUND_HALF_UP);
                buy.setProfit(profit);
                buy.setProfitValue(profitValue);
            }

        }
        return buys;
    }

    public BigDecimal calArea(List<StockIndex> stockIndices){
        if(CollectionUtils.isEmpty(stockIndices)){
            return null;
        }
        BigDecimal area = BigDecimal.ZERO;
        StockIndex first = stockIndices.get(0);
        int firstRedirect = first.getMacd().compareTo(BigDecimal.ZERO);
        for (StockIndex stockIndex:stockIndices){
            int curRedirect = stockIndex.getMacd().compareTo(BigDecimal.ZERO);
            if(curRedirect==0||curRedirect==firstRedirect) {
                area = area.add(stockIndex.getMacd());
            }else {
                return area.abs();
            }
        }
        return area.abs();
    }

    public void calSellTime(List<StockIndex> stockIndices,HuShen300MacdBuyDTO buyDTO){
        if(CollectionUtils.isEmpty(stockIndices)||buyDTO.getPreArea()==null){
            return ;
        }
        BigDecimal area = BigDecimal.ZERO;
        StockIndex first = stockIndices.get(0);
        int firstRedirect = first.getMacd().compareTo(BigDecimal.ZERO);
        for (StockIndex stockIndex:stockIndices){
            int curRedirect = stockIndex.getMacd().compareTo(BigDecimal.ZERO);
            buyDTO.setSellStockIndex(stockIndex);
            buyDTO.setSellTime(stockIndex.getKbarDate());
            if(curRedirect==0||curRedirect==firstRedirect) {
                area = area.add(stockIndex.getMacd());
                if(area.abs().compareTo(buyDTO.getPreArea())>=0){
                    break;
                }
            }else {
                break;
            }
        }
        buyDTO.setArea(area.abs());
    }



    public List<HuShen300MacdBuyDTO> getMacdBuyDTO(){
        List<HuShen300MacdBuyDTO> buys = Lists.newArrayList();
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("20221125",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<Date> kbarSeconds = getKbarSeconds(5);
        List<Long> preTradeDatePoints = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            List<Long> tradePoints = getTradeDateSecondPoint(yyyyMMdd,kbarSeconds);
            List<Long> alls = Lists.newArrayList();
            alls.addAll(preTradeDatePoints);
            alls.addAll(tradePoints);
            List<StockIndex> stockIndices = getCalStockIndex(alls.get(0), alls.get(alls.size() - 1));
            if(CollectionUtils.isEmpty(stockIndices)){
                continue;
            }
            System.out.println(yyyyMMdd);
            LimitQueue<StockIndex> limitQueue = new LimitQueue<>(3);
            StockIndex preIndex = null;
            for (StockIndex stockIndex:stockIndices){
                limitQueue.offer(stockIndex);
                if(preIndex!=null&&preIndex.getMacd()!=null&&stockIndex.getKbarDate().startsWith(yyyyMMdd)){
                    BigDecimal macd = stockIndex.getMacd();
                    int redirect = getRedirect(limitQueue);
                    if(redirect!=0) {
                        HuShen300MacdBuyDTO buyDTO = new HuShen300MacdBuyDTO();
                        buyDTO.setRedirect(redirect);
                        buyDTO.setMacdBuy(macd);
                        buyDTO.setBuyTime(stockIndex.getKbarDate());
                        buys.add(buyDTO);
                    }
                }
                preIndex = stockIndex;
            }
            preTradeDatePoints = tradePoints;
        }
        for (HuShen300MacdBuyDTO buy:buys){
            StockKbar buyKbar = stockKbarService.getByUniqueKey("IFZLCFE" + "_" + buy.getBuyTime());
            StockKbarQuery kbarQuery = new StockKbarQuery();
            kbarQuery.setKbarDateFrom(buy.getBuyTime());
            kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
            kbarQuery.setLimit(6);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
            StockKbar sellKbar = null;
            if(stockKbars!=null&&stockKbars.size()==6){
                sellKbar = stockKbars.get(5);
            }
            buy.setBuyPrice(buyKbar.getClosePrice());
            if(sellKbar!=null){
                buy.setSellPrice(sellKbar.getClosePrice());
                buy.setSellTime(sellKbar.getKbarDate());
                BigDecimal profitValue = buy.getSellPrice().subtract(buy.getBuyPrice());
                BigDecimal rate = PriceUtil.getPricePercentRate(buy.getSellPrice().subtract(buy.getBuyPrice()), buy.getBuyPrice());
                BigDecimal profit = rate.multiply(new BigDecimal(buy.getRedirect())).setScale(4,BigDecimal.ROUND_HALF_UP);
                profitValue = profitValue.multiply(new BigDecimal(buy.getRedirect())).setScale(4,BigDecimal.ROUND_HALF_UP);
                buy.setProfit(profit);
                buy.setProfitValue(profitValue);
            }

        }
        return buys;
    }

    public int getRedirect(LimitQueue<StockIndex> limitQueue){
        List<StockIndex> list = Lists.newArrayList();
        Iterator<StockIndex> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            StockIndex next = iterator.next();
            list.add(next);
        }
        List<StockIndex> reverse = Lists.reverse(list);
        int i = 0;
        int first = 0;
        for (StockIndex stockIndex:reverse){
            BigDecimal macd = stockIndex.getMacd();
            int direct = macd.compareTo(BigDecimal.ZERO);
            i++;
            if(i==1){
                if(direct==0){
                    return 0;
                }
                first = direct;
            }
            if(i==2){
                if(direct==0){
                    continue;
                }
                if(direct==first){
                    return 0;
                }
                return first;
            }
            if(i==3){
                if(direct==first){
                    return 0;
                }
                if(direct==0){
                    return 0;
                }
                return first;
            }
        }
        return 0;
    }


    public List<MacdBuyDTO> calBiasSave(){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        //tradeDatePoolQuery.setTradeDateTo(DateUtil.parseDate("20200909",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<Date> kbarSeconds = getKbarSeconds(15);
        List<Long> alls = Lists.newArrayList();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            List<Long> tradePoints = getTradeDateSecondPoint(yyyyMMdd,kbarSeconds);
            if(alls.size()>=1000){
                alls = alls.subList(alls.size()-1000,alls.size());
            }
            alls.addAll(tradePoints);
            List<StockKbar> stockKbars = getCalMacdStockKbars(alls.get(0), alls.get(alls.size() - 1));
            System.out.println(yyyyMMdd);
            List<MacdIndexDTO> list = Lists.newArrayList();
            int i = 0;
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKbars){
                i++;
                if(i==1){
                    MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                    list.add(macdIndexDTO);
                }else{
                    MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                    list.add(macdIndexDTO);
                }
                preStockKbar = stockKbar;
            }
            for (MacdIndexDTO indexDTO:list){
                if(indexDTO.getTradeDate().startsWith(yyyyMMdd)){
                    StockIndex stockIndex = new StockIndex();
                    stockIndex.setStockCode("IFZLCFE");
                    stockIndex.setStockName("沪深300期货指数5s");
                    stockIndex.setKbarDate(indexDTO.getTradeDate());
                    stockIndex.setUniqueKey(stockIndex.getStockCode()+"_"+indexDTO.getTradeDate());
                    stockIndex.setDiff(indexDTO.getDiff());
                    stockIndex.setDea(indexDTO.getDea());
                    stockIndex.setMacd(indexDTO.getBar());
                    StockIndex byUniqueKey = stockIndexService.getByUniqueKey(stockIndex.getUniqueKey());
                    if(byUniqueKey==null) {
                        stockIndexService.save(stockIndex);
                    }else{
                        byUniqueKey.setStockCode("IFZLCFE");
                        byUniqueKey.setStockName("沪深300期货指数5s");
                        byUniqueKey.setKbarDate(indexDTO.getTradeDate());
                        byUniqueKey.setUniqueKey(stockIndex.getStockCode()+"_"+indexDTO.getTradeDate());
                        byUniqueKey.setDiff(indexDTO.getDiff());
                        byUniqueKey.setDea(indexDTO.getDea());
                        byUniqueKey.setMacd(indexDTO.getBar());
                        stockIndexService.updateById(byUniqueKey);
                    }
                }
            }
        }
        return null;

    }
    public  List<StockKbar> getCalMacdStockKbars(Long start,Long end){
        Long startBefore = start - 1000l;
        Long startDelay = end + 1000l;
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setKbarDateFrom(startBefore.toString());
        kbarQuery.setKbarDateTo(startDelay.toString());
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        return stockKbars;
    }

    public  List<StockIndex> getAfterStockIndex(Long start){
        Long startBefore = start - 1000l;
        StockIndexQuery indexQuery = new StockIndexQuery();
        indexQuery.setKbarDateFrom(startBefore.toString());
        indexQuery.setLimit(2000);
        indexQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockIndex> stockIndices = stockIndexService.listByCondition(indexQuery);
        return stockIndices;
    }

    public  List<StockIndex> getPreStockIndex(Long end){
        Long endDelay = end + 1000l;
        StockIndexQuery indexQuery = new StockIndexQuery();
        indexQuery.setKbarDateTo(endDelay.toString());
        indexQuery.setLimit(2000);
        indexQuery.addOrderBy("kbar_date", Sort.SortType.DESC);
        List<StockIndex> stockIndices = stockIndexService.listByCondition(indexQuery);
        return stockIndices;
    }

    public  List<StockIndex> getCalStockIndex(Long start,Long end){
        Long startBefore = start - 1000l;
        Long startDelay = end + 1000l;
        StockIndexQuery indexQuery = new StockIndexQuery();
        indexQuery.setKbarDateFrom(startBefore.toString());
        indexQuery.setKbarDateTo(startDelay.toString());
        indexQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockIndex> stockIndices = stockIndexService.listByCondition(indexQuery);
        return stockIndices;
    }
    public List<Long> getTradeDateSecondPoint(String tradeDate,List<Date> kbarSeconds){
        List<Long> list = Lists.newArrayList();
        for (Date datePoint:kbarSeconds){
            String datePointStr = DateUtil.format(datePoint, DateUtil.HHmmss);
            String dateStr = tradeDate + datePointStr;
            Long dateLong = Long.valueOf(dateStr);
            list.add(dateLong);
        }
        return list;
    }

    public List<MacdBuyDTO> calMacdBuyThree(){
        List<MacdBuyDTO> buys = Lists.newArrayList();
        List<MacdIndexDTO> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        stockKbarQuery.setLimit(100000);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        StockKbar preStockKbar = null;
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(6);
        LimitQueue<MacdIndexDTO> sellLimitQueue = new LimitQueue<>(1000);
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            i++;
            limitQueue.offer(stockKbar);
            if(i==1){
                MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                list.add(macdIndexDTO);
            }else{
                MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                list.add(macdIndexDTO);
            }
            if(i>=100){
                MacdIndexDTO preMacdIndexDTO = list.get(list.size() - 2);
                MacdIndexDTO macdIndexDTO = list.get(list.size() - 1);
                boolean haveBuy = true;
                if(buys.size()==0||buys.get(buys.size()-1).getSellPrice()!=null){
                    haveBuy = false;
                }
                if(haveBuy){
                    sellLimitQueue.offer(macdIndexDTO);
                    calSellInfo(sellLimitQueue,buys.get(buys.size()-1),stockKbar);
                }
                Integer redirect = null;
                if(macdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==1&&preMacdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==-1){
                    redirect = 1;
                }
                if(macdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==-1&&preMacdIndexDTO.getBar().compareTo(BigDecimal.ZERO)==1){
                    redirect = 0;
                }
                if(redirect!=null){
                    if(!stockKbar.getKbarDate().endsWith("150000")) {
                        MacdBuyDTO macdBuyDTO = new MacdBuyDTO();
                        macdBuyDTO.setStockCode("ICZL");
                        macdBuyDTO.setStockCode("中证主连");
                        macdBuyDTO.setPreBar(preMacdIndexDTO.getBar());
                        macdBuyDTO.setBar(macdIndexDTO.getBar());
                        macdBuyDTO.setBuyPrice(stockKbar.getClosePrice());
                        macdBuyDTO.setBuyTime(stockKbar.getKbarDate());
                        macdBuyDTO.setPreClosePrice(preStockKbar.getClosePrice());
                        macdBuyDTO.setBuyKbarAmount(stockKbar.getTradeAmount());
                        macdBuyDTO.setPreKbarAmount(preStockKbar.getTradeAmount());
                        macdBuyDTO.setRedirect(redirect);
                        BigDecimal raiseRate = getRaiseRate(limitQueue);
                        macdBuyDTO.setRaiseRateDay5(raiseRate);
                        buys.add(macdBuyDTO);
                        sellLimitQueue.clear();
                        sellLimitQueue.add(macdIndexDTO);
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        return buys;
    }


    public boolean calSellInfo(LimitQueue<MacdIndexDTO> limitQueue,MacdBuyDTO buyDTO,StockKbar stockKbar){
        Iterator<MacdIndexDTO> iterator = limitQueue.iterator();
        int i = 0;
        MacdIndexDTO buyMacd = null;
        MacdIndexDTO highMacd = null;
        MacdIndexDTO lowMacd = null;
        while (iterator.hasNext()){
            i++;
            MacdIndexDTO indexDTO = iterator.next();
            if(i==1){
                buyMacd = indexDTO;
            }
            if(buyDTO.getRedirect()==1) {
                if(highMacd==null||indexDTO.getBar().compareTo(highMacd.getBar())==1){
                    highMacd = indexDTO;
                }
                if (i == limitQueue.size()) {
                    BigDecimal absValue = highMacd.getBar().subtract(indexDTO.getBar());
                    BigDecimal percent = absValue.divide(highMacd.getBar(),3,BigDecimal.ROUND_HALF_UP);
                    boolean sellFlag = false;
                    if(absValue.compareTo(new BigDecimal("0.15"))==1&&percent.compareTo(new BigDecimal("0.15"))==1){
                        sellFlag = true;
                    }
                    if(sellFlag){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setHighBar(highMacd.getBar());
                        buyDTO.setDropPercent(percent);
                        buyDTO.setSellDropBuy(0);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if (indexDTO.getBar().compareTo(buyMacd.getBar())==-1){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(1);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(2);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                }
            }
            if(buyDTO.getRedirect()==0) {
                if(lowMacd==null||indexDTO.getBar().compareTo(lowMacd.getBar())==-1){
                    lowMacd = indexDTO;
                }
                if (i == limitQueue.size()) {
                    BigDecimal absValue = lowMacd.getBar().subtract(indexDTO.getBar());
                    BigDecimal percent = absValue.divide(lowMacd.getBar(),3,BigDecimal.ROUND_HALF_UP);
                    boolean sellFlag = false;
                    if(absValue.compareTo(new BigDecimal("-0.15"))==-1&&percent.compareTo(new BigDecimal("0.15"))==1){
                        sellFlag = true;
                    }
                    if(sellFlag){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setHighBar(lowMacd.getBar());
                        buyDTO.setDropPercent(percent);
                        buyDTO.setSellDropBuy(0);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if (indexDTO.getBar().compareTo(buyMacd.getBar())==1){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(1);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        buyDTO.setSellTime(stockKbar.getKbarDate());
                        buyDTO.setSellBar(indexDTO.getBar());
                        buyDTO.setSellPrice(stockKbar.getClosePrice());
                        buyDTO.setSellDropBuy(2);
                        buyDTO.setSellIntelTime(i-1);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<MacdBuyDTO> calMacdBuyTwo(){
        List<MacdBuyDTO> buys = Lists.newArrayList();
        List<MacdIndexDTO> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        stockKbarQuery.setLimit(100000);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        StockKbar preStockKbar = null;
        LimitQueue<StockKbar> limitQueue = new LimitQueue<>(6);
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            limitQueue.offer(stockKbar);
            if(stockKbar.getKbarDate().startsWith("20220425")){
                System.out.println(1);
            }
            i++;
            if(i==1){
                MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                list.add(macdIndexDTO);
            }else{
                MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                list.add(macdIndexDTO);
            }
            if(i>=100){
                MacdIndexDTO preMacdIndexDTO = list.get(list.size() - 2);
                MacdIndexDTO macdIndexDTO = list.get(list.size() - 1);
                boolean haveBuy = true;
                if(buys.size()==0||buys.get(buys.size()-1).getSellPrice()!=null){
                    haveBuy = false;
                }

                if(haveBuy){
                    MacdBuyDTO macdBuyDTO = buys.get(buys.size() - 1);
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                        macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                        haveBuy = false;
                    }else {
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                            if (macdBuyDTO.getRedirect() == 0) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                            if (macdBuyDTO.getRedirect() == 1) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                    }
                }

                if(!haveBuy&&!(stockKbar.getKbarDate().endsWith("150000"))&&!(stockKbar.getKbarDate().endsWith("093000"))) {
                    MacdBuyDTO macdBuyDTO = new MacdBuyDTO();
                    macdBuyDTO.setStockCode("ICZL");
                    macdBuyDTO.setStockCode("中证主连");
                    macdBuyDTO.setPreBar(preMacdIndexDTO.getBar());
                    macdBuyDTO.setBar(macdIndexDTO.getBar());
                    macdBuyDTO.setBuyPrice(stockKbar.getClosePrice());
                    macdBuyDTO.setBuyTime(stockKbar.getKbarDate());
                    macdBuyDTO.setPreClosePrice(preStockKbar.getClosePrice());
                    macdBuyDTO.setBuyKbarAmount(stockKbar.getTradeAmount());
                    macdBuyDTO.setPreKbarAmount(preStockKbar.getTradeAmount());
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                        macdBuyDTO.setRedirect(1);
                    }
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                        macdBuyDTO.setRedirect(0);
                    }
                    BigDecimal raiseRate = getRaiseRate(limitQueue);
                    macdBuyDTO.setRaiseRateDay5(raiseRate);
                    buys.add(macdBuyDTO);
                }

            }
            preStockKbar = stockKbar;
        }
        return buys;
    }

    public BigDecimal getRaiseRate(LimitQueue<StockKbar> limitQueue){
        Iterator<StockKbar> iterator = limitQueue.iterator();
        StockKbar first = null;
        StockKbar last = null;
        while(iterator.hasNext()){
            StockKbar next = iterator.next();
            if(first==null){
                first = next;
            }
            last = next;
        }
        BigDecimal pricePercentRate = PriceUtil.getPricePercentRate(last.getClosePrice().subtract(first.getClosePrice()), first.getClosePrice());
        return pricePercentRate;
    }


    public List<MacdBuyDTO> calMacdBuy(){
        List<MacdBuyDTO> buys = Lists.newArrayList();
        List<MacdIndexDTO> list = Lists.newArrayList();
        StockKbarQuery stockKbarQuery = new StockKbarQuery();
        stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        stockKbarQuery.setLimit(30000);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);
        StockKbar preStockKbar = null;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            i++;
            if(i==1){
                MacdIndexDTO macdIndexDTO = macdIndex(null, stockKbar, preStockKbar, 1);
                list.add(macdIndexDTO);
            }else{
                MacdIndexDTO macdIndexDTO = macdIndex(list.get(list.size() - 1), stockKbar, preStockKbar, i);
                list.add(macdIndexDTO);
            }
            if(i>=100){
                MacdIndexDTO preMacdIndexDTO = list.get(list.size() - 2);
                MacdIndexDTO macdIndexDTO = list.get(list.size() - 1);
                boolean haveBuy = true;
                if(buys.size()==0||buys.get(buys.size()-1).getSellPrice()!=null){
                    haveBuy = false;
                }

                if(haveBuy){
                    MacdBuyDTO macdBuyDTO = buys.get(buys.size() - 1);
                    if(stockKbar.getKbarDate().endsWith("150000")){
                        macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                        macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                        haveBuy = false;
                    }else {
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                            if (macdBuyDTO.getRedirect() == 0) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                        if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                            if (macdBuyDTO.getRedirect() == 1) {
                                macdBuyDTO.setSellPrice(stockKbar.getClosePrice());
                                macdBuyDTO.setSellTime(stockKbar.getKbarDate());
                                haveBuy = false;
                            }
                        }
                    }
                }

                if(!haveBuy&&!(stockKbar.getKbarDate().endsWith("150000"))&&!(stockKbar.getKbarDate().endsWith("093020"))) {
                    MacdBuyDTO macdBuyDTO = new MacdBuyDTO();
                    macdBuyDTO.setStockCode("ICZL");
                    macdBuyDTO.setStockCode("中证主连");
                    macdBuyDTO.setPreBar(preMacdIndexDTO.getBar());
                    macdBuyDTO.setBar(macdIndexDTO.getBar());
                    macdBuyDTO.setBuyPrice(stockKbar.getClosePrice());
                    macdBuyDTO.setBuyTime(stockKbar.getKbarDate());
                    macdBuyDTO.setPreClosePrice(preStockKbar.getClosePrice());
                    macdBuyDTO.setBuyKbarAmount(stockKbar.getTradeAmount());
                    macdBuyDTO.setPreKbarAmount(preStockKbar.getTradeAmount());
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == 1) {
                        macdBuyDTO.setRedirect(1);
                    }
                    if (macdIndexDTO.getBar().compareTo(preMacdIndexDTO.getBar()) == -1) {
                        macdBuyDTO.setRedirect(0);
                    }
                    buys.add(macdBuyDTO);
                }

            }
            preStockKbar = stockKbar;
        }
        return buys;
    }

    public MacdIndexDTO macdIndex(MacdIndexDTO preMacdIndexDTO,StockKbar stockKbar,StockKbar preStockKbar,int i){
        if(i==1){
            MacdIndexDTO macdIndexDTO = new MacdIndexDTO();
            macdIndexDTO.setStockCode(stockKbar.getStockCode());
            macdIndexDTO.setTradeDate(stockKbar.getKbarDate());
            macdIndexDTO.setDea(BigDecimal.ZERO);
            macdIndexDTO.setDiff(BigDecimal.ZERO);
            macdIndexDTO.setBar(BigDecimal.ZERO);
            return macdIndexDTO;
        } else if(i==2){
            BigDecimal ema12 = ((preStockKbar.getClosePrice().multiply(new BigDecimal(14))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(16),4,BigDecimal.ROUND_HALF_UP);
            BigDecimal ema26 = ((preStockKbar.getClosePrice().multiply(new BigDecimal(19))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(21),4,BigDecimal.ROUND_HALF_UP);
            BigDecimal diff = ema12.subtract(ema26).setScale(4,BigDecimal.ROUND_HALF_UP);
            BigDecimal dea = diff.multiply(new BigDecimal("0.2")).setScale(4,BigDecimal.ROUND_HALF_UP);
            BigDecimal bar = (diff.subtract(dea)).multiply(new BigDecimal(2)).setScale(4,BigDecimal.ROUND_HALF_UP);
            MacdIndexDTO macdIndexDTO = new MacdIndexDTO();
            macdIndexDTO.setStockCode(stockKbar.getStockCode());
            macdIndexDTO.setTradeDate(stockKbar.getKbarDate());
            macdIndexDTO.setEma12(ema12);
            macdIndexDTO.setEma26(ema26);
            macdIndexDTO.setDiff(diff);
            macdIndexDTO.setDea(dea);
            macdIndexDTO.setBar(bar);
            return macdIndexDTO;
        }else{
            BigDecimal ema12 = ((preMacdIndexDTO.getEma12().multiply(new BigDecimal(14))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(16),4,BigDecimal.ROUND_HALF_UP);
            BigDecimal ema26 = ((preMacdIndexDTO.getEma26().multiply(new BigDecimal(19))).add((stockKbar.getClosePrice().multiply(new BigDecimal(2))))).divide(new BigDecimal(21),4,BigDecimal.ROUND_HALF_UP);
            BigDecimal diff = ema12.subtract(ema26).setScale(4,BigDecimal.ROUND_HALF_UP);
            BigDecimal dea = ((preMacdIndexDTO.getDea().multiply(new BigDecimal(29))).add(diff.multiply(new BigDecimal(2)))).divide(new BigDecimal(31), 4, BigDecimal.ROUND_HALF_UP);
            BigDecimal bar = (diff.subtract(dea)).multiply(new BigDecimal(2)).setScale(4,BigDecimal.ROUND_HALF_UP);
            MacdIndexDTO macdIndexDTO = new MacdIndexDTO();
            macdIndexDTO.setStockCode(stockKbar.getStockCode());
            macdIndexDTO.setTradeDate(stockKbar.getKbarDate());
            macdIndexDTO.setEma12(ema12);
            macdIndexDTO.setEma26(ema26);
            macdIndexDTO.setDiff(diff);
            macdIndexDTO.setDea(dea);
            macdIndexDTO.setBar(bar);
            return  macdIndexDTO;
        }
    }

    public void huShen300QuoteToKbar(){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            System.out.println(yyyyMMdd);
            /*if(!yyyyMMdd.equals("20221125")){
                continue;
            }*/
            ThsQuoteInfoQuery thsQuoteInfoQuery = new ThsQuoteInfoQuery();
            thsQuoteInfoQuery.setQuoteDate(yyyyMMdd);
            thsQuoteInfoQuery.setStockCode("IFZLCFE");
            thsQuoteInfoQuery.addOrderBy("time_stamp", Sort.SortType.ASC);
            thsQuoteInfoQuery.setLimit(40000);
            List<ThsQuoteInfo> thsQuoteInfos = thsQuoteInfoService.listByCondition(thsQuoteInfoQuery);
            if (CollectionUtils.isEmpty(thsQuoteInfos)) {
                continue;
            }
            calSecondKbar(thsQuoteInfos, 15, yyyyMMdd);

        }
    }

    public void calSecondKbar(List<ThsQuoteInfo> quotes,int seconds,String tradeDate){
        List<Date> kbarSeconds = getKbarSeconds(seconds);
        Date preDatePoint = DateUtil.parseDate("092859",DateUtil.HHmmss);
        for (Date datePoint:kbarSeconds){
            String datePointStr = DateUtil.format(datePoint, DateUtil.HHmmss);
            Date date = DateUtil.parseDate(tradeDate + datePointStr + "000", DateUtil.yyyyMMddHHmmssSSS);
            ThsQuoteInfo preEndQuoteInfo = null;
            BigDecimal start = null;
            BigDecimal end = null;
            BigDecimal high = null;
            BigDecimal low = null;
            long quantity = 0l;
            BigDecimal amount = BigDecimal.ZERO;
            for (ThsQuoteInfo quoteInfo:quotes){
                Long timeStamp = quoteInfo.getTimeStamp();
                String quoteTime = quoteInfo.getQuoteTime();
                if(quoteTime.equals("150000")){
                    timeStamp = timeStamp-1000l;
                }
                long time = date.getTime();
                if(timeStamp<date.getTime()&&timeStamp>=preDatePoint.getTime()){
                    if(high==null||quoteInfo.getCurrentPrice().compareTo(high)==1){
                        high = quoteInfo.getCurrentPrice();
                    }
                    if(low==null||quoteInfo.getCurrentPrice().compareTo(low)==-1){
                        low = quoteInfo.getCurrentPrice();
                    }
                    if(start==null){
                        start = quoteInfo.getCurrentPrice();
                    }
                    end = quoteInfo.getCurrentPrice();
                    if(quoteInfo.getVol()!=null) {
                        if(quoteInfo.getVol()<0){
                            quantity =quantity- quoteInfo.getVol();
                        }else {
                            quantity = quoteInfo.getVol() + quantity;
                        }
                    }
                    if(quoteInfo.getAmt()!=null) {
                        if(quoteInfo.getAmt().compareTo(BigDecimal.ZERO)==-1){
                            amount = amount.subtract(quoteInfo.getAmt());
                        }else {
                            amount = amount.add(quoteInfo.getAmt());
                        }
                    }
                }else if(timeStamp<preDatePoint.getTime()) {
                    preEndQuoteInfo = quoteInfo;
                }
            }
            preDatePoint = date;
            if(start==null){
                start = preEndQuoteInfo.getCurrentPrice();
                end = preEndQuoteInfo.getCurrentPrice();
                high = preEndQuoteInfo.getCurrentPrice();
                low = preEndQuoteInfo.getCurrentPrice();
            }
            StockKbar stockKbar = new StockKbar();
            stockKbar.setStockCode("IFZLCFE");
            stockKbar.setStockName("沪深300期货");
            stockKbar.setKbarDate(tradeDate+datePointStr);
            stockKbar.setUniqueKey(stockKbar.getStockCode()+"_"+stockKbar.getKbarDate());
            stockKbar.setOpenPrice(start);
            stockKbar.setClosePrice(end);
            stockKbar.setHighPrice(high);
            stockKbar.setLowPrice(low);
            stockKbar.setTradeQuantity(quantity);
            stockKbar.setTradeAmount(amount);
            stockKbar.setCreateTime(new Date());
            stockKbarService.save(stockKbar);
        }
    }


    public void saveGatherKbar(List<ThsQuoteInfo> quotes){
        ThsQuoteInfo thsQuoteInfo = quotes.get(0);
        if(thsQuoteInfo.getQuoteTime().equals("092500")){
            StockKbar stockKbar = new StockKbar();
            stockKbar.setStockCode("127046");
            stockKbar.setStockName("中证500期货");
            stockKbar.setKbarDate("20211110"+"093000");
            stockKbar.setUniqueKey(stockKbar.getStockCode()+"_"+stockKbar.getKbarDate());
            stockKbar.setOpenPrice(thsQuoteInfo.getCurrentPrice());
            stockKbar.setClosePrice(thsQuoteInfo.getCurrentPrice());
            stockKbar.setHighPrice(thsQuoteInfo.getCurrentPrice());
            stockKbar.setLowPrice(thsQuoteInfo.getCurrentPrice());

            stockKbar.setTradeQuantity(thsQuoteInfo.getVol());
            stockKbar.setTradeAmount(thsQuoteInfo.getAmt());
            stockKbar.setCreateTime(new Date());
            stockKbarService.save(stockKbar);
        }
    }

    public  BigDecimal getPricePercentRate(BigDecimal price, BigDecimal basePrice) {
        return price.divide(basePrice,6, RoundingMode.HALF_UP).multiply(CommonConstant.DECIMAL_HUNDRED);
    }

    public static Date timeAddSecond(String start,int seconds){
        Date startDate = DateUtil.parseDate(start, DateUtil.HHmmss);
        Date time113000 = DateUtil.parseDate("113000", DateUtil.HHmmss);
        Date time130000 = DateUtil.parseDate("130000", DateUtil.HHmmss);
        for (int i = 1;i<=seconds;i++){
            String startDateStr = DateUtil.format(startDate, DateUtil.HHmmss);
            if(startDateStr.equals("113000")){
                startDate = time130000;
            }
            startDate = DateUtil.addSeconds(startDate, 1);
        }
        return startDate;
    }

    public List<Date> getKbarSeconds(int seconds) {
        List<Date> list = Lists.newArrayList();
        Date date = DateUtil.parseDate("093000", DateUtil.HHmmss);
        for (int i=0;i<=4000;i++) {
            String timeStamp = DateUtil.format(date, DateUtil.HHmmss);
            date = timeAddSecond(timeStamp, seconds);
            String format = DateUtil.format(date, DateUtil.HHmmss);
            if(date.after(DateUtil.parseDate("150000", DateUtil.HHmmss))){
                break;
            }
            if(format.equals("113000")){
                date = DateUtil.parseDate("130000",DateUtil.HHmmss);
            }
            Date datePoint = date;
            list.add(datePoint);

        }
        return list;
    }

}
