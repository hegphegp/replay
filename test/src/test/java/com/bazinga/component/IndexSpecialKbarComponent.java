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
public class IndexSpecialKbarComponent {
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

    public void getSecondKbar(int second,String stockCode){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateFrom(DateUtil.parseDate("20180101",DateUtil.yyyyMMdd));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        for (TradeDatePool tradeDatePool:tradeDatePools){
            String yyyyMMdd = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            System.out.println(yyyyMMdd+"___计算"+second+"sk线ing");
            ThsQuoteInfoQuery thsQuoteInfoQuery = new ThsQuoteInfoQuery();
            thsQuoteInfoQuery.setQuoteDate(yyyyMMdd);
            thsQuoteInfoQuery.setStockCode(stockCode);
            thsQuoteInfoQuery.addOrderBy("time_stamp", Sort.SortType.ASC);
            thsQuoteInfoQuery.setLimit(40000);
            List<ThsQuoteInfo> thsQuoteInfos = thsQuoteInfoService.listByCondition(thsQuoteInfoQuery);
            if (CollectionUtils.isEmpty(thsQuoteInfos)) {
                continue;
            }
            calSecondKbar(thsQuoteInfos, second, yyyyMMdd);

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
