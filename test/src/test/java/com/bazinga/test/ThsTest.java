package com.bazinga.test;

import com.bazinga.base.Sort;
import com.bazinga.component.*;
import com.bazinga.replay.component.*;
import com.bazinga.replay.dto.BigExchangeTestBuyDTO;
import com.bazinga.replay.dto.MacdIndexDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.ThsQuoteInfo;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.ThsQuoteInfoQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockIndexService;
import com.bazinga.replay.service.ThsQuoteInfoService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.ArrayList;
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
public class ThsTest {
    @Autowired
    private BigAmountTestComponent bigAmountTestComponent;
    @Autowired
    private ThsDataComponent thsDataComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private StockIndexService stockIndexService;
    @Autowired
    private ThsStockIndexComponent thsStockIndexComponent;
    @Autowired
    private MacdCalComponent macdCalComponent;
    @Autowired
    private IndexDValueComponent indexDValueComponent;
    @Autowired
    private NorthSouthMoneyComponent northSouthMoneyComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private BlockFollowComponent blockFollowComponent;
    @Autowired
    private ThsBlockKbarComponent thsBlockKbarComponent;
    @Autowired
    private QuanShangHighComponent quanShangHighComponent;
    @Autowired
    private HangYeLevelComponent hangYeLevelComponent;
    @Autowired
    private BlockFollowStaticComponent blockFollowStaticComponent;
    @Autowired
    private TianDiPlankComponent tianDiPlankComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private BlockFollowTestComponent blockFollowTestComponent;
    @Autowired
    private StrongBlockExploreComponent strongBlockExploreComponent;
    @Autowired
    private BlockFollowStaticCurrentComponent blockFollowStaticCurrentComponent;
    @Autowired
    private PlankAmountSuperTestComponent plankAmountSuperTestComponent;
    @Autowired
    private BuyGroundGatherComponent buyGroundGatherComponent;
    @Autowired
    private BuyTwoToThreeComponent buyTwoToThreeComponent;
    @Autowired
    private StockBlockLevelComponent stockBlockLevelComponent;
    @Autowired
    private StrongPlankDefineComponent strongPlankDefineComponent;
    @Autowired
    private FirstPlankBuyTimeLevelComponent firstPlankBuyTimeLevelComponent;
    @Autowired
    private  CirculateInfoService circulateInfoService;
    @Autowired
    private StockFactorTestOneComponent stockFactorTestOneComponent;
    @Autowired
    private StockFactorTestTwoComponent stockFactorTestTwoComponent;
    @Autowired
    private HighMarketExplorComponent highMarketExplorComponent;
    @Autowired
    private BlockFollowStaticCurrentHistoryComponent blockFollowStaticCurrentHistoryComponent;
    @Autowired
    private RealMoneyComponent realMoneyComponent;
    @Autowired
    private TenDayExplorComponent tenDayExplorComponent;
    @Autowired
    private IndexKbarCurrentComponent indexKbarCurrentComponent;
    @Autowired
    private StockFactorTestThreeComponent stockFactorTestThreeComponent;
    @Autowired
    private StockFactorWuDiNewTwoComponent stockFactorWuDiNewTwoComponent;
    @Autowired
    private StockFactorWuDiNewOnlineComponent stockFactorWuDiNewOnlineComponent;
    @Autowired
    private StockFactorDuanXianTwoComponent stockFactorDuanXianTwoComponent;
    @Autowired
    private ThsQuoteSaveComponent thsQuoteSaveComponent;
    @Autowired
    private ThsQuoteInfoService thsQuoteInfoService;
    @Autowired
    private HuShen300SecondKbarComponent huShen300SecondKbarComponent;
    @Autowired
    private HuShen3003MinKbarComponent huShen3003MinKbarComponent;
    @Autowired
    private HuShen3001MinKbarComponent huShen3001MinKbarComponent;
    @Autowired
    private HuShen3003SecondNewKbarComponent huShen3003SecondNewKbarComponent;
    @Autowired
    private ReplayFenBanRateComponent replayFenBanRateComponent;
    @Autowired
    private GatherAmountComponent gatherAmountComponent;
    @Autowired
    private ThreePlankBuyBlockComponent threePlankBuyBlockComponent;
    @Autowired
    private ThreePlankBuyHotBlockComponent threePlankBuyHotBlockComponent;
    @Autowired
    private BlockFollowStaticCurrent4PlankComponent blockFollowStaticCurrent4PlankComponent;
    @Autowired
    private SendReplayInfoAutoComponent sendReplayInfoAutoComponent;
    @Autowired
    private PlankAndOpenTimeComponent plankAndOpenTimeComponent;



    @Test
    public void test1() {
        plankAndOpenTimeComponent.plankAndOpenTime();
        //System.out.println(1111);
        //threePlankBuyHotBlockComponent.threePlankHotBlockBuyTest();
        //threePlankBuyHotBlockComponent.getAlLPlanks();
        //threePlankBuyBlockComponent.threePlankBuyTest();
        //gatherAmountComponent.gatherAmountBuy();
        //blockFollowStaticCurrentComponent.blockFollowStaticInfo();
        //replayFenBanRateComponent.plankFenBan();
        //stockFactorDuanXianTwoComponent.factorTest();
        //线上使用版本
        //tockFactorWuDiNewTwoComponent.factorTest();

        //historyTransactionDataComponent.getFiveMinData("000001","20220121");

       // stockFactorTestThreeComponent.factorTest();
        //indexKbarCurrentComponent.indexKbarCurrent();
        /*indexKbarCurrentComponent.indexStockKbarSend();
        indexKbarCurrentComponent.stockIndexSend();*/
        /*thsStockIndexComponent.shMACDIndex("20221021","3993ok
        00","沪深300",".SZ");
        thsStockIndexComponent.shMACDIndex("20221021","000001","上证指数",".SH");*/
        //tenDayExplorComponent.tenDayExplor();
        //realMoneyComponent.realMoneyExplor();
        //highMarketExplorComponent.bigMarketExplor();
        //stockFactorTestTwoComponent.factorTest();
        //stockFactorTestOneComponent.factorTest();
        //firstPlankBuyTimeLevelComponent.strongPlank();
       // strongPlankDefineComponent.strongPlank();
        //blockFollowStaticCurrent4PlankComponent.blockFollowStaticInfo();
        //用于板块跟随回测 用于线上使用
        //blockFollowStaticCurrentComponent.blockFollowStaticInfo();

        //blockFollowStaticCurrentHistoryComponent.blockFollowStaticInfo();
       /* strongPlankDefineComponent.strongPlank();
        stockBlockLevelComponent.stockFirstBlockInfo();
        //buyTwoToThreeComponent.buyTwoThree();
        //buyTwoToThreeComponent.buyTwoThree();
        //plankAmountSuperTestComponent.plankAmountInfo();
        //blockFollowStaticCurrentComponent.blockFollowStaticInfo();
        //strongBlockExploreComponent.highLowPlank();
        List<String> list = Lists.newArrayList("000950","002767","002718","600624","603669","001308","603595","002992","002813","002864","002888","002077","600336","603329","603327","000533","000545","002317","002337","002339","600259","600192","600152","000404","002225","600101","002272","000722","603122","000755","600056","000709","002514","603191","002526","603161","002589","600868","600833","603969","603963","000601","000600");
        blockFollowTestComponent.blockFollowBuyStock(list);
        //tianDiPlankComponent.highLowPlank();

        //hangYeLevelComponent.calHangYeKbarToRedis();
        //hangYeLevelComponent.hangyeAmount();
        //hangYeLevelComponent.calHangYeKbarToRedis();
        //quanShangHighComponent.quanShangYiDong();
       // thsBlockKbarComponent.initHistoryBlockMinKbar();
        //blockFollowComponent.relativeWithSZInfo();
        //blockFollowComponent.searchTest();
        //blockFollowComponent.getHistoryBlockInfoTest();
        //thsBlockKbarComponent.initHistoryBlockKbars();
        //thsBlockKbarComponent.initHistoryBlockIndex();
        /*BigExchangeTestBuyDTO buyDTO = new BigExchangeTestBuyDTO();
        buyDTO.setTradeDate("20220425");
        bigAmountTestComponent.getStockOrder("601975","20220425",buyDTO);*/
        //bigAmountTestComponent.getStockOrder("000548","20220511");
        //bigAmountTestComponent.plankExchangeAmountInfo();
        //bigAmountTestComponent.getChartStr();
        //thsStockIndexComponent.blockMACDIndex();
        //macdCalComponent.quoteToKbar();
        //macdCalComponent.calMacd();
        //macdCalComponent.macdExcel();
        /*indexDValueComponent.calIndexDValue();*/
        //indexDValueComponent.dvalueExcel();
        //northSouthMoneyComponent.northMoney("","北向资金");
       // indexDValueComponent.dvalueExcel();
        //northSouthMoneyComponent.northMoney("","北向资金");
        //northSouthMoneyComponent.indexPercent("999999","上证指数成交额比例");
        //northSouthMoneyComponent.calHenShenIndex();
        //northSouthMoneyComponent.calHenShenStockKbar10();
        /*List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("999999", "20220602");
        System.out.println(data);*/
        //thsBlockKbarComponent.initHistoryBlockKbars();

       // buyGroundGatherComponent.gatherGround();
    }

    @Test
    public void test2() {
        stockKbarComponent.batchKbarDataInit();
    }


    @Test
    public void test3() {
        //thsQuoteSaveComponent.saveQuoteHuShen300QiHuo();
        //huShen300SecondKbarComponent.huShen300QuoteToKbar();
        //huShen300SecondKbarComponent.calMacdSave();

    }

    //沪深300 5sk线相关
    @Test
    public void test4() {
        thsQuoteSaveComponent.saveQuoteHuShen300QiHuo();
        //huShen300SecondKbarComponent.huShen300QuoteToKbar();
        //huShen300SecondKbarComponent.calMacdSave();
        //huShen300SecondKbarComponent.macdExcel();
    }

    //沪深300 3mink线相关
    @Test
    public void test5() {
        //huShen3003MinKbarComponent.huShen300QuoteToKbar();
       //huShen3003MinKbarComponent.calBiasSave();
        huShen3003MinKbarComponent.macdExcel();
    }


    //沪深300 1mink线相关
    @Test
    public void test6() {
        huShen3001MinKbarComponent.huShen300QuoteToKbar();
        //huShen3001MinKbarComponent.calBiasSave();
        //huShen3001MinKbarComponent.biasExcel();
    }

    @Test
    public void test7(){
        thsDataComponent.getOpenPicture("600704","2021-06-25");
    }
    @Test
    public void test10(){
        //用于4板块跟随回测 用于线上使用
        blockFollowStaticCurrent4PlankComponent.blockFollowStaticInfo();
        //用于3板块跟随回测 用于线上使用
        //blockFollowStaticCurrentComponent.blockFollowStaticInfo();
    }
    @Test
    public void test11(){
        stockFactorWuDiNewOnlineComponent.factorTest("20190101","20200101","index2a","2018");
    }

    @Test
    public void test12(){
        Date date = DateUtil.parseDate("20230214", DateUtil.yyyyMMdd);
        sendReplayInfoAutoComponent.sendStockKbarReplay(DateUtil.format(date,DateUtil.yyyyMMdd));
        //sendReplayInfoAutoComponent.sendStockCommonReplay(DateUtil.format(date,DateUtil.yyyyMMdd));
        sendReplayInfoAutoComponent.sendIndexDetail(DateUtil.format(date,DateUtil.yyyy_MM_dd));
        //sendReplayInfoAutoComponent.sendPlankExchangeDaily(DateUtil.format(date,DateUtil.yyyy_MM_dd));

        sendReplayInfoAutoComponent.sendHistoryBlockStocks(DateUtil.format(date,DateUtil.yyyyMMdd));
        sendReplayInfoAutoComponent.sendStockBolling(DateUtil.format(date,DateUtil.yyyyMMdd));
        sendReplayInfoAutoComponent.sendStockAttributeReplay(DateUtil.format(date,DateUtil.yyyyMMdd));
    }
}
