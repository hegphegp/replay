package com.bazinga.repaly.job;


import com.bazinga.replay.component.StockPlankDailyComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class StockPlankDailyJob {

    @Autowired
    private StockPlankDailyComponent stockPlankDailyComponent;

    public void execute(){
        log.info("<--------------stockPlankDaily复盘 start --------------->");
        stockPlankDailyComponent.stockPlankDailyStatistic(new Date());
        log.info("<--------------stockPlankDaily复盘 end --------------->");
    }
}
