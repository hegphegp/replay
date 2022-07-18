package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class SBEHighTimeDTO {
    private String stockCode;
    private String stockName;
    private Long plankTime;
    private Integer planks;
    private BigDecimal profit;


}
