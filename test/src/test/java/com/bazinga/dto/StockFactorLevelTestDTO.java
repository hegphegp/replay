package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class StockFactorLevelTestDTO {
    private String stockCode;
    private String stockName;
    private BigDecimal marketValue;
    private String tradeDate;
    private Integer level;
    private BigDecimal openRate;
    private BigDecimal endRate;
    private BigDecimal beforeRateDay3;
    private BigDecimal beforeRateDay5;
    private BigDecimal beforeRateDay10;
    private BigDecimal nextDayOpenRate;
    private BigDecimal nextDayLowRate;
    private BigDecimal nextDayHighRate;
    private BigDecimal index2a;
    private int planks;
}
