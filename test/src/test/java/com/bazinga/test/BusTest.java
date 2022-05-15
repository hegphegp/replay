package com.bazinga.test;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.component.DragonTigerDailyComponent;
import com.bazinga.replay.component.HistoryTransactionDataComponent;
import com.bazinga.replay.component.StockKbarComponent;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.replay.model.ShStockOrder;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.service.ShStockOrderService;
import com.bazinga.util.DateUtil;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;
import java.util.List;

public class BusTest extends BaseTestCase {

    @Autowired
    private DragonTigerDailyComponent dragonTigerDailyComponent;

    @Autowired
    private HistoryTransactionDataComponent historyTransactionDataComponent;

    @Autowired
    private StockKbarComponent stockKbarComponent;

    @Autowired
    private ShStockOrderService shStockOrderService;

    @Test
    public void test(){
       // stockKbarComponent.batchUpdateDaily();

      //  dragonTigerDailyComponent.save2Db();
        List<ShStockOrder> byDateTrade = shStockOrderService.getByDateTrade(DateUtil.parseDate("20220511",DateUtil.yyyyMMdd));
        System.out.println(JSONObject.toJSONString(byDateTrade));
      /*  List<ThirdSecondTransactionDataDTO> data = historyTransactionDataComponent.getData("113615", "20211103");
        System.out.println(JSONObject.toJSONString(data));*/
    }
}
