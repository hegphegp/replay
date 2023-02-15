package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.MarketMoneyDTO;
import com.bazinga.replay.dto.StockFactorLevelTestDTO;
import com.bazinga.replay.dto.StrongerStockTestDTO;
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
public class StrongerStockThanSzInfoComponent {
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
    private ThsStockKbarComponent thsStockKbarComponent;
    @Autowired
    private IndexDetailComponent indexDetailComponent;
    @Autowired
    private StockKbarService stockKbarService;
    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();
    public static final ExecutorService THREAD_POOL_QUOTE_FACTOR = ThreadPoolUtils.create(4, 32, 512, "QuoteThreadPool");

    public static String leveStockCode = "600896,600555,600385,600090,300202,000673,300312,600870,300038,300367,600146,000613,300023,002464,300325,000611,000502,002447,002684,300064,002147,002770,600093,600275,002473,600209,600856,300178,002618,600652,600890,600091,002260,000687,600291,600695,603157,000585,603996,000835,600145,002619,000780,600723,600068,300362,002359,000760,600634,600614,002711,600485,002450,600891,002071,600701,000662,600247,600978,600677,600086,600687,600317";
    //public static String leveStockCode = "1111";


    public void strongerStockTest(){
        List<StrongerStockTestDTO> buys = getStrongerStock();
        List<Object[]> datas = Lists.newArrayList();
        for (StrongerStockTestDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.getMarketValue());
            list.add(dto.getBuyRate());
            list.add(dto.getSellDate());
            list.add(dto.getStockEndRates().get(0));
            list.add(dto.getStockEndRates().get(1));
            list.add(dto.getStockEndRates().get(2));
            list.add(dto.getSzEndRates().get(0));
            list.add(dto.getSzEndRates().get(1));
            list.add(dto.getSzEndRates().get(2));
            list.add(dto.getProfit());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","股票代码","股票名称","交易日期","市值","买入时涨幅","卖出日期","股票1日收盘涨幅","股票2日收盘涨幅","股票3日收盘涨幅","上证1日收盘涨幅","上证2日收盘涨幅","上证3日收盘涨幅","盈利"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("美国往事",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("强势股票数据");
        }catch (Exception e){
            log.info(e.getMessage());
        }

    }


    public List<StrongerStockTestDTO> getStrongerStock(){
        List<StrongerStockTestDTO> list = Lists.newArrayList();
        Map<String, String> usemap = new HashMap<>();
        Map<String, BigDecimal> szEndRateMap = getSzEndRateMap();
        List<TradeDatePool> tradeDatePools = getTradeDatePools();
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String dateyyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            List<ThsCirculateInfo> marketACirculate = thsCirculateInfoComponent.getMarketACirculate(dateyyyyMMdd);
            for (ThsCirculateInfo circulateInfo:marketACirculate){
                if(usemap.get(circulateInfo.getStockCode())!=null){
                    continue;
                }
                usemap.put(circulateInfo.getStockCode(),circulateInfo.getStockCode());
                List<ThsStockKbar> stockKbars = thsStockKbarComponent.getStockKBarsDeleteNewDays(circulateInfo.getStockCode());
                if(CollectionUtils.isEmpty(stockKbars)){
                    continue;
                }
                List<StrongerStockTestDTO> buys = calStrongerInfos(circulateInfo, stockKbars, szEndRateMap, "000300");
                if(CollectionUtils.isEmpty(buys)){
                    continue;
                }
                list.addAll(buys);
            }
        }
        return list;

    }
    public List<StrongerStockTestDTO> calStrongerInfos(ThsCirculateInfo circulateInfo,List<ThsStockKbar> stockKbars,Map<String,BigDecimal> szEndRateMap,String indexCode){
        List<StrongerStockTestDTO> list = Lists.newArrayList();
        LimitQueue<ThsStockKbar> limitQueue = new LimitQueue<>(4);
        for (ThsStockKbar stockKbar:stockKbars){
            limitQueue.offer(stockKbar);
            if(DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd).before(DateUtil.parseDate("20230101", DateUtil.yyyyMMdd))){
                continue;
            }
            if(!DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd).before(DateUtil.parseDate("20230215", DateUtil.yyyyMMdd))){
                continue;
            }
            String dateyyyy_MM_dd = DateUtil.dateStringFormat(stockKbar.getKbarDate(), DateUtil.yyyyMMdd, DateUtil.yyyy_MM_dd);
            IndexDetail indexDetailUK = indexDetailComponent.getIndexDetailUK(dateyyyy_MM_dd, circulateInfo.getStockCode(), indexCode);
            if(indexDetailUK==null){
                continue;
            }
            StrongerStockTestDTO buyDto = new StrongerStockTestDTO();
            buyDto.setStockCode(stockKbar.getStockCode());
            buyDto.setStockName(circulateInfo.getStockName());
            buyDto.setTradeDate(stockKbar.getKbarDate());
            buyDto.setMarketValue(stockKbar.getMarketValue());
            boolean buyFlag = judgeStrongerStock(limitQueue, 4, buyDto, szEndRateMap);
            if(buyFlag){
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getOpenPrice().subtract(stockKbar.getZeroPrice()), stockKbar.getZeroPrice());
                buyDto.setBuyRate(openRate);
                BigDecimal profit = calProfit(buyDto, stockKbars, szEndRateMap);
                buyDto.setProfit(profit);
                list.add(buyDto);
            }
        }
        return list;
    }

    public boolean judgeStrongerStock(LimitQueue<ThsStockKbar> limitQueue,int limitQueueSize,StrongerStockTestDTO buyDto,Map<String,BigDecimal> szEndRateMap){
        if(limitQueue.size()<limitQueueSize){
            return false;
        }
        List<ThsStockKbar> thsStockKbars = limitQueueToList(limitQueue);
        int i=0;
        for (ThsStockKbar thsStockKbar:thsStockKbars){
            i++;
            BigDecimal szRate = szEndRateMap.get(thsStockKbar.getKbarDate());
            BigDecimal endRate = PriceUtil.getPricePercentRate(thsStockKbar.getClosePrice().subtract(thsStockKbar.getZeroPrice()), thsStockKbar.getZeroPrice());
            if(i==limitQueueSize){
                return true;
            }else{
                buyDto.getSzEndRates().add(szRate);
                buyDto.getStockEndRates().add(endRate);
            }
            if(endRate.compareTo(szRate)<=0){
                return false;
            }
        }
        return true;
    }

    public BigDecimal calProfit(StrongerStockTestDTO buyDto,List<ThsStockKbar> thsStockKbars,Map<String,BigDecimal> szEndRateMap){
        ThsStockKbar buyStockKbar = null;
        boolean flag = false;
        int i = 0;
        for (ThsStockKbar stockKbar:thsStockKbars){
            if(stockKbar.getKbarDate().equals(buyDto.getTradeDate())){
                flag = true;
                buyStockKbar = stockKbar;
            }
            if(!flag){
                continue;
            }
            BigDecimal szEndRate = szEndRateMap.get(stockKbar.getKbarDate());
            BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(stockKbar.getZeroPrice()), stockKbar.getZeroPrice());
            if(endRate.compareTo(szEndRate)<0){
                i++;
            }else{
                i=0;
            }
            if(i==3){
                BigDecimal profit = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(buyStockKbar.getOpenPrice()), buyStockKbar.getOpenPrice());
                if(buyStockKbar.getAdjFactor()!=null&&buyStockKbar.getAdjFactor().compareTo(stockKbar.getAdjFactor())!=0){
                    profit = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyStockKbar.getAdjOpenPrice()), buyStockKbar.getAdjOpenPrice());
                }
                buyDto.setSellDate(stockKbar.getKbarDate());
                return profit;
            }
            if(i>3){
                return null;
            }
        }
        return null;
    }

    public Map<String,BigDecimal> getSzEndRateMap(){
        Map<String, BigDecimal> map = new HashMap<>();
        StockKbarQuery query = new StockKbarQuery();
        query.setKbarDate("999999");
        query.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(query);
        StockKbar preStockKbar = null;
        for (StockKbar stockKbar:stockKbars){
            if(preStockKbar!=null){
                BigDecimal endRate = PriceUtil.getPricePercentRate(stockKbar.getClosePrice().subtract(preStockKbar.getClosePrice()), preStockKbar.getClosePrice());
                map.put(stockKbar.getKbarDate(),endRate);
            }
            preStockKbar = stockKbar;
        }
        return map;
    }

    public List<TradeDatePool> getTradeDatePools(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }

    public List<ThsStockKbar> limitQueueToList(LimitQueue<ThsStockKbar> limitQueue){
        if(limitQueue.size()<2){
            return null;
        }
        List<ThsStockKbar> list = Lists.newArrayList();
        Iterator<ThsStockKbar> iterator = limitQueue.iterator();
        while (iterator.hasNext()){
            ThsStockKbar next = iterator.next();
            list.add(next);
        }
        return list;
    }





}
