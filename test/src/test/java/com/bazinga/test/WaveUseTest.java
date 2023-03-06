package com.bazinga.test;

import com.bazinga.component.*;
import com.bazinga.replay.component.*;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockIndexService;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
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
public class WaveUseTest {

    @Autowired
    private IndexSpecialKbarComponent indexSpecialKbarComponent;
    @Autowired
    private ThsQuoteSaveComponent thsQuoteSaveComponent;


    /**
     * 计算k线
     */
    @Test
    public void test1() {
        //thsQuoteSaveComponent.saveQuoteQiHuo("20180101","20230101","IFZL","CFE","沪深300期货");
        indexSpecialKbarComponent.getSecondKbar(60,"IFZLCFE","沪深300期货",1);
        indexSpecialKbarComponent.getSecondKbar(900,"IFZLCFE","沪深300期货",15);
        indexSpecialKbarComponent.getSecondKbar(1800,"IFZLCFE","沪深300期货",30);
        indexSpecialKbarComponent.getSecondKbar(3600,"IFZLCFE","沪深300期货",60);
    }

}
