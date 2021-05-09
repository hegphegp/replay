package com.bazinga.replay.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class ThirdSecondTransactionDataDTO {

    private String tradeTime;

    private BigDecimal tradePrice;

    private Integer tradeQuantity;

    private Integer tradeType;

}
