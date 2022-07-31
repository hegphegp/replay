package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class TwoToThreeTestDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal openRate;
    private int tFlag=0;
    private String buyTime;
    private int endFlag=0;
    private BigDecimal gatherAmount;
    private BigDecimal preAmount;
    private Long circulateZ;
    private Long circulate;
    private BigDecimal plankPrice;
    private Integer tenDayPlanks;
    private BigDecimal profit;
    private BigDecimal endNoPlankSellProfit;

}
