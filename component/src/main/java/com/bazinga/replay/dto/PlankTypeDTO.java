package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PlankTypeDTO {
    boolean thirdSecondTransactionIsPlank;
    boolean isPlank;
    boolean isEndPlank;
    boolean isBeautifulPlank;
    Date insertTime;
    int beforePlanks;
    int planks;
    int all;
    int space;
}
