package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockFactorThreeTestDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal marketValue;
    private String factorDate;
    private BigDecimal factorValue;
    private BigDecimal preFactorValue;
    private int planks;
    private BigDecimal openRate;
    private BigDecimal endRate;
    private BigDecimal beforeRateDay3;
    private BigDecimal beforeRateDay5;
    private BigDecimal beforeRateDay10;
    private BigDecimal profit;

}
