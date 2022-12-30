package com.bazinga.replay.component;

import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.service.CirculateInfoAllService;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockPlankDailyService;
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
public class CirculateInfoComponent {
    @Autowired
    private StockPlankDailyService stockPlankDailyService;
    @Autowired
    private CirculateInfoService circulateInfoService;


    public List<CirculateInfo> getMainAndGrowth(){
        CirculateInfoQuery query = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(query);
        List<CirculateInfo> list = circulateInfos.stream().filter(circulateInfoAll -> !circulateInfoAll.getStockCode().startsWith("688")).collect(Collectors.toList());
        return list;
    }


}
