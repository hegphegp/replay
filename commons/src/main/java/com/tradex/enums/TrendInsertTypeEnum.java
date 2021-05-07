package com.tradex.enums;

public enum TrendInsertTypeEnum {
    INSERT_SUCCESS(0,"买入成功"),
    INSERT_DEFEAT(1,"无法买入");

    private Integer code;

    private String desc;

    TrendInsertTypeEnum(Integer code, String desc) {
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
