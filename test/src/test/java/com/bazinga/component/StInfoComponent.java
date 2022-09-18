package com.bazinga.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.dto.*;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.bazinga.util.ThreadPoolUtils;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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
public class StInfoComponent {
    @Autowired
    private RedisMoniorService redisMoniorService;
    @Autowired
    private HistoryBlockStocksService historyBlockStocksService;

    public static Map<String,Map<String,BigDecimal>> buyPriceCacheMap = new HashMap<>();

    public boolean isStStock(String stockCode,String tradeDate) {
        String rediesKey = stockCode +"_" + "test";
        RedisMonior redisMinor = redisMoniorService.getByRedisKey(rediesKey);
        if(redisMinor==null||StringUtils.isBlank(redisMinor.getRedisValue())){
            return false;
        }
        boolean flag = redisMinor.getRedisValue().contains(tradeDate);
        return flag;
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

    public static void main(String[] args) {
        String str = "anbcskjjdisdsdsdasa";
        boolean dasa = str.contains("dasa");
        System.out.println(dasa);
    }
}
