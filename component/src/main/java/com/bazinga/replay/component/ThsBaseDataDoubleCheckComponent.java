package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.exception.BusinessException;
import com.bazinga.queue.LimitQueue;
import com.bazinga.replay.dto.*;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.*;
import com.bazinga.replay.service.*;
import com.bazinga.replay.util.PoiExcelUtil;
import com.bazinga.util.DateUtil;
import com.bazinga.util.Excel2JavaPojoUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class ThsBaseDataDoubleCheckComponent {
    @Autowired
    private ThsCirculateInfoService thsCirculateInfoService;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private ThsStockKbarService thsStockKbarService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;
    @Autowired
    private ThsCirculateInfoComponent thsCirculateInfoComponent;
    @Autowired
    private ThsStockKbarComponent thsStockKbarComponent;

    public void checkOutCirculateAndThsCirculate(String tradeDate){
        List<OutCirculateExcelDTO> excelDTOS = synOutCirculates();
        List<ThsCirculateInfo> thsCirculates = thsCirculateInfoComponent.getMarketACirculate(tradeDate);
        Map<String, OutCirculateExcelDTO> excelDTOMap = excelDTOS.stream().collect(Collectors.toMap(OutCirculateExcelDTO::getStockCode, outCirculateExcelDTO -> outCirculateExcelDTO));
        Map<String, ThsCirculateInfo> thsCirculateInfoMap = thsCirculates.stream().collect(Collectors.toMap(ThsCirculateInfo::getStockCode, thsCirculateInfo -> thsCirculateInfo));
        for (OutCirculateExcelDTO excelDTO:excelDTOS){
            ThsCirculateInfo circulateInfo = thsCirculateInfoMap.get(excelDTO.getStockCode());
            if(circulateInfo==null){
                System.out.println(excelDTO.getStockCode()+" === "+excelDTO.getStockName()+" === excelHave");
            }
        }
        for (ThsCirculateInfo thsCirculateInfo:thsCirculates){
            OutCirculateExcelDTO excelDTO = excelDTOMap.get(thsCirculateInfo.getStockCode());
            if (excelDTO==null){
                System.out.println(thsCirculateInfo.getStockCode()+" *** "+thsCirculateInfo.getStockName()+" *** thsCirculateHave");
            }
        }
    }

    public void checkDateKbarWithCirculate(String tradeDateStr){
        List<ThsCirculateInfo> circulateInfos = thsCirculateInfoComponent.getMarketACirculate(tradeDateStr);
        List<ThsStockKbar> stockKbars = thsStockKbarComponent.getDayStockKbars(tradeDateStr);
        Map<String, ThsCirculateInfo> circulateInfoMap = circulateInfos.stream().collect(Collectors.toMap(ThsCirculateInfo::getStockCode, thsCirculateInfo -> thsCirculateInfo));
        Map<String, ThsStockKbar> stockKbarMap = stockKbars.stream().collect(Collectors.toMap(ThsStockKbar::getStockCode, stockKbar -> stockKbar));
        for (ThsCirculateInfo circulateInfo:circulateInfos){
            ThsStockKbar thsStockKbar = stockKbarMap.get(circulateInfo.getStockCode());
            if(thsStockKbar==null){
                System.out.println(circulateInfo.getStockCode()+" === "+circulateInfo.getStockName()+" ===thsCirculateHave");
            }
        }
        for (ThsStockKbar stockKbar:stockKbars){
            ThsCirculateInfo circulateInfo = circulateInfoMap.get(stockKbar.getStockCode());
            if (circulateInfo==null){
                System.out.println(stockKbar.getStockCode()+" *** "+stockKbar.getStockName()+" *** stockKbarHave");
            }
        }
    }

    public List<OutCirculateExcelDTO> synOutCirculates() {
        File file = new File("D:/circulate/outCirculate.xlsx");
        if (!file.exists()) {
            throw new BusinessException("文件:" + "D:/circulate/outCirculate.xlsx" + "不存在");
        }
        try {
            List<OutCirculateExcelDTO> dataList = new Excel2JavaPojoUtil(file).excel2JavaPojo(OutCirculateExcelDTO.class);
            return dataList;
        } catch (Exception e) {
            throw new BusinessException("文件解析及同步异常", e);
        }
    }

    public List<TradeDatePool> getTradeDatePools(){
        TradeDatePoolQuery query = new TradeDatePoolQuery();
        query.addOrderBy("trade_date", Sort.SortType.ASC);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(query);
        return tradeDatePools;
    }
}
