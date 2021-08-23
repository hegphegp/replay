package com.bazinga.test;

import com.bazinga.replay.component.*;
import com.bazinga.util.DateTimeUtils;
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
    @Autowired
    private PlankExchangeDailyComponent plankExchangeDailyComponent;
    @Autowired
    private StockReplayDailyComponent stockReplayDailyComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private StockCommonReplayComponent stockCommonReplayComponent;
    @Test
    public void test1() {
        stockReplayDailyComponent.stockReplayDaily(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockReplayDailyComponent.calPreDateAvgPrice(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockPlankDailyComponent.stockPlankDailyStatistic(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        newStockComponent.catchNewStock();
        plankExchangeDailyComponent.plankExchangeDaily(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockKbarComponent.batchUpdateDaily();
        stockPlankDailyComponent.calMax100DaysPriceForTwoPlank(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockPlankDailyComponent.calMin15DaysPriceForTwoPlank(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockPlankDailyComponent.calSubNewStock(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));

        //新版复盘
        stockCommonReplayComponent.saveCommonReplay(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockCommonReplayComponent.firstPlankNoBuyInfo(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));
        stockCommonReplayComponent.highRaiseStockInfo(DateUtil.parseDate("2021-08-20 15:30:30",DateUtil.DEFAULT_FORMAT));

    }

    @Test
    public void test2() {
        synInfoComponent.synCirculateInfo();
    }


    @Test
    public void test3() {
        stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
    }


}
