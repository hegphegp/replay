package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrongPercentDTO {
    private Integer stockSize;
    private Integer marketCount;
    private Integer blockCount;
    private BigDecimal rate;
    private SBEHighTimeDTO buyDTO;
}
