package com.bazinga.replay.dto;

import lombok.Data;

@Data
public class OpenPictureDTO {

    private String stockCode;

    private String tradeTime;

    private String tradeDate;

    private Long bidQty;

    private Long askQty;
}
