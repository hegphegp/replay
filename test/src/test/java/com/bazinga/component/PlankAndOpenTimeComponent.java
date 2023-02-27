package com.bazinga.component;


import com.bazinga.base.Sort;
import com.bazinga.dto.ReplayFenBanDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CirculateInfoComponent;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.dto.DayPlankAndOpenDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
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
public class PlankAndOpenTimeComponent {
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
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

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void plankAndOpenTime(){
        Map<String, DayPlankAndOpenDTO> buysMap = stockReplayDaily();
        List<Object[]> datas = Lists.newArrayList();

        for (String dateStr:buysMap.keySet()) {
            DayPlankAndOpenDTO dto = buysMap.get(dateStr);
            List<Object> list = new ArrayList<>();
            list.add(dto.getTradeDate());
            list.add(dto.getTradeDate());
            list.add(dto.getPlankStocks());
            list.add(dto.getOpenTimes());
            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","交易日期","当天板票数量","当天总开板数量"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("上板开板统计",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("上板开板统计");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public Map<String, DayPlankAndOpenDTO> stockReplayDaily(){
        Map<String, DayPlankAndOpenDTO> map = new HashMap<>();
        List<ReplayFenBanDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        int count  = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            count++;
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            /*if(!stockCode.equals("002877")){
                continue;
            }*/
            System.out.println(stockCode+"------"+count);
            List<StockKbar> stockKBars = getStockKBars(stockCode);
            if (CollectionUtils.isEmpty(stockKBars)) {
                log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                continue;
            }
            StockKbar preStockKbar = null;
            for (StockKbar stockKbar:stockKBars){
                Date date = DateUtil.parseDate(stockKbar.getKbarDate(), DateUtil.yyyyMMdd);
                if(!date.before(DateUtil.parseDate("20210501",DateUtil.yyyyMMdd))) {
                    if (preStockKbar != null) {
                        boolean highPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), stockKbar.getHighPrice(), preStockKbar.getClosePrice(), preStockKbar.getKbarDate());
                        if (highPlank) {
                            Integer openTime = thirdSecondTransactionDataPlank(preStockKbar.getClosePrice(), stockKbar.getStockCode(), stockKbar.getKbarDate());
                            if (openTime != null) {
                                DayPlankAndOpenDTO dayPlankAndOpenDTO = map.get(stockKbar.getKbarDate());
                                if (dayPlankAndOpenDTO == null) {
                                    dayPlankAndOpenDTO = new DayPlankAndOpenDTO();
                                    dayPlankAndOpenDTO.setTradeDate(stockKbar.getKbarDate());
                                    map.put(stockKbar.getKbarDate(), dayPlankAndOpenDTO);
                                }
                                dayPlankAndOpenDTO.setOpenTimes(dayPlankAndOpenDTO.getOpenTimes() + openTime);
                                dayPlankAndOpenDTO.setPlankStocks(dayPlankAndOpenDTO.getPlankStocks() + 1);
                            }
                        }
                    }
                }
                preStockKbar = stockKbar;
            }
        }
        return map;
    }




    /**
     *
     */
    public Integer thirdSecondTransactionDataPlank(BigDecimal yesterdayPrice,String stockCode,String tradeDate){
        try {
            List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(stockCode, tradeDate);
            String plankTime = null;
            int  openTime= 0;
            boolean preIsPlank =false;
            if(CollectionUtils.isEmpty(list)){
                log.error("查询不到当日分时成交数据计算上板时间 stockCode:{}",stockCode);
                return null;
            }
            for (ThirdSecondTransactionDataDTO dto:list){
                BigDecimal tradePrice = dto.getTradePrice();
                Integer tradeType = dto.getTradeType();
                boolean isUpperPrice = PriceUtil.isUpperPrice(stockCode,tradePrice,yesterdayPrice);
                boolean isSell = false;
                if(tradeType==null){
                    continue;
                }
                if(tradeType!=null && tradeType!=0){
                    isSell = true;
                }
                if(isSell&&isUpperPrice){
                    if(plankTime==null) {
                        plankTime = dto.getTradeTime();
                    }
                    preIsPlank = true;
                }else{
                    if(preIsPlank){
                        openTime++;
                    }
                    preIsPlank = false;
                }
            }
            if(plankTime==null){
                return null;
            }
            return openTime;
        }catch (Exception e){
            log.error("分时成交统计数据查询分时数据异常 stockCode:{}",stockCode);
        }
        return 0;
    }

    public List<StockKbar> getStockKBars(String stockCode){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(stockCode);
        kbarQuery.setKbarDateFrom("20201101");
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        List<StockKbar> stockKbarDeleteNew = commonComponent.deleteNewStockTimes(stockKbars, 1000);
        return stockKbarDeleteNew;
    }


}
