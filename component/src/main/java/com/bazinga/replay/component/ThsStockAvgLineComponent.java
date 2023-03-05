package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ThsStockKbarQuery;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.ThsStockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import groovy.transform.ASTTest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

@Component
@Slf4j
public class ThsStockAvgLineComponent {

    @Autowired
    private ThsStockKbarService thsStockKbarService;
    @Autowired
    private ThsStockKbarComponent thsStockKbarComponent;
    @Autowired
    private ThsCirculateInfoComponent thsCirculateInfoComponent;
    @Autowired
    private TradeDatePoolComponent tradeDatePoolComponent;
    @Autowired
    private StockAverageLineService stockAverageLineService;

    public void calAvgLine(){
        List<String> useList = Lists.newArrayList();
        List<TradeDatePool> tradeDatePools = tradeDatePoolComponent.getTradeDatePools("20180101", "20230301");
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            System.out.println(yyyyMMdd);
            List<ThsCirculateInfo> circulates = thsCirculateInfoComponent.getMarketACirculate(yyyyMMdd);
            for (ThsCirculateInfo circulateInfo:circulates){
                if(useList.contains(circulateInfo.getStockCode())){
                    continue;
                }
                calStockAvgLine(circulateInfo.getStockCode(),3);
                useList.add(circulateInfo.getStockCode());
            }
        }
    }

    public void calStockAvgLine(String stockCode,int days){
        List<ThsStockKbar> allStockKBars = thsStockKbarComponent.getAllStockKBars(stockCode);
        if(CollectionUtils.isEmpty(allStockKBars)){
            return;
        }
        LimitQueue<ThsStockKbar> limitQueue = new LimitQueue<>(days);
        for (ThsStockKbar thsStockKbar:allStockKBars){
            limitQueue.offer(thsStockKbar);
            if(limitQueue.size()<days){
                continue;
            }
            BigDecimal avgPrice = calAvgPrice(limitQueue);
            String uniqueKey = stockCode + "_" + thsStockKbar.getKbarDate() + "_" + days;
            StockAverageLine line = stockAverageLineService.getByUniqueKey(uniqueKey);
            if(line==null) {
                saveStockAvgLine(stockCode, thsStockKbar.getStockName(), thsStockKbar.getKbarDate(), avgPrice, days);
            }
        }
    }
    public void saveStockAvgLine(String stockCode,String stockName,String kbarDate,BigDecimal avgPrice,int days){
        StockAverageLine stockAverageLine = new StockAverageLine();
        stockAverageLine.setStockCode(stockCode);
        stockAverageLine.setStockName(stockName);
        stockAverageLine.setKbarDate(kbarDate);
        stockAverageLine.setUniqueKey(stockCode+"_"+kbarDate+"_"+days);
        stockAverageLine.setAveragePrice(avgPrice);
        stockAverageLine.setDayType(days);
        stockAverageLine.setCreateTime(new Date());
        stockAverageLineService.save(stockAverageLine);
    }

    public BigDecimal calAvgPrice(LimitQueue<ThsStockKbar> limitQueue){
        if(limitQueue.size()==0){
            return null;
        }
        Iterator<ThsStockKbar> iterator = limitQueue.iterator();
        BigDecimal total = BigDecimal.ZERO;
        while (iterator.hasNext()){
            ThsStockKbar thsStockKbar = iterator.next();
            total = total.add(thsStockKbar.getAdjClosePrice());
        }
        BigDecimal avgPrice = total.divide(new BigDecimal(limitQueue.size()), 2, BigDecimal.ROUND_HALF_UP);
        return avgPrice;
    }



}
