package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class HighMarketExplorDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal marketValue;
    private boolean isHighUpper;
    private BigDecimal endRate;
    private BigDecimal nextHighRate;
    private BigDecimal nextLowRate;
    private BigDecimal nextOpenRate;
    private BigDecimal nextEndRate;
    private BigDecimal beforeRateDay3;
    private BigDecimal beforeRateDay5;
    private BigDecimal beforeRateDay10;
    private BigDecimal highRateBeforeDay5;
    private BigDecimal highDay3;
    private BigDecimal lowDay3;
    private BigDecimal endDay3;
    private BigDecimal profit1;
    private BigDecimal profit2;
    private BigDecimal profit3;
    private BigDecimal profit4;
    private BigDecimal profit5;
    private BigDecimal profit6;
    private BigDecimal profit7;
    private BigDecimal profit8;
    private BigDecimal profit9;
    private BigDecimal profit10;
    private BigDecimal profit11;
    private int level;


    public static List<HighMarketExplorDTO> endRateSort(List<HighMarketExplorDTO> list){
        Collections.sort(list,new HighMarketExplorDTO.EndRateComparator());
        return list;
    }

    static class EndRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            HighMarketExplorDTO p1 = (HighMarketExplorDTO)object1;
            HighMarketExplorDTO p2 = (HighMarketExplorDTO)object2;
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
