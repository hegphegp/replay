package com.bazinga.replay.component;
import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.model.StockIndex;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.util.DateUtil;
import com.bazinga.util.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class IndexKbarCurrentComponent {
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private ThsStockIndexComponent thsStockIndexComponent;
    @Autowired
    private ThsBlockKbarComponent thsBlockKbarComponent;

    public void indexKbarCurrent(){
        BigDecimal openPriceSZ = stockKbarComponent.calCurrentIndexKbarOpenPrice("999999", "上证指数", 1);
        BigDecimal openPriceHS300 = stockKbarComponent.calCurrentIndexKbarOpenPrice("399300", "沪深300指数", 1);
        sendHttp("999999", "上证指数", openPriceSZ);
        sendHttp("399300", "沪深300指数", openPriceHS300);
    }

    public void indexKbarCurrentNew(){
        String yyyyMMdd = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
       // BigDecimal stockKbar999999 = thsBlockKbarComponent.getBlockKbarThsCurrent("000001",".SH","上证指数", yyyyMMdd);
        //BigDecimal stockKbar399300 = thsBlockKbarComponent.getBlockKbarThsCurrent("399300",".SZ","沪深300", yyyyMMdd);
        BigDecimal stockKbar399006 = thsBlockKbarComponent.getBlockKbarThsCurrent("399006",".SZ","创业板指", yyyyMMdd);
        BigDecimal stockKbar883904 = thsBlockKbarComponent.getBlockKbarThsCurrent("883904",".TI","增发募集指数", yyyyMMdd);
        BigDecimal stockKbar000016 = thsBlockKbarComponent.getBlockKbarThsCurrent("000016",".SH","上证50", yyyyMMdd);
        BigDecimal stockKbar399001 = thsBlockKbarComponent.getBlockKbarThsCurrent("399001",".SZ","深圳成指", yyyyMMdd);
        BigDecimal stockKbar883911 = thsBlockKbarComponent.getBlockKbarThsCurrent("883911",".TI","创历史新高", yyyyMMdd);
        BigDecimal stockKbar883906 = thsBlockKbarComponent.getBlockKbarThsCurrent("883906",".TI","昨日高振幅", yyyyMMdd);
        BigDecimal stockKbar000852 = thsBlockKbarComponent.getBlockKbarThsCurrent("000852",".SH","中证1000", yyyyMMdd);
        BigDecimal stockKbar883913 = thsBlockKbarComponent.getBlockKbarThsCurrent("883913",".TI","龙虎榜指数", yyyyMMdd);
        BigDecimal stockKbar883910 = thsBlockKbarComponent.getBlockKbarThsCurrent("883910",".TI","同花顺热股", yyyyMMdd);
        BigDecimal stockKbar883901 = thsBlockKbarComponent.getBlockKbarThsCurrent("883901",".TI","昨日资金前十", yyyyMMdd);
        BigDecimal stockKbar399905 = thsBlockKbarComponent.getBlockKbarThsCurrent("399905",".SZ","中证500", yyyyMMdd);

        /*BigDecimal stockKbar883949 = thsBlockKbarComponent.getBlockKbarThsCurrent("883949", ".TI", "陆股通清仓",yyyyMMdd);
        BigDecimal stockKbar399905 = thsBlockKbarComponent.getBlockKbarThsCurrent("399905", ".SZ", "中证500", yyyyMMdd);
        BigDecimal stockKbar883913 = thsBlockKbarComponent.getBlockKbarThsCurrent("883913",".TI","龙虎榜指数", yyyyMMdd);
        BigDecimal stockKbar399903 = thsBlockKbarComponent.getBlockKbarThsCurrent("399903",".SZ","中证100", yyyyMMdd);
        BigDecimal stockKbar883900 = thsBlockKbarComponent.getBlockKbarThsCurrent("883900",".TI","昨日涨停表现", yyyyMMdd);
        BigDecimal stockKbar883905 = thsBlockKbarComponent.getBlockKbarThsCurrent("883905",".TI","昨日换手前十", yyyyMMdd);
        BigDecimal stockKbar883920 = thsBlockKbarComponent.getBlockKbarThsCurrent("883920",".TI","近期解禁",yyyyMMdd);
        BigDecimal stockKbar883963 = thsBlockKbarComponent.getBlockKbarThsCurrent("883963",".TI","国家队减持", yyyyMMdd);
        BigDecimal stockKbar883917 = thsBlockKbarComponent.getBlockKbarThsCurrent("883917",".TI","行业龙头", yyyyMMdd);
        BigDecimal stockKbar883404 = thsBlockKbarComponent.getBlockKbarThsCurrent("883404",".TI","同花顺情绪指数", yyyyMMdd);
        BigDecimal stockKbar883939 = thsBlockKbarComponent.getBlockKbarThsCurrent("883939",".TI","陆股通持续净买入", yyyyMMdd);
        BigDecimal stockKbar000852 = thsBlockKbarComponent.getBlockKbarThsCurrent("000852",".SH","中证1000", yyyyMMdd);
        BigDecimal stockKbar883901 = thsBlockKbarComponent.getBlockKbarThsCurrent("883901",".TI","昨日资金前十", yyyyMMdd);
        BigDecimal stockKbar883902 = thsBlockKbarComponent.getBlockKbarThsCurrent("883902",".TI","昨日成交前十", yyyyMMdd);
        BigDecimal stockKbar883918 = thsBlockKbarComponent.getBlockKbarThsCurrent("883918",".TI","昨日炸板股", yyyyMMdd);
        BigDecimal stockKbar883962 = thsBlockKbarComponent.getBlockKbarThsCurrent("883962",".TI","国家队增持", yyyyMMdd);
        BigDecimal stockKbar883400 = thsBlockKbarComponent.getBlockKbarThsCurrent("883400",".TI","昨日ST首板股表现", yyyyMMdd);
        BigDecimal stockKbar883979 = thsBlockKbarComponent.getBlockKbarThsCurrent("883979",".TI","昨日首板表现", yyyyMMdd);
        BigDecimal stockKbar883912 = thsBlockKbarComponent.getBlockKbarThsCurrent("883912",".TI","深股通成交前十", yyyyMMdd);

        BigDecimal stockKbarUSDCNH = thsBlockKbarComponent.getBlockKbarThsCurrent("USDCNH",".FX","人民币离岸汇率", yyyyMMdd);
        BigDecimal stockKbarIXIC = thsBlockKbarComponent.getBlockKbarThsCurrent("IXIC",".GI","纳斯达克指数", yyyyMMdd);
        BigDecimal stockKbar883908 = thsBlockKbarComponent.getBlockKbarThsCurrent("883908",".TI","沪股通成交前十", yyyyMMdd);
        BigDecimal stockKbar883910 = thsBlockKbarComponent.getBlockKbarThsCurrent("883910",".TI","同花顺热股", yyyyMMdd);
        BigDecimal stockKbar883958 = thsBlockKbarComponent.getBlockKbarThsCurrent("883958",".TI","昨日连板", yyyyMMdd);
        BigDecimal stockKbarHSI = thsBlockKbarComponent.getBlockKbarThsCurrent("HSI",".HK","恒生指数", yyyyMMdd);*/
        System.out.println(111);
        //sendHttp("999999","上证指数", stockKbar999999);
        //sendHttp("399300","沪深300", stockKbar399300);
        sendHttp("883906","昨日高振幅", stockKbar883906);
        sendHttp("883911","创历史新高", stockKbar883911);
        sendHttp("399006","创业板指", stockKbar399006);
        sendHttp("399001","深圳成指", stockKbar399001);
        sendHttp("000016","上证50", stockKbar000016);
        sendHttp("883904","增发募集指数", stockKbar883904);

        sendHttp("000852","中证1000", stockKbar000852);
        sendHttp("883913","龙虎榜指数", stockKbar883913);
        sendHttp("883910","同花顺热股", stockKbar883910);
        sendHttp("883901","昨日资金前十", stockKbar883901);
        sendHttp("399905","中证500", stockKbar399905);


        /*sendHttp("883949", "陆股通清仓",stockKbar883949);
        sendHttp("399905", "中证500", stockKbar399905);
        sendHttp("883913","龙虎榜指数", stockKbar883913);
        sendHttp("399903","中证100", stockKbar399903);
        sendHttp("883900","昨日涨停表现", stockKbar883900);
        sendHttp("883905","昨日换手前十", stockKbar883905);
        sendHttp("883920","近期解禁",stockKbar883920);
        sendHttp("883963","国家队减持", stockKbar883963);
        sendHttp("883917","行业龙头", stockKbar883917);
        sendHttp("883404","同花顺情绪指数", stockKbar883404);
        sendHttp("883939","陆股通持续净买入", stockKbar883939);
        sendHttp("000852","中证1000", stockKbar000852);
        sendHttp("883901","昨日资金前十", stockKbar883901);
        sendHttp("883902","昨日成交前十", stockKbar883902);
        sendHttp("883918","昨日炸板股", stockKbar883918);
        sendHttp("883962","国家队增持", stockKbar883962);
        sendHttp("883400","昨日ST首板股表现", stockKbar883400);
        sendHttp("883979","昨日首板表现", stockKbar883979);
        sendHttp("883912","深股通成交前十", stockKbar883912);
        sendHttp("USDCNH","人民币离岸汇率", stockKbarUSDCNH);
        sendHttp("IXIC",  "纳斯达克指数", stockKbarIXIC);
        sendHttp("883908","沪股通成交前十", stockKbar883908);
        sendHttp("883910","同花顺热股", stockKbar883910);
        sendHttp("883958","昨日连板", stockKbar883958);
        sendHttp("HSI",   "恒生指数", stockKbarHSI);*/

    }

    public void sendHttp(String stockCode,String stockName,BigDecimal openPrice){
        if(openPrice==null){
            return;
        }
        String url = "http://120.26.85.183:8080/wave/replay/kbarOpenPrice";
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockCode);
        map.put("stockName",stockName);
        map.put("openPrice",openPrice.toString());
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, map);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        log.info("调用wave结果 stockCode:{} result：{}",stockCode,result);
    }


    /*public void indexStockKbarSend(){
        String yyyyMMdd = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
        StockKbar stockKbarSZ = stockKbarComponent.initSpecialStockKbarRecentTradeDate("999999", "上证指数", yyyyMMdd);
        StockKbar stockKbarHS = stockKbarComponent.initSpecialStockKbarRecentTradeDate("399300", "沪深300指数", yyyyMMdd);
        sendHttpStockKbar(stockKbarSZ);
        sendHttpStockKbar(stockKbarHS);
    }*/

    public void indexStockKbarSend(Date date){
        //Date date = DateUtil.parseDate("20221230",DateUtil.yyyyMMdd);
        String yyyyMMdd = DateUtil.format(date, DateUtil.yyyyMMdd);
        String yyyy_MM_dd = DateUtil.format(date, DateUtil.yyyy_MM_dd);
        StockKbar stockKbar883949 = thsBlockKbarComponent.getBlockKbarThsDay("883949", ".TI", "陆股通清仓", yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar399905 = thsBlockKbarComponent.getBlockKbarThsDay("399905", ".SZ", "中证500", yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883913 = thsBlockKbarComponent.getBlockKbarThsDay("883913",".TI","龙虎榜指数",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar399903 = thsBlockKbarComponent.getBlockKbarThsDay("399903",".SZ","中证100",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883900 = thsBlockKbarComponent.getBlockKbarThsDay("883900",".TI","昨日涨停表现",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883905 = thsBlockKbarComponent.getBlockKbarThsDay("883905",".TI","昨日换手前十",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883920 = thsBlockKbarComponent.getBlockKbarThsDay("883920",".TI","近期解禁",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883963 = thsBlockKbarComponent.getBlockKbarThsDay("883963",".TI","国家队减持",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar399001 = thsBlockKbarComponent.getBlockKbarThsDay("399001",".SZ","深圳成指",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883917 = thsBlockKbarComponent.getBlockKbarThsDay("883917",".TI","行业龙头",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883404 = thsBlockKbarComponent.getBlockKbarThsDay("883404",".TI","同花顺情绪指数",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883939 = thsBlockKbarComponent.getBlockKbarThsDay("883939",".TI","陆股通持续净买入",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar000852 = thsBlockKbarComponent.getBlockKbarThsDay("000852",".SH","中证1000",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883901 = thsBlockKbarComponent.getBlockKbarThsDay("883901",".TI","昨日资金前十",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883902 = thsBlockKbarComponent.getBlockKbarThsDay("883902",".TI","昨日成交前十",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883918 = thsBlockKbarComponent.getBlockKbarThsDay("883918",".TI","昨日炸板股",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883906 = thsBlockKbarComponent.getBlockKbarThsDay("883906",".TI","昨日高振幅",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883962 = thsBlockKbarComponent.getBlockKbarThsDay("883962",".TI","国家队增持",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883400 = thsBlockKbarComponent.getBlockKbarThsDay("883400",".TI","昨日ST首板股表现",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883911 = thsBlockKbarComponent.getBlockKbarThsDay("883911",".TI","创历史新高",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar399006 = thsBlockKbarComponent.getBlockKbarThsDay("399006",".SZ","创业板指",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883979 = thsBlockKbarComponent.getBlockKbarThsDay("883979",".TI","昨日首板表现",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883912 = thsBlockKbarComponent.getBlockKbarThsDay("883912",".TI","深股通成交前十",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar999999 = thsBlockKbarComponent.getBlockKbarThsDay("000001",".SH","上证指数",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbarUSDCNH = thsBlockKbarComponent.getBlockKbarThsDay("USDCNH",".FX","人民币离岸汇率",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar000016 = thsBlockKbarComponent.getBlockKbarThsDay("000016",".SH","上证50",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbarIXIC = thsBlockKbarComponent.getBlockKbarThsDay("IXIC",".GI","纳斯达克指数",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883908 = thsBlockKbarComponent.getBlockKbarThsDay("883908",".TI","沪股通成交前十",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883904 = thsBlockKbarComponent.getBlockKbarThsDay("883904",".TI","增发募集指数",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883910 = thsBlockKbarComponent.getBlockKbarThsDay("883910",".TI","同花顺热股",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar883958 = thsBlockKbarComponent.getBlockKbarThsDay("883958",".TI","昨日连板",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbarHSI = thsBlockKbarComponent.getBlockKbarThsDay("HSI",".HK","恒生指数",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        StockKbar stockKbar399300 = thsBlockKbarComponent.getBlockKbarThsDay("399300",".SZ","沪深300",yyyy_MM_dd, yyyy_MM_dd, yyyyMMdd);
        sendHttpStockKbar(stockKbar883949);
        sendHttpStockKbar(stockKbar399905);
        sendHttpStockKbar(stockKbar883913);
        sendHttpStockKbar(stockKbar399903);
        sendHttpStockKbar(stockKbar883900);
        sendHttpStockKbar(stockKbar883905);
        sendHttpStockKbar(stockKbar883920);
        sendHttpStockKbar(stockKbar883963);
        sendHttpStockKbar(stockKbar399001);
        sendHttpStockKbar(stockKbar883917);
        sendHttpStockKbar(stockKbar883404);
        sendHttpStockKbar(stockKbar883939);
        sendHttpStockKbar(stockKbar000852);
        sendHttpStockKbar(stockKbar883901);
        sendHttpStockKbar(stockKbar883902);
        sendHttpStockKbar(stockKbar883918);
        sendHttpStockKbar(stockKbar883906);
        sendHttpStockKbar(stockKbar883962);
        sendHttpStockKbar(stockKbar883400);
        sendHttpStockKbar(stockKbar883911);
        sendHttpStockKbar(stockKbar399006);
        sendHttpStockKbar(stockKbar883979);
        sendHttpStockKbar(stockKbar883912);
        sendHttpStockKbar(stockKbar999999);
        sendHttpStockKbar(stockKbarUSDCNH);
        sendHttpStockKbar(stockKbar000016);
        sendHttpStockKbar(stockKbarIXIC);
        sendHttpStockKbar(stockKbar883908);
        sendHttpStockKbar(stockKbar883904);
        sendHttpStockKbar(stockKbar883910);
        sendHttpStockKbar(stockKbar883958);
        sendHttpStockKbar(stockKbarHSI);
        sendHttpStockKbar(stockKbar399300);;
    }

    public void indexStockKbarManualSend(String stockCode,String stockName,String tradeDate,
                                         BigDecimal open,BigDecimal close,BigDecimal high,BigDecimal low,Long volume,BigDecimal amount){
        StockKbar stockKbar = thsBlockKbarComponent.getManualBlockKbarThsDay(stockCode,stockName,tradeDate,open,high,low,close,volume,amount);
        sendHttpStockKbar(stockKbar);
    }

    public void sendHttpStockKbar(StockKbar stockKbar){
        if(stockKbar==null){
            return;
        }
        String url = "http://120.26.85.183:8080/wave/basicInfo/saveStockKbar";
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockKbar.getStockCode());
        map.put("stockName",stockKbar.getStockName());
        map.put("tradeDate",stockKbar.getKbarDate());
        map.put("openPrice",stockKbar.getOpenPrice().toString());
        map.put("closePrice",stockKbar.getClosePrice().toString());
        map.put("highPrice",stockKbar.getHighPrice().toString());
        map.put("lowPrice",stockKbar.getLowPrice().toString());
        map.put("tradeQuantity",stockKbar.getTradeQuantity().toString());
        map.put("tradeAmount",stockKbar.getTradeAmount().toString());
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, map);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        log.info("调用wave结果 stockCode:{} result：{}",stockKbar.getStockCode(),result);
    }

    public void stockIndexSend(){
        String dateyyyyMMhh = DateUtil.format(new Date(), DateUtil.yyyyMMdd);
        //String dateyyyyMMhh = "20221230";
        StockIndex stockIndex399300 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"399300","沪深300",".SZ");
        StockIndex stockIndex000001 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"000001","上证指数",".SH");
        StockIndex stockIndex399006 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"399006","创业板指",".SZ");
        StockIndex stockIndex399905 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"399905","中证500",".SZ");
        StockIndex stockIndex000016 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"000016","上证50",".SH");
        StockIndex stockIndex399001 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"399001","深圳成指",".SZ");
        StockIndex stockIndex000852 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"000852","中证1000",".SH");
        StockIndex stockIndex399903 = thsStockIndexComponent.shMacdIndexNoSave(dateyyyyMMhh,"399903","中证100",".SZ");

        sendHttpStockIndex( stockIndex399300);
        sendHttpStockIndex( stockIndex000001);
        sendHttpStockIndex( stockIndex399006);
        sendHttpStockIndex( stockIndex399905);
        sendHttpStockIndex( stockIndex000016);
        sendHttpStockIndex( stockIndex399001);
        sendHttpStockIndex( stockIndex000852);
        sendHttpStockIndex( stockIndex399903);

    }

    public void sendHttpStockIndex(StockIndex stockIndex){
        String url = "http://120.26.85.183:8080/wave/basicInfo/saveStockIndex";
        Map<String, String> map = new HashMap<>();
        map.put("stockCode",stockIndex.getStockCode());
        map.put("stockName",stockIndex.getStockName());
        map.put("tradeDate",stockIndex.getKbarDate());
        map.put("macd",stockIndex.getMacd().toString());
        map.put("diff",stockIndex.getDiff().toString());
        map.put("dea",stockIndex.getDea().toString());
        map.put("bias6",stockIndex.getBias6().toString());
        map.put("bias12",stockIndex.getBias12().toString());
        map.put("bias24",stockIndex.getBias24().toString());
        String result = null;
        try {
            result = HttpUtil.sendHttpGet(url, map);
        } catch (Exception e) {
            log.info(e.getMessage(),e);
        }
        log.info("调用wave结果 stockCode:{} result：{}",stockIndex.getStockCode(),result);
    }

}
