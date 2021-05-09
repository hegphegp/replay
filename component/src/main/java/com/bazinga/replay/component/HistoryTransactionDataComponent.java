package com.bazinga.replay.component;


import com.alibaba.fastjson.JSONObject;
import com.bazinga.replay.convert.ThirdSecondTransactionDataDTOConvert;
import com.bazinga.replay.dto.ThirdSecondTransactionDataDTO;
import com.bazinga.util.DateUtil;
import com.google.common.collect.Lists;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class HistoryTransactionDataComponent {

    public List<ThirdSecondTransactionDataDTO> getCurrentTransactionData(String stockCode){
        DataTable dataTable = TdxHqUtil.getTransactionData(stockCode, 0, 1200);
        return ThirdSecondTransactionDataDTOConvert.convert(dataTable);
    }

    public List<ThirdSecondTransactionDataDTO> getData(String stockCode, Date date){
        List<ThirdSecondTransactionDataDTO> resultList = Lists.newArrayList();
        int dateAsInt = DateUtil.getDateAsInt(date);
        int loopTimes = 0;
        int count =600;
        while (loopTimes<30 &&(CollectionUtils.isEmpty(resultList) || !"09:25".equals(resultList.get(0).getTradeTime()))){
            DataTable historyTransactionData = TdxHqUtil.getHistoryTransactionData(stockCode, dateAsInt, loopTimes * count, count);
            if(historyTransactionData ==null ){
                continue;
            }
            List<ThirdSecondTransactionDataDTO> list = ThirdSecondTransactionDataDTOConvert.convert(historyTransactionData);
            resultList.addAll(0,list);
            loopTimes++;
        }
        return resultList;

    }

    public List<ThirdSecondTransactionDataDTO> getData(String stockCode, String kbarDate){
        List<ThirdSecondTransactionDataDTO> resultList = Lists.newArrayList();
        int dateAsInt = Integer.parseInt(kbarDate);
        int loopTimes = 0;
        int count =600;
        while (loopTimes<30 &&(CollectionUtils.isEmpty(resultList) || !"09:25".equals(resultList.get(0).getTradeTime()))){
            DataTable historyTransactionData = TdxHqUtil.getHistoryTransactionData(stockCode, dateAsInt, loopTimes * count, count);
            if(historyTransactionData ==null ){
                continue;
            }
            List<ThirdSecondTransactionDataDTO> list = ThirdSecondTransactionDataDTOConvert.convert(historyTransactionData);
            resultList.addAll(0,list);
            loopTimes++;
        }
        return resultList;

    }

    public List<ThirdSecondTransactionDataDTO> getPreOneHourData(List<ThirdSecondTransactionDataDTO> list){
        if(CollectionUtils.isEmpty(list)){
            return list;
        }
        int index = list.size();
        for(int i=0; i<list.size(); i++){
            if("10:30".equals(list.get(i).getTradeTime())){
                index = i;
                break;
            }
        }
        return list.subList(0,index);
    }

    public List<ThirdSecondTransactionDataDTO> getPreHalfOneHourData(List<ThirdSecondTransactionDataDTO> list){
        if(CollectionUtils.isEmpty(list)){
            return list;
        }
        int index = 1;
        for(int i=0; i<list.size(); i++){
            if("10:00".equals(list.get(i).getTradeTime())){
                index = i;
                break;
            }
        }
        return list.subList(0,index);
    }

    public List<ThirdSecondTransactionDataDTO> getFixTimeData(List<ThirdSecondTransactionDataDTO> list,String fixTime){
        if(CollectionUtils.isEmpty(list)){
            return list;
        }
        int index = 1;
        fixTime = fixTime.replace(":","");
        for(int i=0; i<list.size(); i++){
            String minTradeTime = list.get(i).getTradeTime().replace(":", "");
            if(Integer.parseInt(minTradeTime)>=Integer.parseInt(fixTime)){
                index = i;
                break;
            }
        }
        return list.subList(0,index);
    }

    public Integer getUpperOpenCount(BigDecimal upperPrice, List<ThirdSecondTransactionDataDTO> list){
        int openCount = 0;

        boolean isCanAdd = false;

        for (ThirdSecondTransactionDataDTO transactionDataDTO : list) {
            if(transactionDataDTO.getTradeType() == 1 && upperPrice.compareTo(transactionDataDTO.getTradePrice())==0){
                isCanAdd = true;
            }else {
                if(isCanAdd){
                    openCount ++;
                    isCanAdd = false;
                }
            }
        }
        return openCount ;
    }



    public BigDecimal isOverOpenPrice(List<ThirdSecondTransactionDataDTO> list){
        if(list==null || list.size()<11){
            log.info("数据不合法 data = {}", JSONObject.toJSONString(list));
            return BigDecimal.ZERO;
        }
        for(int i =1; i<=10; i++){
            if(list.get(i).getTradePrice().compareTo(list.get(0).getTradePrice())>0){
                return list.get(i).getTradePrice();
            }
        }
        return BigDecimal.ZERO;
    }

    public BigDecimal isHalfHourOverOpenPrice(List<ThirdSecondTransactionDataDTO> list){
        for(int i =1;; i++){
            if(list.get(i).getTradeTime().startsWith("10")){
                if(list.get(i).getTradePrice().compareTo(list.get(0).getTradePrice())>0){
                    return list.get(i).getTradePrice();
                }else {
                    return BigDecimal.ZERO;
                }
            }
        }
    }

    public Float calAveragePrice(List<ThirdSecondTransactionDataDTO> list){
        float totalPrice = 0;
        Integer totalQuantity = 0;
        for (ThirdSecondTransactionDataDTO item : list) {
            totalPrice += item.getTradeQuantity() * item.getTradePrice().floatValue();
            totalQuantity += item.getTradeQuantity();
        }
        return (float) (Math.round(totalPrice / totalQuantity * 100)) / 100;
    }

    public BigDecimal calAvgPrice(String stockCode, Date tradeDate) {
        String tradeDateStr = DateUtil.format(tradeDate, DateUtil.yyyy_MM_dd);
        try{
            List<ThirdSecondTransactionDataDTO> datas = getData(stockCode, tradeDate);
            if(CollectionUtils.isEmpty(datas)){
                return null;
            }
            BigDecimal avgPrice = new BigDecimal(calAveragePrice(datas)).setScale(2,BigDecimal.ROUND_HALF_UP);
            return avgPrice;
        } catch (Exception e) {
            log.info("计算均价异常 stockCode:{} tradeDate:{}",stockCode,tradeDateStr);
            return null;
        }
    }
}
