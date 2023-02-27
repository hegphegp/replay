package com.bazinga.test;


import com.bazinga.base.Sort;
import com.bazinga.replay.component.CommonComponent;
import com.bazinga.replay.component.ThsBaseDataDoubleCheckComponent;
import com.bazinga.replay.component.ThsCirculateInitComponent;
import com.bazinga.replay.component.ThsStockKbarInitComponent;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;
import java.util.List;


/**
 * 单元测试基类<p/>
 * 其他单元测试都继承本类
 *
 * @author zixiao
 * @date 2016/5/20
 */
@ContextConfiguration(locations="classpath:/META-INF/spring/replay-context.xml")
@RunWith(SpringJUnit4ClassRunner.class)
public class BaseDataTest {
    @Autowired
    private ThsBaseDataDoubleCheckComponent thsBaseDataDoubleCheckComponent;
    @Autowired
    private ThsCirculateInitComponent thsCirculateInitComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private ThsStockKbarInitComponent thsStockKbarInitComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Test
    public void test1() {
        thsBaseDataDoubleCheckComponent.checkDateKbarWithCirculate("20211011");
        //thsBaseDataDoubleCheckComponent.checkOutCirculateAndThsCirculate("20221227");
        //thsCirculateInitComponent.initAMarketChangeCirculateInfo("20221222", DateUtil.format(new Date(),DateUtil.yyyyMMdd));
    }

    @Test
    public void thsCirculateDaily() {
        //Date preTradeDate = commonComponent.preTradeDate(new Date());
        Date preTradeDate = DateUtil.parseDate("20230215", DateUtil.yyyyMMdd);
        thsCirculateInitComponent.initAMarketChangeCirculateInfo(DateUtil.format(preTradeDate,DateUtil.yyyyMMdd), DateUtil.format(new Date(),DateUtil.yyyyMMdd));
    }

    @Test
    public void thsStockKbarDaily() {
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateTo(DateUtil.parseDate("20220607",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.DESC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        for (TradeDatePool tradeDatePool:tradeDatePools) {
            String format = DateUtil.format(tradeDatePool.getTradeDate(), DateUtil.yyyyMMdd);
            thsStockKbarInitComponent.circulateCheckStockAndUpdate(format);
        }
        //thsStockKbarInitComponent.circulateCheckStockAndUpdate("20221222");
    }


}
