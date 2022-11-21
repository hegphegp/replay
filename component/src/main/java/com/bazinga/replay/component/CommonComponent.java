package com.bazinga.replay.component;



import com.bazinga.base.Sort;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
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

    public Date afterTradeDate(Date date){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateTimeUtils.getDate235959(date));
        query.addOrderBy("trade_date",Sort.SortType.ASC);
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        if(CollectionUtils.isEmpty(dates)){
            return null;
        }
        return dates.get(0).getTradeDate();
    }

    public Date getCurrentTradeDate(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateTimeUtils.getDate235959(new Date()));
        query.addOrderBy("trade_date",Sort.SortType.DESC);
        query.setLimit(1);
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        return dates.get(0).getTradeDate();
    }

    public Map<String,Date> getPreTradeDateMap(){
        Map<String,Date> map = new HashMap<>();
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateTimeUtils.getDate235959(new Date()));
        query.addOrderBy("trade_date",Sort.SortType.ASC);
        List<TradeDatePool> dates = tradeDatePoolService.listByCondition(query);
        TradeDatePool preTradeDatePool = null;
        for (TradeDatePool tradeDatePool:dates){
            if(preTradeDatePool!=null){
                map.put(DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyy_MM_dd), preTradeDatePool.getTradeDate());
            }
            preTradeDatePool = tradeDatePool;
        }
        return map;
    }


    //包括新股最后一个一字板
    public List<StockKbar> deleteNewStockTimes(List<StockKbar> list, int size){
        List<StockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        StockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (StockKbar dto:list){
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

}
