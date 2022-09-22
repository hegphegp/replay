package com.bazinga.dto;


import jnr.ffi.annotations.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class TenDayExplorDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal marketValue;
    private BigDecimal endRate;
    private BigDecimal lowRate;
    private BigDecimal exchangeRate;
    private BigDecimal tenDayMaxRate;

    private int level;

    private BigDecimal amountRate;
    private BigDecimal highDay7;
    private Integer highDayInt;
    private BigDecimal lowDay7;
    private Integer lowDayInt;
    private BigDecimal endDay7;

    private BigDecimal beforeRate3;
    private BigDecimal beforeRate5;
    private BigDecimal beforeRate10;

    public static List<TenDayExplorDTO> endRateSort(List<TenDayExplorDTO> list){
        Collections.sort(list,new TenDayExplorDTO.EndRateComparator());
        return list;
    }

    static class EndRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            TenDayExplorDTO p1 = (TenDayExplorDTO)object1;
            TenDayExplorDTO p2 = (TenDayExplorDTO)object2;
            if(p2.getEndRate()==null){
                if(p1.getEndRate()==null){
                    return 0;
                }
                return 1;
            }
            if(p1.getEndRate()==null){
                return -1;
            }
            int i = p1.getEndRate().compareTo(p2.getEndRate());
            return i;
        }
    }

}
