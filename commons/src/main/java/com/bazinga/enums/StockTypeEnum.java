package com.bazinga.enums;

/**
 * @author yunshan
 * @date 2019/5/13
 */
public enum StockTypeEnum {

    POINT_FIVE(0, "0.5亿以下"),
    POINT_FIVE_TO_ONE(1, "0.5到一亿"),
    ONE_TO_TWO(2, "1 到 2 亿"),
    TWO_TO_THREE(3, "2 到 3 亿"),
    THREE_TO_FIVE_POINT_FIVE(4, "3 到 5.5 亿"),
    FIVE_POINT_FIVE_TO_EIGHT(5, "5.5 到 8亿"),
    EIGHT_TO_ELEVEN(6, "8 到 11 亿"),
    OVER_ELEVEN(7, "11亿以上");


    private Integer code;

    private String desc;

    StockTypeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
