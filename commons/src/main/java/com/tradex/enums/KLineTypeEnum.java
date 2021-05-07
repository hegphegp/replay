package com.tradex.enums;

public enum KLineTypeEnum {
    FIVE_MINUTE_LINE(0,"5分钟k线"),
    DAY_LINE(1,"日k线"),
    TEN(2,"10点"),
    TWO(3,"下午两点"),
    TWO_THIRTY(4,"下午两点半"),
    TWO_FORTY(5,"下午两点40"),
    TWO_FIFTY(6,"下午两点50");

    private Integer code;

    private String desc;

    KLineTypeEnum(Integer code, String desc) {
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
