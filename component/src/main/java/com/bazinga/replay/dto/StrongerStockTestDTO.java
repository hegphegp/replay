package com.bazinga.replay.dto;


import com.google.common.collect.Lists;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StrongerStockTestDTO {
    private String stockCode;
    private String stockName;
    private BigDecimal marketValue;
    private String tradeDate;
    private BigDecimal buyRate;
    private List<BigDecimal> szEndRates = Lists.newArrayList();
    private List<BigDecimal> stockEndRates = Lists.newArrayList();
    private String sellDate;
    private int levels = 1;
    private BigDecimal profit;
}
