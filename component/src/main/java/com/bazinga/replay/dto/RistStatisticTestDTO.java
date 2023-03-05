package com.bazinga.replay.dto;


import com.google.common.collect.Lists;
import jnr.ffi.annotations.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class RistStatisticTestDTO {

    private String tradeDate;
    private int v1real;
    private int v2;
    private  int v3;
    private BigDecimal v1RealSuccessRate;
    private BigDecimal v2SuccessRate;
    private BigDecimal v3SuccessRate;
    private Integer result;
    private BigDecimal profit;
}
