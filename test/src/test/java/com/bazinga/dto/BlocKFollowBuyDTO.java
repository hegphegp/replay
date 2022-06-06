package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class BlocKFollowBuyDTO {
    private String blockCode;
    private String blockName;
    private int before10Planks;
    private int before10OnePlanks;
    private int beautifulPlanks;
    private BigDecimal biasDay6;
    private BigDecimal biasDay12;
    private BigDecimal biasDay24;
    private BigDecimal macd;

    private BigDecimal profit10First;
    private BigDecimal count10First;
    private BigDecimal profit10Two;
    private BigDecimal count10Two;
    private BigDecimal profit10Three;
    private BigDecimal count10Three;


    private BigDecimal profit3PlankFirst;
    private BigDecimal count3PlankFirst;
    private BigDecimal profit3PlankTwo;
    private BigDecimal count3PlankTwo;
    private BigDecimal profit3PlankThree;
    private BigDecimal count3PlankThree;

    private BigDecimal profit3FristPlankFirst;
    private BigDecimal count3FirstPlankFirst;
    private BigDecimal profit3FirstPlankTwo;
    private BigDecimal count3FirstPlankTwo;
    private BigDecimal profit3FirstPlankThree;
    private BigDecimal count3FirstPlankThree;
}
