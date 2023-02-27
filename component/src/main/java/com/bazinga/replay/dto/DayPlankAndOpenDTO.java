package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DayPlankAndOpenDTO {

    private String tradeDate;

    private int plankStocks;

    private int openTimes;

}
