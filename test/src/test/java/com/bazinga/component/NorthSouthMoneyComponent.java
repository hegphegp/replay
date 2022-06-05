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

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void northMoney(String stockCode){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        boolean flag = false;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd);
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            if(format.equals("2022-06-02")){
                flag  = true;
            }
            if(format.equals("2022-06-06")){
                flag  = false;
            }
            if(flag){
                StockKbar bar0940 = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd + " 094000");
                StockKbar bar0950 = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd + " 095000");
                StockKbar bar1000 = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd + " 100000");
                StockKbar bar1010 = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd + " 101000");
                StockKbar bar1020 = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd + " 102000");
                StockKbar bar1030 = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd + " 103000");



            }
        }

    }
    public void indexPercent(String stockCode){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        boolean flag = false;
        StockKbar preStockKbar = null;
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            StockKbar stockKbar = stockKbarService.getByUniqueKey(stockCode + "_" + yyyyMMdd);
            if(yyyyMMdd.equals("20220602")){
                flag  = true;
            }
            if(yyyyMMdd.equals("20220606")){
                flag  = false;
            }
            if(flag&&preStockKbar!=null&&stockKbar!=null){
                List<ThirdSecondTransactionDataDTO> datas = historyTransactionDataComponent.getData(stockCode, yyyyMMdd);
                BigDecimal totalAmount = BigDecimal.ZERO;
                for (ThirdSecondTransactionDataDTO data:datas){
                    totalAmount = totalAmount.add(new BigDecimal(data.getTradeQuantity()));
                    if(data.getTradeTime().equals("0940")){

                    }
                }
            }
            preStockKbar = stockKbar;
        }

    }
}
