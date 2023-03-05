package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockMABuyTwoDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal marketValue;
    private String sellDate;
    private Integer positionDays;
    private String statisticDate;

    //用于版本1
    private BigDecimal rateBuyDay;
    private BigDecimal openRateNextDay;


    private BigDecimal endRateStatisticDay;
    private BigDecimal openRateDay;
    private BigDecimal rate3;
    private BigDecimal rate5;
    private BigDecimal rate10;
    private BigDecimal rate15;
    private BigDecimal rate45;

    private String firstDate;
    private String endDate;
    private Integer highLowDays;

    private BigDecimal avgAmountHighLow;
    private BigDecimal avgAmountFirstDay15;

    private Integer buyStatisticDays;
    private Integer marketStocks;

    private BigDecimal profit;
}
