package com.bazinga.dto;


import com.bazinga.replay.model.StockIndex;
import jnr.ffi.annotations.In;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class HuShen300MacdBuyDTO {
    private String stockCode;
    private String stockName;
    private Integer redirect;
    private StockIndex buyStockIndex;
    private StockIndex preBuyStockIndex;
    private BigDecimal preMacdBuy;
    private BigDecimal macdBuy;
    private BigDecimal preMacdSell;
    private BigDecimal macdSell;
    private BigDecimal buyPrice;
    private StockIndex sellStockIndex;
    private BigDecimal sellPrice;
    private BigDecimal preBuyIndex;
    private String buyTime;
    private String preBuyTime;
    private String sellTime;
    private String diffSellTime;

    private String prePreTime;
    private BigDecimal preMacdHigh;
    private BigDecimal prePreMacdHigh;
    private BigDecimal prePreArea;
    private BigDecimal preArea;
    private BigDecimal area;
    private BigDecimal sellDiff;
    private BigDecimal preSellDiff;

    private BigDecimal diffProfit;
    private BigDecimal diffProfitValue;
    private BigDecimal profitValue;
    private BigDecimal profit;




}
