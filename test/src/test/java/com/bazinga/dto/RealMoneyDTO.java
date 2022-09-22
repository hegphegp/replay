package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class RealMoneyDTO {
    private String tradeDate;
    private int stockCount=0;
    private BigDecimal totalAmount = BigDecimal.ZERO;


}
