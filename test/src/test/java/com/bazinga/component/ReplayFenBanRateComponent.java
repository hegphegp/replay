package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.HuShen300MacdBuyDTO;
import com.bazinga.dto.MacdBuyDTO;
import com.bazinga.dto.ReplayFenBanDTO;
import com.bazinga.dto.TenDayExplorDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.*;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.MacdIndexDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;

/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ReplayFenBanRateComponent {
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

    public void plankFenBan(){
        List<ReplayFenBanDTO> buys = stockReplayDaily();
        List<Object[]> datas = Lists.newArrayList();

        for (ReplayFenBanDTO dto:buys) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getTradeDate());
            list.add(dto.isEndPlank());
            list.add(dto.getPlanks());
            list.add(dto.getCloseRate());
            list.add(dto.getBeforeRate3());
            list.add(dto.getBeforeRate5());
            list.add(dto.getBeforeRate10());
            list.add(dto.getNextOpenRate());
            list.add(dto.getNextCloseRate());
            list.add(dto.getNextHighRate());
            list.add(dto.getNextLowRate());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","股票代码","股票名称","交易日期","尾盘是否封住","板高","收盘涨幅","3天涨幅","5天涨幅","10天涨幅","次日开盘涨幅","次日收盘涨幅","次日最高涨幅","次日最低涨幅"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("macd买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("macd买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public List<ReplayFenBanDTO> stockReplayDaily(){
        List<ReplayFenBanDTO> list = Lists.newArrayList();
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        int count  = 0;
        for (CirculateInfo circulateInfo:circulateInfos){
            count++;
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            /*if(!stockCode.equals("002795")){
                continue;
            }*/
            System.out.println(stockCode+"------"+count);
            List<StockKbar> stockKBars = getStockKBars(stockCode);
            if (CollectionUtils.isEmpty(stockKBars)) {
                log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                continue;
            }
            StockKbar preStockKbar = null;
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(16);
            for (StockKbar stockKbar:stockKBars){
                limitQueue.offer(stockKbar);
                PlankTypeDTO plankTypeDTO = continuePlankTypeDto(limitQueue, circulateInfo);
                if(plankTypeDTO.isThirdSecondTransactionIsPlank()){
                    BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(preStockKbar.getAdjClosePrice()), preStockKbar.getAdjClosePrice());
                    String stockPlankType = getStockPlankType(plankTypeDTO);
                    ReplayFenBanDTO buy = new ReplayFenBanDTO();
                    buy.setStockCode(stockCode);
                    buy.setStockName(stockName);
                    buy.setTradeDate(stockKbar.getKbarDate());
                    buy.setEndPlank(plankTypeDTO.isEndPlank());
                    buy.setPlanks(stockPlankType);
                    buy.setCloseRate(closeRate);
                    getBeforeRateInfo(stockKbar,buy,stockKBars);
                    getNextDayInfo(stockKbar,buy,stockKBars);
                    list.add(buy);
                }
                preStockKbar = stockKbar;
            }
        }
        return list;
    }

    public void  getBeforeRateInfo(StockKbar buyKbar, ReplayFenBanDTO dto, List<StockKbar> stockKbars){
        List<StockKbar> reverse = Lists.reverse(stockKbars);
        boolean flag = false;
        int i = 0;
        for (StockKbar stockKbar:reverse){
            if(flag){
                i++;
            }
            if(i==3){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRate3(rate);
            }
            if(i==5){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRate5(rate);
            }
            if(i==10){
                BigDecimal rate = PriceUtil.getPricePercentRate(buyKbar.getAdjClosePrice().subtract(stockKbar.getAdjClosePrice()), stockKbar.getAdjClosePrice());
                dto.setBeforeRate10(rate);
                return;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public void  getNextDayInfo(StockKbar buyKbar, ReplayFenBanDTO dto, List<StockKbar> stockKbars){
        boolean flag = false;
        int i = 0;
        for (StockKbar stockKbar:stockKbars){
            if(flag){
                i++;
            }
            if(i==1){
                BigDecimal openRate = PriceUtil.getPricePercentRate(stockKbar.getAdjOpenPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal closeRate = PriceUtil.getPricePercentRate(stockKbar.getAdjClosePrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal highRate = PriceUtil.getPricePercentRate(stockKbar.getAdjHighPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                BigDecimal lowRate = PriceUtil.getPricePercentRate(stockKbar.getAdjLowPrice().subtract(buyKbar.getAdjClosePrice()), buyKbar.getAdjClosePrice());
                dto.setNextOpenRate(openRate);
                dto.setNextCloseRate(closeRate);
                dto.setNextHighRate(highRate);
                dto.setNextLowRate(lowRate);
                return;
            }
            if(stockKbar.getKbarDate().equals(buyKbar.getKbarDate())){
                flag = true;
            }
        }
    }

    public String getStockPlankType(PlankTypeDTO plankTypeDTO){
        int currentPlanks = plankTypeDTO.getPlanks();
        int beforePlanks = plankTypeDTO.getBeforePlanks();
        int space = plankTypeDTO.getSpace();
        int breakDays = space-1;
        if(beforePlanks==0){
            breakDays   = 0;
        }
        if(breakDays==0){
          String type = currentPlanks+"连板";
          return type;
        }else{
            int totalPlanks = currentPlanks + beforePlanks;
            int totalDays = currentPlanks + beforePlanks+1;
            String type = totalDays+"天"+totalPlanks+"板";
            return type;
        }
    }


    //判断连板天数
    public PlankTypeDTO continuePlankTypeDto(LimitQueue<StockKbar> limitQueue, CirculateInfo circulateInfo){
        List<StockKbar> kbars = Lists.newArrayList();
        Iterator<StockKbar> iterator = limitQueue.iterator();
        while(iterator.hasNext()){
            StockKbar kbar = iterator.next();
            kbars.add(kbar);
        }
        PlankTypeDTO plankTypeDTO = new PlankTypeDTO();
        plankTypeDTO.setPlank(false);
        plankTypeDTO.setEndPlank(false);
        if(kbars.size()<2){
            return plankTypeDTO;
        }
        List<StockKbar> reverse = Lists.reverse(kbars);
        StockKbar preStockKbar = null;
        int space = 0;
        int current = 0;
        int before = 0;
        int i=0;
        for (StockKbar stockKbar:reverse){
            i++;
            if(preStockKbar!=null) {
                BigDecimal endPrice = stockKbar.getClosePrice();
                boolean highPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getHighPrice(),endPrice,preStockKbar.getKbarDate());
                boolean endPlank = PriceUtil.isHistoryUpperPrice(circulateInfo.getStockCode(), preStockKbar.getClosePrice(),endPrice,preStockKbar.getKbarDate());
                if(i==2){
                    if(highPlank){
                        /*List<ThirdSecondTransactionDataDTO> list = historyTransactionDataComponent.getData(circulateInfo.getStockCode(),preStockKbar.getKbarDate());
                        int transactionDataPlank = thirdSecondTransactionDataPlank(endPrice, circulateInfo.getStockCode(), list);
                        if(transactionDataPlank==1){
                            current++;
                            plankTypeDTO.setThirdSecondTransactionIsPlank(true);
                        }*/
                        current++;
                        plankTypeDTO.setThirdSecondTransactionIsPlank(true);
                        plankTypeDTO.setEndPlank(endPlank);
                    }
                    if(!plankTypeDTO.isThirdSecondTransactionIsPlank()) {
                        return plankTypeDTO;
                    }
                }
                if(i>2) {
                    if (endPlank) {
                        if (space == 0) {
                            current++;
                        }
                        if (space == 1) {
                            before++;
                        }
                    } else {
                        space++;
                    }
                    if (space >= 2) {
                        break;
                    }
                }
            }
            preStockKbar = stockKbar;
        }
        plankTypeDTO.setPlanks(current);
        plankTypeDTO.setBeforePlanks(before);
        plankTypeDTO.setSpace(space);
        return plankTypeDTO;
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
        kbarQuery.setKbarDateFrom("20201101");
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        List<StockKbar> stockKbarDeleteNew = commonComponent.deleteNewStockTimes(stockKbars, 1000);
        return stockKbarDeleteNew;
    }


}
