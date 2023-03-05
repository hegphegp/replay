package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.ThsCirculateInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class TradeDatePoolComponent {
    @Autowired
    private TradeDatePoolService tradeDatePoolService;

    /**
     *
     * @param start   20220101
     * @param end  20230101
     * 数据包含两边   可以为null
     * @return
     */
    public List<TradeDatePool> getTradeDatePools(String start,String end){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        if(StringUtils.isNotBlank(start)) {
            query.setTradeDateFrom(DateUtil.parseDate(start, DateUtil.yyyyMMdd));
        }
        if(StringUtils.isNotBlank(end)) {
            query.setTradeDateTo(DateUtil.parseDate(end, DateUtil.yyyyMMdd));
        }
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }





}
