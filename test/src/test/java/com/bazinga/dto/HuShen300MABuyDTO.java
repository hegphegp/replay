package com.bazinga.dto;


import com.bazinga.replay.model.StockIndex;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HuShen300MABuyDTO {
    private String stockCode;
    private String stockName;
    private Integer redirect;
    private String tradeTime;
    private String sellTime;
    private BigDecimal profitValue;
    private BigDecimal profit;
}
