package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AdjFactorDTO {

    private String stockCode;

    private String tradeDate;

    private BigDecimal adjFactor;

    public AdjFactorDTO(String stockCode, String tradeDate, BigDecimal adjFactor) {
        this.stockCode = stockCode;
        this.tradeDate = tradeDate;
        this.adjFactor = adjFactor;
    }
}
