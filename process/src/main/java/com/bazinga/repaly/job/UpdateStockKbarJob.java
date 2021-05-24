package com.bazinga.repaly.job;


import com.bazinga.replay.component.StockKbarComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class UpdateStockKbarJob {

    @Autowired
    private StockKbarComponent stockKbarComponent;

    public void execute(){
        stockKbarComponent.batchUpdateDaily();
    }
}
