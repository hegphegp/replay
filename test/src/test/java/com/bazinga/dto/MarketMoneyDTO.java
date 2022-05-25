package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class MarketMoneyDTO {
    private String stockCode;

    private String stockName;

    private long circulate;

    private BigDecimal marketMoney;

}
