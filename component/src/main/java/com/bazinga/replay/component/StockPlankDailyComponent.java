package com.bazinga.replay.component;

import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.service.CirculateInfoAllService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class StockPlankDailyComponent {
    @Autowired
    private StockPlankDailyService stockPlankDailyService;
    @Autowired
    private CirculateInfoAllComponent circulateInfoAllComponent;


    public void stockPlankDailyStatistic(Date date){
        List<CirculateInfoAll> circulateInfoAlls = circulateInfoAllComponent.getMainAndGrowth();
        for (CirculateInfoAll circulateInfoAll:circulateInfoAlls){
            String stockCode = circulateInfoAll.getStock();
            String stockName = circulateInfoAll.getStockName();

        }
    }


    public List<KBarDTO> getStockKBars(CirculateInfoAll circulateInfo){
        try {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStock(), 0, 300);
            List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
            List<KBarDTO> list = kbarDtoHandleComponent.deleteNewStockTimes(kbars, 300, circulateInfo.getStock());
            return list;
        }catch (Exception e){
            return null;
        }
    }


}
