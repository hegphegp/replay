package com.bazinga.test;

import com.bazinga.replay.component.StockKbarComponent;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StockKbarComponentTest extends BaseTestCase {

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Test
    public void test(){

        stockKbarComponent.batchKbarDataInit();
    }
}
