package com.bazinga.test;

import com.bazinga.replay.component.NewStockComponent;
import com.bazinga.replay.component.StockPlankDailyComponent;
import com.bazinga.replay.component.SynInfoComponent;
import com.bazinga.util.DateUtil;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

/**
 * 单元测试基类<p/>
 * 其他单元测试都继承本类
 *
 * @author zixiao
 * @date 2016/5/20
 */
@ContextConfiguration(locations="classpath:/META-INF/spring/replay-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseTestCase {
    @Autowired
    private StockPlankDailyComponent stockPlankDailyComponent;
    @Autowired
    private NewStockComponent newStockComponent;
    @Autowired
    private SynInfoComponent synInfoComponent;
    @Test
    public void test1() {
        stockPlankDailyComponent.stockPlankDailyStatistic(new Date());
        //newStockComponent.catchNewStock();
    }

    @Test
    public void test2() {
        synInfoComponent.synCirculateInfo();
    }


}
