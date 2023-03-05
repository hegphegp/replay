package com.bazinga.dto;


import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HuShen300MABuyTwoDTO {
    private String stockCode;
    private String stockName;
    private Integer redirect;
    private String tradeTime;
    private String sellTime;

    private BigDecimal leftChange;
    private BigDecimal rightChange;

    private BigDecimal startIndex;
    private BigDecimal centerIndex;
    private BigDecimal endIndex;

    private StockAverageLine centerLine;
    private StockKbar centerKbar;
    private StockAverageLine preCenterLine;
    private StockKbar preCenterKbar;

    private BigDecimal profitValue;
    private BigDecimal profit;
}
