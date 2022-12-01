package com.bazinga.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class ReplayFenBanDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private boolean isEndPlank;
    private String planks;
    private BigDecimal closeRate;
    private BigDecimal beforeRate3;
    private BigDecimal beforeRate5;
    private BigDecimal beforeRate10;
    private BigDecimal nextOpenRate;
    private BigDecimal nextCloseRate;
    private BigDecimal nextHighRate;
    private BigDecimal nextLowRate;
}
