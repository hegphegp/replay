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
    private BigDecimal beforeRateDay3;
    private BigDecimal beforeRateDay5;
    private BigDecimal beforeRateDay10;
    private BigDecimal highRateBeforeDay5;
    private BigDecimal highDay3;
    private BigDecimal lowDay3;
    private BigDecimal endDay3;

}
