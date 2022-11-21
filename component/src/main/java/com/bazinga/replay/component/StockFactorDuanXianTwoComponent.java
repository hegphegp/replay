package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.exception.BusinessException;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.*;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.*;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockFactorDuanXianTwoComponent {
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

    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public static String leveStockCode = "600896,600555,600385,600090,300202,000673,300312,600870,300038,300367,600146,000613,300023,002464,300325,000611,000502,002447,002684,300064,002147,002770,600093,600275,002473,600209,600856,300178,002618,600652,600890,600091,002260,000687,600291,600695,603157,000585,603996,000835,600145,002619,000780,600723,600068,300362,002359,000760,600634,600614,002711,600485,002450,600891,002071,600701,000662,600247,600978,600677,600086,600687,600317";
    //public static String leveStockCode = "1111";


    public void factorTest(){
        List<LaoWoYinKuiExcelDTO> excelDTOS = getLaoWoYinLiExcel();
        getDaBanInfo(excelDTOS);
        List<Object[]> datas = Lists.newArrayList();
        for (LaoWoYinKuiExcelDTO dto:excelDTOS) {
            List<Object> list = new ArrayList<>();
            list.add(dto.getStockCode());
            list.add(dto.getTradeDate());
            list.add(dto.getStockCode());
            list.add(dto.getStockName());
            list.add(dto.getBuyAmount());
            list.add(dto.getSellAmount());
            list.add(dto.getProfitAmount());
            list.add(dto.getProfitRate());
            list.add(dto.getPlankType());
            list.add(dto.getBuyTime());
            list.add(dto.getDealTime());
            list.add(dto.getBuyDealTime());
            list.add(dto.getBreakType());
            list.add(dto.getBuyType());
            list.add(dto.getBuyMode());
            list.add(dto.getBlockName());
            list.add(dto.getFactorValue());
            list.add(dto.getSameCount());
            list.add(dto.getTotal());
            Object[] objects = list.toArray();
            datas.add(objects);
        }


        String[] rowNames = {"index","交易日期","股票代码","股票名称","买金额","卖金额","正盈利","盈亏比","连板情况","委托时间","成交时间","成交时间差","是否炸板",
                "买入方式","所属模式","所属行业","因子日因子值","因子日相同行业前200数量","因子日相同行业前200因子值累计"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("美国往事短线",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("美国往事短线");
        }catch (Exception e){
            log.info(e.getMessage());
        }
    }

    public void getDaBanInfo(List<LaoWoYinKuiExcelDTO> excelDTOS){
        Map<String, Date> preTradeDateMap = commonComponent.getPreTradeDateMap();
        Map<String, List<StockFactorLevelTestDTO>> factorMap = new HashMap<>();
        int index = 0;
        for (LaoWoYinKuiExcelDTO excelDTO:excelDTOS) {
            index++;
            System.out.println(index);
            if(StringUtils.isBlank(excelDTO.getStockCode())){
                continue;
            }
            Date date = DateUtil.cstToDate(excelDTO.getTradeDate());
            excelDTO.setTradeDate(DateUtil.format(date, DateUtil.yyyy_MM_dd));
            Date preTradeDate = preTradeDateMap.get(excelDTO.getTradeDate());
            String preDateyyyMMdd = DateUtil.format(preTradeDate, DateUtil.yyyyMMdd);
            String preDateyyy_MM_dd = DateUtil.format(preTradeDate, DateUtil.yyyy_MM_dd);
            StockFactor stockFactor = getStockFactor(preDateyyy_MM_dd, excelDTO.getStockCode());
            if (stockFactor != null) {
                excelDTO.setFactorValue(stockFactor.getIndex1());
            }
            System.out.println(excelDTO.getStockName()+excelDTO.getTradeDate());
            getBlockInfo(excelDTO.getStockCode(), preDateyyyMMdd, excelDTO);
            if (StringUtils.isBlank(excelDTO.getBlockCode())) {
                continue;
            }
            List<StockFactorLevelTestDTO> plankTimePairs = factorMap.get(excelDTO.getTradeDate());
            if (plankTimePairs == null){
                plankTimePairs = getPlankTimePairs(preTradeDate);
                factorMap.put(excelDTO.getTradeDate(),plankTimePairs);
            }
            if(CollectionUtils.isEmpty(plankTimePairs)){
                continue;
            }
            int i = 0;
            BigDecimal totalFactor = BigDecimal.ZERO;
            for (StockFactorLevelTestDTO testDTO:plankTimePairs){
                if(excelDTO.getBlockCode().equals(testDTO.getBlockCode())){
                    i++;
                    totalFactor = totalFactor.add(testDTO.getIndex2a());
                }
            }
            if(i>0){
                excelDTO.setSameCount(i);
                excelDTO.setTotal(totalFactor);
            }
        }

    }

    public List<StockFactorLevelTestDTO> getPlankTimePairs(Date tradeDate){
        List<StockFactorLevelTestDTO> dayBuys = Lists.newArrayList();
        String dateyyyyMMdd = DateUtil.format(tradeDate, DateUtil.yyyyMMdd);
        String dateStr = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
        List<StockFactor> stockFactors200 = getStockFactors200(dateStr);
        if(CollectionUtils.isEmpty(stockFactors200)){
            return dayBuys;
        }
        for (StockFactor stockFactor:stockFactors200){
            boolean stStock = isBlockStStock(stockFactor.getStockCode(), dateyyyyMMdd);
            if(stStock){
                continue;
            }
            StockFactorLevelTestDTO buyDTO = new StockFactorLevelTestDTO();
            buyDTO.setStockCode(stockFactor.getStockCode());
            buyDTO.setStockName(stockFactor.getStockName());
            buyDTO.setIndex2a(stockFactor.getIndex1());
            getBlockInfo(stockFactor.getStockCode(),dateyyyyMMdd,buyDTO);
            dayBuys.add(buyDTO);
        }
        return dayBuys;
    }



    public void getBlockInfo(String stockCode,String tradeDate,StockFactorLevelTestDTO buy){
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        for (HistoryBlockStocks blockStocks:historyBlockStocks){
            if(!blockStocks.getBlockCode().startsWith("881")){
                continue;
            }
            String stocks = blockStocks.getStocks();
            if(stocks.contains(stockCode)){
                buy.setBlockName(blockStocks.getBlockName());
                buy.setBlockCode(blockStocks.getBlockCode());
                return;
            }
        }
    }

    public void getBlockInfo(String stockCode,String tradeDate,LaoWoYinKuiExcelDTO buy){
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        for (HistoryBlockStocks blockStocks:historyBlockStocks){
            if(!blockStocks.getBlockCode().startsWith("881")){
                continue;
            }
            String stocks = blockStocks.getStocks();
            if(stocks!=null&&stocks.contains(stockCode)){
                buy.setBlockName(blockStocks.getBlockName());
                buy.setBlockCode(blockStocks.getBlockCode());
                return;
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


    public List<ThsStockKbar> getStockKBarsDelete30Days(String stockCode){
        try {
            ThsStockKbarQuery query = new ThsStockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(query);
            List<ThsStockKbar> result = Lists.newArrayList();
            for (ThsStockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<ThsStockKbar> best = deleteNewStockTimes(stockKbars, 2000);
            return best;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
    public List<ThsStockKbar> deleteNewStockTimes(List<ThsStockKbar> list, int size){
        List<ThsStockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        ThsStockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (ThsStockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }

    public List<StockFactor> getStockFactors200(String tradeDateString){
        try {
            StockFactorQuery query = new StockFactorQuery();
            query.setKbarDate(tradeDateString);
            query.addOrderBy("index1", Sort.SortType.DESC);
            query.setLimit(200);
            List<StockFactor> stockFactors = stockFactorService.listByCondition(query);
            return stockFactors;
        }catch (Exception e){
            return null;
        }
    }

    public StockFactor getStockFactor(String tradeDateString,String stockCode){
        StockFactorQuery query = new StockFactorQuery();
        query.setKbarDate(tradeDateString);
        query.setStockCode(stockCode);
        List<StockFactor> stockFactors = stockFactorService.listByCondition(query);
        if(CollectionUtils.isEmpty(stockFactors)){
            return null;
        }
        return stockFactors.get(0);

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
    /**
     * 使用同花顺板块st内查询
     * @param tradeDate
     * @return
     */
    public boolean isBlockStStock(String stockCode,String tradeDate) {
        HistoryBlockStocksQuery query = new HistoryBlockStocksQuery();
        query.setBlockCode("885699");
        query.setTradeDate(tradeDate);
        List<HistoryBlockStocks> historyBlockStocks = historyBlockStocksService.listByCondition(query);
        if(CollectionUtils.isEmpty(historyBlockStocks)){
            return false;
        }
        HistoryBlockStocks blockStocks = historyBlockStocks.get(0);
        if(StringUtils.isBlank(blockStocks.getStocks())){
            return false;
        }
        String stocks = blockStocks.getStocks();
        if(stocks.contains(stockCode)){
            return true;
        }
        return false;
    }



    public List<LaoWoYinKuiExcelDTO> getLaoWoYinLiExcel() {
        File file = new File("D:/circulate/laowoyinkui.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/laowoyinkui.xlsx" + "不存在");
        }
        try {
            List<LaoWoYinKuiExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(LaoWoYinKuiExcelDTO.class);
            return dataList;
        } catch (Exception e) {
            log.error("更新流通 z 信息异常", e);
            throw new BusinessException("文件解析及同步异常", e);
        }
    }


}
