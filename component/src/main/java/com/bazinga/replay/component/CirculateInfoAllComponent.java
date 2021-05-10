package com.bazinga.replay.component;

import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.service.CirculateInfoAllService;
import com.bazinga.replay.service.StockPlankDailyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class CirculateInfoAllComponent {
    @Autowired
    private StockPlankDailyService stockPlankDailyService;
    @Autowired
    private CirculateInfoAllService circulateInfoAllService;


    public List<CirculateInfoAll> getMainAndGrowth(){
        CirculateInfoAllQuery query = new CirculateInfoAllQuery();
        List<CirculateInfoAll> circulateInfoAlls = circulateInfoAllService.listByCondition(query);
        List<CirculateInfoAll> list = circulateInfoAlls.stream().filter(circulateInfoAll -> !circulateInfoAll.getStock().startsWith("688")).collect(Collectors.toList());
        return list;
    }


}
