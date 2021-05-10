package com.bazinga.replay.component;



import com.bazinga.base.Sort;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.google.common.collect.Lists;
import com.tradex.util.StockUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class CommonComponent {
    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    public Date preTradeDate(Date date){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateTimeUtils.getDate000000(date));
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        if(CollectionUtils.isEmpty(dates)){
            return new Date();
        }
        return dates.get(0).getTradeDate();
    }

    public boolean isTradeDate(Date date){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate000000(date));
        query.setTradeDateTo(DateTimeUtils.getDate235959(date));
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        if(CollectionUtils.isEmpty(dates)){
            return false;
        }
        return true;
    }

}
