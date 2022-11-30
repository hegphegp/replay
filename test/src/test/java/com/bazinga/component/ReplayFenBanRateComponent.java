package com.bazinga.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.CommonConstant;
import com.bazinga.dto.HuShen300MacdBuyDTO;
import com.bazinga.dto.MacdBuyDTO;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CirculateInfoComponent;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.ThsDataComponent;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.MacdIndexDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
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
import org.apache.commons.lang3.StringUtils;
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

    public static final ExecutorService THREAD_POOL_QUOTE = ThreadPoolUtils.create(16, 32, 512, "QuoteThreadPool");

    public void macdExcel(){
       /* List<HuShen300MacdBuyDTO> buys = getMacdBuyDTO();
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
            list.add(dto.getPreMacdBuy());
            list.add(dto.getMacdSell());
            list.add(dto.getPreMacdSell());
            list.add(dto.getBuyPrice());
            list.add(dto.getSellPrice());
            list.add(dto.getPreBuyIndex());
            list.add(dto.getProfit());
            list.add(dto.getProfitValue());

            Object[] objects = list.toArray();
            datas.add(objects);
        }

        String[] rowNames = {"index","方向（1多 -1空）","买入日期","买入时间点","卖出日期","卖出时间点","买入macd","买入前一次macd","卖出macd","卖出前macd","买入价格","卖出价格","买入前一跳指数","利润","利润值"};
        PoiExcelUtil poiExcelUtil = new PoiExcelUtil("macd买入",rowNames,datas);
        try {
            poiExcelUtil.exportExcelUseExcelTitle("macd买入");
        }catch (Exception e){
            log.info(e.getMessage());
        }*/
    }

    public void stockReplayDaily(){
        HashMap<String, StockReplayDaily> map = new HashMap<>();
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        for (CirculateInfo circulateInfo:circulateInfos){
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            List<StockKbar> stockKBars = getStockKBars(stockCode);
            if (CollectionUtils.isEmpty(stockKBars)) {
                log.info("复盘数据 没有获取到k线数据 stockCode:{} stockName:{}", stockCode, stockName);
                continue;
            }
            LimitQueue<StockKbar> limitQueue = new LimitQueue<>(10);
            for (StockKbar stockKbar:stockKBars){
                limitQueue.offer(stockKbar);

            }


        }
    }
    public List<StockKbar> getStockKBars(String stockCode){
        StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode(stockCode);
        kbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        List<StockKbar> stockKbarDeleteNew = commonComponent.deleteNewStockTimes(stockKbars, 800);
        return stockKbarDeleteNew;
    }


}
