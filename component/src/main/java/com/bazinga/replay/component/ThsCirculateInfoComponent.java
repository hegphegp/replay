package com.bazinga.replay.component;

import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.bazinga.replay.service.ThsCirculateInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsCirculateInfoComponent {
    @Autowired
    private ThsCirculateInfoService thsCirculateInfoService;

    /**
     * 获取当天市场上所有的股票
     * @param tradeDate 20220101
     * @return
     */
    public List<ThsCirculateInfo> getMarketACirculate(String tradeDate){
        ThsCirculateInfoQuery thsCirculateInfoQuery = new ThsCirculateInfoQuery();
        thsCirculateInfoQuery.setTradeDate(tradeDate);
        List<ThsCirculateInfo> thsCirculateInfos = thsCirculateInfoService.listByCondition(thsCirculateInfoQuery);
        return thsCirculateInfos;
    }


}
