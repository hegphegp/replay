package com.bazinga.dto;


import lombok.Data;

import java.math.BigDecimal;

@Data
public class StrongPlankDefineTestDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private int isTPlank=0;
    private String plankTime;
    private String openTime;
    private Integer second;
    private BigDecimal speedRate;
    private Integer heartTimes;
    private int endFlag = 0;
    private BigDecimal profit;

}
