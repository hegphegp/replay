package com.bazinga.replay.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

@Data
public class PlankTypeDTO {
    boolean isPlank;
    boolean isEndPlank;
    int beforePlanks;
    int planks;
    int all;
    int space;
}
