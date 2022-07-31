package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class GatherPlankTestDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal tenDayRate;
    private int tenDayPlanks;
    private int continuePlanks;
    private BigDecimal profit;
    private BigDecimal endNoPlankSellProfit;

}
