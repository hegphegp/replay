package com.bazinga.test;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.base.Sort;
import com.bazinga.component.ThsBlockStocksComponent;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.component.*;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.CirculateDetailDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.CirculateInfoQuery;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.CirculateInfoService;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.TradeDatePoolService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import jnr.ffi.annotations.In;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.python.core.PyFunction;
import org.python.core.PyInteger;
import org.python.core.PyObject;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
    @Autowired
    private PlankChenJiaoEComponent plankChenJiaoEComponent;
    @Autowired
    private BlockKbarComponent blockKbarComponent;
    @Autowired
    private HotBlockDropInfoComponent hotBlockDropInfoComponent;
    @Autowired
    private BadPlankComponent badPlankComponent;
    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;
    @Autowired
    private BlockKbarSelfComponent blockKbarSelfComponent;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private PlanksInfoComponent planksInfoComponent;
    @Autowired
    private StockAttributeReplayComponent stockAttributeReplayComponent;
    @Autowired
    private CirculateInfoService circulateInfoService;
    @Autowired
    private StockBollingComponent stockBollingComponent;
    @Autowired
    private StockListComponent stockListComponent;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private HistoryBlockInfoComponent historyBlockInfoComponent;
    @Autowired
    private CurrentDayTransactionDataComponent currentDayTransactionDataComponent;
    @Autowired
    private ThsStockIndexComponent thsStockIndexComponent;
    @Autowired
    private ThsBlockStocksComponent thsBlockStocksComponent;
    @Autowired
    private IndexKbarCurrentComponent indexKbarCurrentComponent;
    @Autowired
    private ThsCurrentQuoteSaveComponent thsCurrentQuoteSaveComponent;
    @Autowired
    private ThsBlockKbarComponent thsBlockKbarComponent;


    @Test
    public void test1() {
        //无敌数据
        Date date = new Date();
        newStockComponent.catchNewStock();
        plankChenJiaoEComponent.exportData();
        stockReplayDailyComponent.stockReplayDaily(date);
        stockReplayDailyComponent.calPreDateAvgPrice(date);
        stockPlankDailyComponent.stockPlankDailyStatistic(date);
        stockPlankDailyComponent.stockPlankDailyStatistic(date);
        stockPlankDailyComponent.stockPlankDailyStatistic(date);
        plankExchangeDailyComponent.plankExchangeDaily(date);
        stockKbarComponent.batchUpdateDaily();
        stockPlankDailyComponent.calMax100DaysPriceForTwoPlank(date);
        stockPlankDailyComponent.calMin15DaysPriceForTwoPlank(date);
        //stockPlankDaily添加次新标签
        stockPlankDailyComponent.calSubNewStock(date);
        //stockPlankDaily添加insertTime
        stockPlankDailyComponent.insertTime(date);
        //stockPlankDaily添加实际连板数
        stockPlankDailyComponent.realPlanks(date);
        stockPlankDailyComponent.middlePlanks(date);


        //新版复盘
        stockCommonReplayComponent.saveCommonReplay(date);
        stockCommonReplayComponent.firstPlankNoBuyInfo(date);
        stockCommonReplayComponent.highRaiseStockInfo(date);
        stockCommonReplayComponent.forTwoPlankWuDi(date);
        stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399300","沪深300指数",100);
        badPlankComponent.badPlankJudge(date);
        stockPlankDailyComponent.superFactor(date);
        stockKbarComponent.calCurrentDayAvgLine(date);
        stockAttributeReplayComponent.saveStockAttributeReplay(date);
        stockPlankDailyComponent.handleStopTradeStock(date);
        stockBollingComponent.calCurrentDayBoll(date);
        thsBlockStocksComponent.indexBLockDetail();
        historyBlockInfoComponent.initHistoryBlockInfo();
        indexKbarCurrentComponent.indexStockKbarSend();
        indexKbarCurrentComponent.stockIndexSend();
        thsCurrentQuoteSaveComponent.saveHS300FutureQuoteIndex(DateUtil.format(new Date(),DateUtil.yyyy_MM_dd));
        /*blockKbarComponent.thsBlockKbar(DateTimeUtils.getDate000000(date));
        hotBlockDropInfoComponent.thsBlockKbar(DateTimeUtils.getDate000000(date));*/
    }

    @Test
    public void test2() {
        synInfoComponent.synCirculateInfo();
    }

    @Test
    public void test4() {
        synInfoComponent.synHotCirculateInfo();
    }

    @Test
    public void test8() {
        synInfoComponent.synMarketInfoZz500();
    }

    @Test
    public void test5() {
        synInfoComponent.synZiDingYiInfo();
    }

    @Test
    public void test6() {
        synInfoComponent.synTbondInfo();
    }

    @Test
    public void test9() {
        stockKbarComponent.initSpecialStockAndSaveKbarData("399300","沪深300指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        String dateyyyyMMhh = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
        //String dateyyyyMMhh = "20221209";
        thsStockIndexComponent.shMACDIndex(dateyyyyMMhh,"399300","沪深300",".SZ");
        thsStockIndexComponent.shMACDIndex(dateyyyyMMhh,"000001","上证指数",".SH");
    }

    @Test
    public void test11() {
        historyBlockInfoComponent.getPreBlockStocks("20221228","20221229");
    }
    @Test
    public void test10(){
        CirculateInfoQuery circulateInfoQuery = new CirculateInfoQuery();
        List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(circulateInfoQuery);
        int i= 0;
        for (CirculateInfo item:circulateInfos){
            i++;
            System.out.println(i);
            stockKbarComponent.batchKbarDataInitToStock(item.getStockCode(),item.getStockName());
        }
    }

    @Test
    public void test7() {
        thsBlockStocksComponent.indexBLockDetail();
        //thsCurrentQuoteSaveComponent.saveHS300FutureQuoteIndex("2022-12-15");
        //historyBlockInfoComponent.initHistoryBlockInfo();
        /*stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399300","沪深300指数",100);*/
        /*stockKbarComponent.initSpecialStockAndSaveKbarData("000016","上证50",1200);
        stockKbarComponent.initSpecialStockAndSaveKbarData("000852","中证1000",1200);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500",1200);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399903","中证100",1200);*/
        /*String dateyyyyMMhh = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
        thsStockIndexComponent.shMACDIndex(dateyyyyMMhh,"399300","沪深300",".SZ");
        thsStockIndexComponent.shMACDIndex(dateyyyyMMhh,"000001","上证指数",".SH");*/
        /*Date date = new Date();
        long time = date.getTime();
        System.out.println(time);
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("002121", "20220610");
        List<ThirdSecondTransactionDataDTO> data1 = currentDayTransactionDataComponent.getData("002121");
        System.out.println(111);*/
        //synInfoComponent.synHSTECH();
        /*historyBlockInfoComponent.initHistoryBlockInfo();
        stockKbarComponent.batchKbarDataInit();
       // stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",1500);
        historyBlockInfoComponent.initHistoryBlockInfo();
        /*stockKbarComponent.batchKbarDataInit();
        stockListComponent.getCirculateInfo(new Date());*/
       /* StockKbarQuery kbarQuery = new StockKbarQuery();
        kbarQuery.setStockCode("000001");
        List<StockKbar> stockKbars = stockKbarService.listByCondition(kbarQuery);
        System.out.println(stockKbars);*/

        //stockKbarComponent.batchKbarDataInit();
        //stockKbarComponent.initSpecialStockAndSaveKbarData("399006","创业扳指",1500);
        //String[] arguments = new String[] {"python", "D://dashuju/add.py","20","52"};
        //String[] arguments = new String[] {"python", "D://dashuju/gb_main_test.py","5.91","0","35765222","3.51","9:32","95209936","53","0","1.4","-5","-3.88","-3.72","-10.94","34","1.05","3.51"};
        /*String[] arguments = new String[] {"python", "gb_main_test.py","--data_list 5.91 0 35765222 3.51 9:32 95209936 53 0 1.4 -5 -3.88 -3.72 -10.94 34 1.05 3.51"};

        try {
            Process process = Runtime.getRuntime().exec(arguments);
            BufferedReader in = new BufferedReader(new InputStreamReader(process.getInputStream(),
                    "utf-8"));
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line+"jjjjjjjjjjjj");
            }
            in.close();
            //java代码中的process.waitFor()返回值为0表示我们调用python脚本成功，
            //返回值为1表示调用python脚本失败，这和我们通常意义上见到的0与1定义正好相反
            int re = process.waitFor();
            System.out.println(re+"============");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        /*Map<String, AdjFactorDTO> stocks = stockListComponent.getStocks();
        System.out.println(stocks);*/


        /*PythonInterpreter interpreter = new PythonInterpreter();

        interpreter.execfile("D:\\dashuju\\gb_main.py");
        PyFunction pyFunction = interpreter.get("gb_main", PyFunction.class);
        PyString[] pyArr = new PyString[]{new PyString("5.91"), new PyString("0"), new PyString("35765222"), new PyString(" 3.51"), new PyString("9:32"), new PyString("95209936"), new PyString("53"), new PyString("0"), new PyString("1.4"), new PyString("-5")
                , new PyString("-3.88"), new PyString("-3.72"), new PyString("-10.94"), new PyString("34"), new PyString("1.05"), new PyString("3.51")};
        PyObject pyObject = pyFunction.__call__(pyArr);
        System.out.println(JSONObject.toJSONString(pyObject));*/
       /* interpreter.execfile("D:\\testPy\\add.py");
        PyFunction pyFunction = interpreter.get("add", PyFunction.class);
        int a = 15; int b = 20;
        PyObject pyObject = pyFunction.__call__(new PyInteger(a), new PyInteger(b));
        System.out.println(pyObject);*/


        //stockAttributeReplayComponent.saveStockAttributeReplay(DateUtil.parseDate("2022-04-08 15:30:00",DateUtil.DEFAULT_FORMAT));
        /*DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, "000001", 0, 100);
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("000001", "20220401");
        System.out.println(securityBars);*/
        //stockAttributeReplayComponent.saveStockAttributeReplay(new Date());
        //stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",1300);
        //stockKbarComponent.calAvgLine("999999","上证指数",24);
        //stockKbarComponent.calCurrentDayAvgLine(new Date());
        //stockAttributeReplayComponent.saveStockAttributeReplay(new Date());
        //plankChenJiaoEComponent.exportData();
        //stockKbarComponent.batchKbarDataInit();
        /*Date date = new Date();
        stockPlankDailyComponent.handleStopTradeStock(date);*/
        //stockAttributeReplayComponent.saveStockAttributeReplay(DateUtil.parseDate("2022-02-18 15:30:30",DateUtil.DEFAULT_FORMAT));
        //stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500指数",100);
        //synInfoComponent.synZiDingYiInfo();
        //stockCommonReplayComponent.highRaiseStockInfo(new Date());
        //List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNewDays("601068", 50, 11);
        /*List<CirculateInfo> circulateInfos = circulateInfoService.listByCondition(new CirculateInfoQuery());
        for (CirculateInfo circulateInfo:circulateInfos) {
            System.out.println(circulateInfo.getStockCode());
            stockKbarComponent.batchKbarDataInitToStock(circulateInfo.getStockCode(), circulateInfo.getStockName());
        }
        stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500指数",100);*/



    }


    @Test
    public void test3() {
        //stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        //stockKbarComponent.initSpecialStockAndSaveKbarData("399300","沪深300",1200);
        //stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500指数",100);
        //stockKbarComponent.initSpecialStockAndSaveKbarData("399001","深圳成指",1500);
        //stockKbarComponent.calCurrentDayAvgLine(DateUtil.parseDate("2022-04-18 15:30:30",DateUtil.DEFAULT_FORMAT));
        /*DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, "000001", 0, 800);
        stockPlankDailyComponent.middlePlanks(new Date());*/
       /* stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        stockKbarComponent.initSpecialStockAndSaveKbarData("399905","中证500指数",100);*/
        //stockKbarComponent.batchKbarDataInit();
        //newStockComponent.catchNewStock();
        //stockKbarComponent.initSpecialStockAndSaveKbarData("999999","上证指数",100);
        //stockPlankDailyComponent.calMin15DaysPriceForTwoPlank(DateUtil.parseDate("20210910",DateUtil.yyyyMMdd));
       //stockPlankDailyComponent.superFactor(DateUtil.parseDate("20210910",DateUtil.yyyyMMdd));
        //stockKbarComponent.initSpecialStockAndSaveKbarData("880863","昨日涨停",100);
        /*List<StockKbar> kbars = stockKbarComponent.getStockKBarRemoveNew("605588", 3, 50);
        System.out.println(kbars);*/
        //blockKbarComponent.thsBlockKbar(DateUtil.parseDate("20211018",DateUtil.yyyyMMdd));
        //hotBlockDropInfoComponent.thsBlockKbar(DateUtil.parseDate("20211018",DateUtil.yyyyMMdd));
        //hotBlockDropInfoComponent.getAvgPrice();
        //badPlankComponent.badPlankJudge(new Date());
        /*DataTable securityBars = TdxHqUtil.getSecurityBars(KCate.DAY, "128094", 0,100);
        List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("123048", "20211102");
        System.out.println(data);*/
        //blockKbarSelfComponent.initBlockKbarSelf();
        //stockPlankDailyComponent.stockPlankDailyStatistic(new Date());
        /*TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.setTradeDateFrom(DateUtil.parseDate("20210101",DateUtil.yyyyMMdd));
        query.setTradeDateTo(DateUtil.parseDate("20211122",DateUtil.yyyyMMdd));
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        for (TradeDatePool tradeDatePool:tradeDatePools) {
            System.out.println(tradeDatePool);
            //blockKbarComponent.thsBlockKbar(tradeDatePool.getTradeDate());
        }*/
        //blockKbarComponent.thsBlockKbarGatherQuantity();
        //planksInfoComponent.planksInfo(DateUtil.parseDate("20211206",DateUtil.yyyyMMdd));
        //stockPlankDailyComponent.realPlanks(new Date());

        //stockKbarComponent.batchKbarDataInit();


    }
    @Test
    public void test29() {
        //thsBlockKbarComponent.getBlockKbarThs("883949","陆股通清仓",".TI","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("399905",".SZ","中证500","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883913",".TI","龙虎榜指数","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("399903",".SZ","中证100","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883900",".TI","昨日涨停表现","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883905",".TI","昨日换手前十","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883920",".TI","近期解禁","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883963",".TI","国家队减持","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("399001",".SZ","深圳成指","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883917",".TI","行业龙头","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883404",".TI","同花顺情绪指数","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883939",".TI","陆股通持续净买入","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("000852",".SH","中证1000","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883901",".TI","昨日资金前十","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883902",".TI","昨日成交前十","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883918",".TI","昨日炸板股","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883906",".TI","昨日高振幅","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883962",".TI","国家队增持","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883400",".TI","昨日ST首板股表现","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883911",".TI","创历史新高","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("399006",".SZ","创业板指","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883979",".TI","昨日首板表现","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883912",".TI","深股通成交前十","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("000001",".SH","上证指数","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("USDCNH",".FX","人民币离岸汇率","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("000016",".SH","上证50","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("IXIC",".GI","纳斯达克指数","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883908",".TI","沪股通成交前十","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883904",".TI","增发募集指数","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883910",".TI","同花顺热股","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("883958",".TI","昨日连板","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("HSI",".HK","恒生指数","2017-01-01","2022-12-23");
        //thsBlockKbarComponent.getBlockKbarThs("399300",".SZ","沪深300","2017-01-01","2022-12-23");



        //thsStockIndexComponent.shMACDIndex("20221118","000001","上证指数",".SH");
        //thsStockIndexComponent.shMACDIndex("20221118","399006","创业板指",".SZ");
        //thsStockIndexComponent.shMACDIndex("20221118","399300","沪深300",".SZ");
        thsStockIndexComponent.shMACDIndex("20221118","399905","中证500",".SZ");
        thsStockIndexComponent.shMACDIndex("20221118","000016","上证50",".SH");
        thsStockIndexComponent.shMACDIndex("20221118","399001","深圳成指",".SZ");
        thsStockIndexComponent.shMACDIndex("20221118","000852","中证1000",".SH");
        thsStockIndexComponent.shMACDIndex("20221118","399903","中证100",".SZ");

    }

}
