package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class BlockTotalInfoDTO {

    private String dateStr;
    private BigDecimal openTotalRate;
    private BigDecimal closeTotalRate;
    //总额
    private BigDecimal totalExchange;
    //数量
    private int count;
}
