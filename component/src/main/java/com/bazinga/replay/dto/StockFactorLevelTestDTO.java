package com.bazinga.replay.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StockFactorLevelTestDTO {
    private String stockCode;
    private String stockName;
    private BigDecimal marketValue;
    private String tradeDate;
    private Integer level;
    private Integer marketValueLevel;
    private BigDecimal openRate;
    private BigDecimal endRate;
    private String blockCode;
    private String blockName;
    private BigDecimal beforeRateDay3;
    private BigDecimal beforeRateDay5;
    private BigDecimal beforeRateDay10;
    private BigDecimal nextDayOpenRate;
    private BigDecimal nextDayLowRate;
    private BigDecimal nextDayHighRate;
    private BigDecimal amount;
    private BigDecimal preAmount;
    private BigDecimal preIndex2a;
    private BigDecimal index2a;
    private Integer blockLevel;
    private Integer blockCount;
    private int planks;
    private BigDecimal profitAvgPrice;
    private BigDecimal profitEndPrice;
}
