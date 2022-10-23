package com.bazinga.replay.component;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class IndexKbarCurrentComponent {
    @Autowired
    private StockKbarComponent stockKbarComponent;

    public void indexKbarCurrent(){
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",1);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399300","沪深300指数",1);
    }

}
