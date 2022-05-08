package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.constant.SymbolConstants;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.convert.StockKbarConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.CirculateDetailDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockAverageLineQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import javax.json.JsonArray;
import javax.json.JsonValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@Slf4j
public class StockListComponent {

    private static final int  LOOP_TIMES =3;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockAverageLineService stockAverageLineService;

    private static final ExecutorService AVGLINE_POOL = ThreadPoolUtils.create(8, 16, 512, "calAvgLinePool");

    public  Map<String, AdjFactorDTO> getStocks() {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "stock_basic");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        paramMap.put("list_status", "L");
       /* paramMap.put("params", paramsdate);
        paramMap.put("fields", "trade_date,adj_factor");*/
        int times = 1;
        while (times <= LOOP_TIMES){
            try {
                String body = Jsoup.connect("http://api.tushare.pro").ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(JSONObject.toJSONString(paramMap)).post().text();
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray fields = data.getJSONArray("items");
                if (CollectionUtils.isEmpty(fields)) {
                    return null;
                }
                List<CirculateDetailDTO> circulateInfos = convertToCirculate(fields);
                List<CirculateDetailDTO> list = filterCirculate(circulateInfos);
                System.out.println(list.size());
                return null;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}",  e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public  Map<String, AdjFactorDTO> getMarketInfo(String stockCode) {
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("api_name", "bak_basic");
        paramMap.put("token", "fb5a3049bfc93659682fd10dfb14cafad3ce69637b36bc94a3f45916");
        Map<String, String> paramsdate = new HashMap<>();
        paramMap.put("ts_code", stockCode);
        //paramMap.put("trade_date", "20211012");
       /* paramMap.put("params", paramsdate);
        paramMap.put("fields", "trade_date,adj_factor");*/
        int times = 1;
        while (times <= LOOP_TIMES){
            try {
                String body = Jsoup.connect("http://api.tushare.pro").ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(JSONObject.toJSONString(paramMap)).post().text();
                JSONObject jsonObject = JSONObject.parseObject(body);
                JSONObject data = jsonObject.getJSONObject("data");
                JSONArray fields = data.getJSONArray("items");
                if (CollectionUtils.isEmpty(fields)) {
                    return null;
                }

                return null;
            } catch (Exception e) {
                log.error("第{}次获取复权因子异常 stockCode ={}",  e);
            }
            times++;
            try {
                TimeUnit.SECONDS.sleep(15);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    public List<CirculateDetailDTO> filterCirculate(List<CirculateDetailDTO> circulateInfos){
        Date tradeDateStart = getTradeDateStart();
        List<CirculateDetailDTO> list = Lists.newArrayList();
        for (CirculateDetailDTO circulateInfo:circulateInfos){
            if(MarketUtil.isAStocks(circulateInfo.getStockCode())){
                if(circulateInfo.getStockName().contains("ST")){
                    System.out.println(circulateInfo.getStockCode()+"==="+circulateInfo.getStockName());
                    continue;
                }
                if(MarketUtil.isChuangYe(circulateInfo.getStockCode())){
                    if(circulateInfo.getStockName().startsWith("C")) {
                        Date marketDate = DateUtil.parseDate(circulateInfo.getMarketDate(), DateUtil.yyyyMMdd);
                        if (marketDate.after(tradeDateStart)) {
                            continue;
                        }
                    }
                    if(circulateInfo.getStockName().startsWith("N")) {
                        continue;
                    }

                }
                list.add(circulateInfo);
            }
        }
        return list;
    }


    public Date getTradeDateStart(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        query.setTradeDateTo(new Date());
        query.setLimit(5);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        TradeDatePool tradeDatePool = tradeDatePools.get(4);
        return tradeDatePool.getTradeDate();
    }


    public List<CirculateDetailDTO> convertToCirculate(JSONArray jsonArray){
        List<CirculateDetailDTO> circulates = Lists.newArrayList();
        for(int i = 0;i<jsonArray.size();i++){
            JSONArray circulateArray = jsonArray.getJSONArray(i);
            String stockCode = circulateArray.get(1).toString();
            String stockName = circulateArray.get(2).toString();
            String marketDate = circulateArray.get(6).toString();
            CirculateDetailDTO circulateInfo = new CirculateDetailDTO();
            circulateInfo.setStockCode(stockCode);
            circulateInfo.setStockName(stockName);
            circulateInfo.setMarketDate(marketDate);
            circulates.add(circulateInfo);
        }
        return circulates;
    }


}
