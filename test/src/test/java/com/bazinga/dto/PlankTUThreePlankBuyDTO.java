package com.bazinga.dto;


import lombok.Data;

@Data
public class PlankTUThreePlankBuyDTO {
    private String stockCode;
    private String tradeDate;
    private Long firstPlankTime;
    private Long firstBuyTime;
    private String buyTime;
    private boolean continueBeautifulPlank;
    private int planks;

}
