package com.bazinga.dto;


import jnr.ffi.annotations.In;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HuShen300MacdBuyDTO {
    private String stockCode;
    private String stockName;
    private Integer redirect;
    private BigDecimal preMacdBuy;
    private BigDecimal macdBuy;
    private BigDecimal preMacdSell;
    private BigDecimal macdSell;
    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private String buyTime;
    private String sellTime;
    private BigDecimal profitValue;
    private BigDecimal profit;




}
