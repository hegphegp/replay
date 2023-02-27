package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThreePlankBuyDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private int planks;
    private boolean endPlank;
    private BigDecimal openRate;
    private BigDecimal hs300DevRate;
    private BigDecimal szDevRate;
    private String buyTime;
    private Long buyTimeLong;
    private String blockNameLevel;
    private BigDecimal raiseRate;
    private Integer beforePlanks;
    private BigDecimal allDayProfit;
    private BigDecimal endProfit;

}
