package com.bazinga.replay.component;


import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockAverageLineService;
import com.bazinga.replay.service.StockKbarService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class StockBollingComponent {


    @Autowired
    private CirculateInfoService circulateInfoService;

    @Autowired
    private StockKbarService stockKbarService;

    @Autowired
    private StockAverageLineService stockAverageLineService;



    public void  batchInitBoll(){


    }

    public void initBoll(String stockCode){

      //  stockAverageLineService.



    }


}
