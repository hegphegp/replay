package com.bazinga.repaly.job;


import com.bazinga.replay.component.NewStockComponent;
import com.bazinga.replay.component.StockPlankDailyComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@Slf4j
public class NewStockJob {

    @Autowired
    private NewStockComponent newStockComponent;

    public void execute(){
        log.info("<--------------新股复盘 start --------------->");
        newStockComponent.catchNewStock();
        log.info("<--------------新股复盘 end --------------->");
    }
}
