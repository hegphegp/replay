package com.bazinga.dto;


import com.bazinga.replay.model.StockAverageLine;
import com.bazinga.replay.model.StockKbar;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LineMaHighLowDTO {
    private String stockCode;
    private Integer highLowFlag;
    private String firstDate;
    private String centerDate;
    private String endDate;
    private BigDecimal avgPrice;

}
