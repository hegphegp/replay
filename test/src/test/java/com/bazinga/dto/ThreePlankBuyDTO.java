package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThreePlankBuyDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private boolean endPlank;
    private BigDecimal openRate;
    private String buyTime;
    private Long buyTimeLong;
    private String blockNameLevel;
    private BigDecimal allDayProfit;
    private BigDecimal endProfit;

}
