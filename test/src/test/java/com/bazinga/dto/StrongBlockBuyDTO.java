package com.bazinga.dto;


import jnr.ffi.annotations.In;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrongBlockBuyDTO {
    private String blockCode;
    private String blockName;
    private String tradeDate;
    private int stockCount;
    private Integer marketCount1;
    private Integer blockCount1;
    private BigDecimal rate1;

    private Integer marketCount2;
    private Integer blockCount2;
    private BigDecimal rate2;

    private Integer marketCount3;
    private Integer blockCount3;
    private BigDecimal rate3;

}
