package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.IndexDValueBuyDTO;
import com.bazinga.dto.IndexDValueDTO;
import com.bazinga.dto.MacdBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.MacdIndexDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.OtherIndexInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.model.TradeDatePool;
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
public class NorthSouthMoneyComponent {
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
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private OtherIndexInfoService otherIndexInfoService;

    public static List<String> timeStamps = Lists.newArrayList("094000","095000","100000","101000","102000","103000");
    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void northMoney(String stockCode,String stockName){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        boolean flag = false;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            if(format.equals("2018-01-02")){
                flag  = true;
            }
            if(format.equals("2022-06-06")){
                flag  = false;
            }
            if(flag){
                for (String stamp:timeStamps){
                    StockKbar bar0940 = stockKbarService.getByUniqueKey("188888" + "_" + yyyyMMdd + ""+stamp);
                    StockKbar sbar0940 = stockKbarService.getByUniqueKey("188889" + "_" + yyyyMMdd + ""+stamp);
                    BigDecimal amount0940 = BigDecimal.ZERO;
                    if(bar0940!=null&&bar0940.getTradeAmount()!=null){
                        amount0940 = amount0940.add(bar0940.getTradeAmount());
                    }
                    if(sbar0940!=null&&sbar0940.getTradeAmount()!=null){
                        amount0940 = amount0940.add(sbar0940.getTradeAmount());
                    }
                    OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                    otherIndexInfo.setIndexCode("1888888");
                    otherIndexInfo.setIndexName(stockName);
                    otherIndexInfo.setTradeDate(yyyyMMdd);
                    otherIndexInfo.setTimeStamp(stamp);
                    otherIndexInfo.setIndexValue(amount0940);
                    otherIndexInfoService.save(otherIndexInfo);
                }

            }
        }

    }
    public void indexPercent(String stockCode,String stockName){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        boolean flag = false;
        StockKbar preStockKbar = null;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            StockKbar stockKbar = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd);
            if(yyyyMMdd.equals("20180102")){
                flag  = true;
            }
            if(yyyyMMdd.equals("20220606")){
                flag  = false;
            }
            List<String> times = Lists.newArrayList("09:40", "09:50", "10:00", "10:10", "10:20", "10:30");
            if(flag&&preStockKbar!=null&&stockKbar!=null){
                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, yyyyMMdd);
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (ThirdSecondTransactionDataDTO data:datas){
                    BigDecimal percent = totalAmount.divide(preStockKbar.getTradeAmount(), 2, BigDecimal.ROUND_HALF_UP);
                    if(data.getTradeTime().equals("09:40")&&times.contains("09:40")){
                        OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                        otherIndexInfo.setIndexCode(stockCode);
                        otherIndexInfo.setIndexName(stockName);
                        otherIndexInfo.setTradeDate(yyyyMMdd);
                        otherIndexInfo.setTimeStamp("094000");
                        otherIndexInfo.setIndexValue(percent);
                        otherIndexInfoService.save(otherIndexInfo);
                        times.remove("09:40");
                    }
                    if(data.getTradeTime().equals("09:50")&&times.contains("09:50")){
                        OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                        otherIndexInfo.setIndexCode(stockCode);
                        otherIndexInfo.setIndexName(stockName);
                        otherIndexInfo.setTradeDate(yyyyMMdd);
                        otherIndexInfo.setTimeStamp("095000");
                        otherIndexInfo.setIndexValue(percent);
                        otherIndexInfoService.save(otherIndexInfo);
                        times.remove("09:50");
                    }
                    if(data.getTradeTime().equals("10:00")&&times.contains("10:00")){
                        OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                        otherIndexInfo.setIndexCode(stockCode);
                        otherIndexInfo.setIndexName(stockName);
                        otherIndexInfo.setTradeDate(yyyyMMdd);
                        otherIndexInfo.setTimeStamp("100000");
                        otherIndexInfo.setIndexValue(percent);
                        otherIndexInfoService.save(otherIndexInfo);
                        times.remove("10:00");
                    }
                    if(data.getTradeTime().equals("10:10")&&times.contains("10:10")){
                        OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                        otherIndexInfo.setIndexCode(stockCode);
                        otherIndexInfo.setIndexName(stockName);
                        otherIndexInfo.setTradeDate(yyyyMMdd);
                        otherIndexInfo.setTimeStamp("101000");
                        otherIndexInfo.setIndexValue(percent);
                        otherIndexInfoService.save(otherIndexInfo);
                        times.remove("10:10");
                    }
                    if(data.getTradeTime().equals("10:20")&&times.contains("10:20")){
                        OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                        otherIndexInfo.setIndexCode(stockCode);
                        otherIndexInfo.setIndexName(stockName);
                        otherIndexInfo.setTradeDate(yyyyMMdd);
                        otherIndexInfo.setTimeStamp("102000");
                        otherIndexInfo.setIndexValue(percent);
                        otherIndexInfoService.save(otherIndexInfo);
                        times.remove("10:20");
                    }
                    if(data.getTradeTime().equals("10:30")&&times.contains("10:30")){
                        OtherIndexInfo otherIndexInfo = new OtherIndexInfo();
                        otherIndexInfo.setIndexCode(stockCode);
                        otherIndexInfo.setIndexName(stockName);
                        otherIndexInfo.setTradeDate(yyyyMMdd);
                        otherIndexInfo.setTimeStamp("103000");
                        otherIndexInfo.setIndexValue(percent);
                        otherIndexInfoService.save(otherIndexInfo);
                        times.remove("10:30");
                    }
                    totalAmount = totalAmount.add(new BigDecimal(data.getTradeQuantity()*100));

                }
            }
            preStockKbar = stockKbar;
        }

    }
}
