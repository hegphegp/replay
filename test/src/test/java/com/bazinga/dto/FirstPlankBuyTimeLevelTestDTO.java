package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class FirstPlankBuyTimeLevelTestDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private String tradeTime;
    private Long tradeTimeLong;
    private Integer timeLevel;
    private boolean endFlag;
    private BigDecimal profit;


    public static List<FirstPlankBuyTimeLevelTestDTO> tradeTimeSort(List<FirstPlankBuyTimeLevelTestDTO> list){
        Collections.sort(list,new FirstPlankBuyTimeLevelTestDTO.TradeTimeComparator());
        return list;
    }

    static class TradeTimeComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            FirstPlankBuyTimeLevelTestDTO p1 = (FirstPlankBuyTimeLevelTestDTO)object1;
            FirstPlankBuyTimeLevelTestDTO p2 = (FirstPlankBuyTimeLevelTestDTO)object2;
            if(p2.getTradeTimeLong()==null){
                if(p1.getTradeTimeLong()==null){
                    return 0;
                }
                return 1;
            }
            if(p1.getTradeTimeLong()==null){
                return -1;
            }
            int i = p1.getTradeTimeLong().compareTo(p2.getTradeTimeLong());
            return i;
        }
    }

}
