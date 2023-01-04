package com.bazinga.repaly.job;


import com.bazinga.replay.component.IndexKbarCurrentComponent;
import com.bazinga.replay.component.NewStockComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockKbarCurrentJob {

    @Autowired
    private IndexKbarCurrentComponent indexKbarCurrentComponent;

    public void execute(){
        log.info("<--------------上证沪深指数开始 start --------------->");
        indexKbarCurrentComponent.indexKbarCurrent();
        indexKbarCurrentComponent.indexKbarCurrentNew();
        log.info("<--------------上证沪深指数结束 end --------------->");
    }
}
