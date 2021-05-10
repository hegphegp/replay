package com.bazinga.enums;

/**
 * @author yunshan
 * @date 2019/2/16
 */
public enum MarketTypeEnum {

    GENERAL(0,"普通市场"),
    STAR_MARKET(1,"科创板");


    private Integer code;

    private String desc;

    MarketTypeEnum(Integer code, String desc) {
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
