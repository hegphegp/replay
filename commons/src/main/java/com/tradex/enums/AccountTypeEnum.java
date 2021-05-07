package com.tradex.enums;

public enum AccountTypeEnum {
    HQ_L1(1,"L1连接"),
    HQ_L2(2,"L2连接"),
    HQ_TRADE(3,"交易连接");

    private Integer code;

    private String desc;

    AccountTypeEnum(Integer code, String desc) {
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
