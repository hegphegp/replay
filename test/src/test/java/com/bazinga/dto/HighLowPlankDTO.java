package com.bazinga.dto;


import lombok.Data;

@Data
public class HighLowPlankDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private String preMarketHighPlanks;
    private int planks;
    private String upperTime;
    private String suddenTime;

}
