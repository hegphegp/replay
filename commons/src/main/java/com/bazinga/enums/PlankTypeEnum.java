package com.bazinga.enums;

/**
 * @author yunshan
 * @date 2019/2/16
 */
public enum PlankTypeEnum {

    FIRST_PLANK(1,"首板"),
    SECOND_PLANK(2,"2板"),
    THIRD_PLANK(3,"3板"),
    FOURTH_PLANK(4,"4板"),
    FIFTH_PLANK(5,"5板"),
    THREE_DAY_TWO_PLANK(10,"三天两板"),
    FOUR_DAY_THREE_PLANK(11,"反包连板"),

    SPECIAL_PLANK_TYPE(20,"特殊高位板");

    private Integer code;

    private String desc;

    PlankTypeEnum(Integer code, String desc) {
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

    public static PlankTypeEnum getByCode(Integer code){
        if(code==null){
            return null;
        }
        for (PlankTypeEnum plankTypeEnum:PlankTypeEnum.values()){
            if(plankTypeEnum.getCode()==code){
                return plankTypeEnum;
            }
        }
        return null;
    }

}
