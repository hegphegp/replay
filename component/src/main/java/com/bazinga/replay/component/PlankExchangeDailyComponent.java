package com.bazinga.replay.component;

import com.alibaba.fastjson.JSONObject;
import com.bazinga.enums.PlankTypeEnum;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.model.CirculateInfo;
import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.StockPlankDailyQuery;
import com.bazinga.replay.service.PlankExchangeDailyService;
import com.bazinga.replay.service.StockPlankDailyService;
import com.bazinga.replay.service.StockRehabilitationService;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class PlankExchangeDailyComponent {
    @Autowired
    private StockPlankDailyService stockPlankDailyService;
    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private StockKbarComponent stockKbarComponent;
    @Autowired
    private PlankExchangeDailyService plankExchangeDailyService;


    public void plankExchangeDaily(Date date){
        boolean isTradeDate = commonComponent.isTradeDate(date);
        if(!isTradeDate){
            return;
        }
        date = DateTimeUtils.getDate000000(date);
        List<CirculateInfo> circulateInfos = circulateInfoComponent.getMainAndGrowth();
        for (CirculateInfo circulateInfo:circulateInfos){
            String stockCode = circulateInfo.getStockCode();
            String stockName = circulateInfo.getStockName();
            try {
                List<KBarDTO> stockKBars = getStockKBars(circulateInfo);
                if (CollectionUtils.isEmpty(stockKBars)||stockKBars.size()<=20) {
                    log.info("复盘数据 没有获取到k线数据 或者数据日期长度不够 stockCode:{} stockName:{}", stockCode, stockName);
                    continue;
                }
                boolean isTwoPlank = isTwoPlank(stockKBars, circulateInfo);
                if(isTwoPlank){
                    PlankExchangeDaily plankExchangeDaily = new PlankExchangeDaily();
                    plankExchangeDaily.setStockCode(circulateInfo.getStockCode());
                    plankExchangeDaily.setStockName(circulateInfo.getStockName());
                    plankExchangeDaily.setTradeDate(DateUtil.format(date,DateUtil.yyyy_MM_dd));
                    plankExchangeDaily.setPlankType(1);
                    plankExchangeDaily.setCreateTime(new Date());
                    highExchangeDate(stockKBars,plankExchangeDaily);
                    plankExchangeDailyService.save(plankExchangeDaily);

                }
            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", stockCode, stockName,e);
            }

        }
    }

    public void highExchangeDate(List<KBarDTO> kbars,PlankExchangeDaily plankExchangeDaily){
        Long exchangeMoney = null;
        String dateStr = null;
        for (KBarDTO kbar :kbars){
            if(exchangeMoney==null||exchangeMoney<kbar.getTotalExchangeMoney()){
                exchangeMoney = kbar.getTotalExchangeMoney();
                dateStr = kbar.getDateStr();
            }
        }
        BigDecimal maxExchangeMoney = new BigDecimal(exchangeMoney);
        plankExchangeDaily.setExchangeMoney(maxExchangeMoney);
        plankExchangeDaily.setMaxExchangeMoneyDate(dateStr);
        return;
    }

    //判断是不是二板
    public boolean isTwoPlank(List<KBarDTO> kbars, CirculateInfo circulateInfo){
        PlankTypeDTO plankTypeDTO = new PlankTypeDTO();
        plankTypeDTO.setPlank(false);
        plankTypeDTO.setEndPlank(false);
        if(kbars.size()<3){
            return false;
        }
        List<KBarDTO> reverse = Lists.reverse(kbars);
        List<KBarDTO> kBarDTOS = reverse.subList(0, 3);
        BigDecimal preEndPrice = null;
        BigDecimal preHighPrice = null;
        Date preDate = null;
        int i=0;
        for (KBarDTO kBarDTO:kBarDTOS){
            i++;
            if(preEndPrice!=null) {
                BigDecimal endPrice = kBarDTO.getEndPrice();
                boolean highPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preHighPrice,endPrice);
                boolean endPlank = PriceUtil.isUpperPrice(circulateInfo.getStockCode(), preEndPrice,endPrice);
                if(i==2){
                    if(!highPlank){
                        return false;
                    }
                }
                if(i==3){
                    if(endPlank){
                        return false;
                    }
                }
            }
            preEndPrice = kBarDTO.getEndPrice();
            preHighPrice = kBarDTO.getHighestPrice();
            preDate  = kBarDTO.getDate();
        }
        return true;
    }


    public List<KBarDTO> getStockKBars(CirculateInfo circulateInfo){
        try {
            DataTable dataTable = TdxHqUtil.getSecurityBars(KCate.DAY, circulateInfo.getStockCode(), 0, 200);
            List<KBarDTO> kbars = KBarDTOConvert.convertKBar(dataTable);
            List<KBarDTO> list = deleteNewStockTimes(kbars, 200, circulateInfo.getStockCode());
            return list;
        }catch (Exception e){
            return null;
        }
    }


    //包括新股最后一个一字板
    public List<KBarDTO> deleteNewStockTimes(List<KBarDTO> list,int size,String stockCode){
        List<KBarDTO> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (KBarDTO dto:list){
                if(preEndPrice!=null&&i==0){
                    boolean endPlank = PriceUtil.isUpperPrice(dto.getEndPrice(), preEndPrice);
                    if(MarketUtil.isChuangYe(stockCode)&&!dto.getDate().before(DateUtil.parseDate("2020-08-24",DateUtil.yyyy_MM_dd))){
                        endPlank = PriceUtil.isUpperPrice(stockCode,dto.getEndPrice(),preEndPrice);
                    }
                    if(!(dto.getHighestPrice().equals(dto.getLowestPrice())&&endPlank)){
                        i++;
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getEndPrice();
            }
        }else{
            return list;
        }
        return datas;
    }


    public BigDecimal getFactor(String stockCode,Date tradeDate){
        Date preTradeDate = commonComponent.preTradeDate(tradeDate);
        String fromDate = DateUtil.format(preTradeDate, DateUtil.yyyyMMdd);
        Map<String, AdjFactorDTO> adjFactorMap = stockKbarComponent.getAdjFactorMap(stockCode, fromDate);
        AdjFactorDTO adjFactorDTO = adjFactorMap.get(DateUtil.format(tradeDate, DateUtil.yyyyMMdd));
        AdjFactorDTO preAdjFactorDTO = adjFactorMap.get(DateUtil.format(preTradeDate, DateUtil.yyyyMMdd));
        if(adjFactorDTO==null||preAdjFactorDTO==null||adjFactorDTO.getAdjFactor()==null||preAdjFactorDTO.getAdjFactor()==null){
            return null;
        }
        if(adjFactorDTO.getAdjFactor().equals(preAdjFactorDTO.getAdjFactor())){
            return null;
        }
        BigDecimal factor = preAdjFactorDTO.getAdjFactor().divide(adjFactorDTO.getAdjFactor(), 4, BigDecimal.ROUND_HALF_UP);
        return factor;
    }


}
