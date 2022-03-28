package com.bazinga.replay.component;


import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockBolling;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.CommonUtil;
import com.bazinga.util.DateFormatUtils;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class StockBollingComponent {


    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private StockAverageLineService stockAverageLineService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;


    public void  batchInitBoll(){
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo : circulateInfos) {
            initBoll(circulateInfo.getStockCode());
        }
    }

    private void initBoll(String stockCode){
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(new TradeDatePoolQuery());
        for (TradeDatePool tradeDatePool : tradeDatePools) {
            String kbarDate = DateFormatUtils.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            String uniqueKey = stockCode + SymbolConstants.UNDERLINE + kbarDate +SymbolConstants.UNDERLINE + 20;
            StockAverageLine byUniqueKey = stockAverageLineService.getByUniqueKey(uniqueKey);
            if(byUniqueKey!=null){
                BigDecimal std = calStd(stockCode, kbarDate, 20);
                if(std.compareTo(BigDecimal.ZERO) <0){
                    continue;
                }
                log.info("stockCode{} std{}",stockCode,std);
                StockBolling stockBolling = new StockBolling();

            }

        }




    }

    private BigDecimal calStd(String stockCode, String kbarDate , int days){
        StockAverageLineQuery query = new StockAverageLineQuery();
        query.addOrderBy("kbar_date", Sort.SortType.DESC);
        query.setStockCode(stockCode);
        query.setKbarDateTo(kbarDate);
        query.setDayType(days);
        query.addOrderBy("kbar_date", Sort.SortType.DESC);
        query.setLimit(days);
        List<StockAverageLine> stockAverageLines = stockAverageLineService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockAverageLines) || stockAverageLines.size() <20){
            return new BigDecimal("-1");
        }
        BigDecimal avgPrice = stockAverageLines.stream().map(StockAverageLine::getAveragePrice).reduce(BigDecimal::add).get()
                .divide(new BigDecimal(String.valueOf(days)),2,BigDecimal.ROUND_HALF_UP);

        BigDecimal total = BigDecimal.ZERO;
        for (StockAverageLine stockAverageLine : stockAverageLines) {
            total = total.add(stockAverageLine.getAveragePrice().subtract(avgPrice).pow(2));
        }
        return CommonUtil.sqrt(total.divide(new BigDecimal(String.valueOf(days)),2,BigDecimal.ROUND_HALF_UP),2);
    }


}
