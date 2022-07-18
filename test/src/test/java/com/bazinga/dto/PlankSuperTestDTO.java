package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class PlankSuperTestDTO {
    private String tradeDate;
    private BigDecimal plankAmount = BigDecimal.ZERO;
    private BigDecimal noPlankAmount= BigDecimal.ZERO;
    private BigDecimal highPlankAmount= BigDecimal.ZERO;
    private BigDecimal highNoPlankAmount= BigDecimal.ZERO;
    private BigDecimal firstPlankAmount = BigDecimal.ZERO;
    private BigDecimal firstNoPlankAmount = BigDecimal.ZERO;

    private int plankCount;
    private int endPlankCount;
    private int firstPlankCount;
    private int firstEndPlankCount;
    private int highPlankCount;
    private int highEndPlankCount;

    private String highStockCode;
    private Integer plankTime;
    private Integer planks;
    private Integer endFlag;
    private BigDecimal profit;


}
