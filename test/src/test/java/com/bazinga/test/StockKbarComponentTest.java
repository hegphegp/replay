package com.bazinga.test;

import com.bazinga.replay.component.StockBollingComponent;
import com.bazinga.replay.component.StockCommonReplayComponent;
import com.bazinga.replay.component.StockKbarComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.Date;

public class StockKbarComponentTest extends BaseTestCase {

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Autowired
    private StockCommonReplayComponent stockCommonReplayComponent;

    @Autowired
    private StockBollingComponent stockBollingComponent;

    @Test
    public void test(){

        stockKbarComponent.batchUpdateDaily();
        //stockCommonReplayComponent.saveCommonReplay(new Date());
    }

    @Test
    public void test2(){
        stockKbarComponent.batchcalAvgLine();
    }

    @Test
    public void test3(){
        stockBollingComponent.batchInitBoll();
    }

    @Test
    public void test4(){
        Double avg = stockKbarComponent.calDaysAvg("000537", "20220330", 20);
        System.out.println(avg);
        BigDecimal std = stockBollingComponent.calStd("300333", "20220105", 20);
        System.out.println(std);
    }
}
