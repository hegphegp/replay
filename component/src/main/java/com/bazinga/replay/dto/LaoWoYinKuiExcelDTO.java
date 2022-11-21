package com.bazinga.replay.dto;

import com.bazinga.annotation.ExcelElement;
import lombok.Data;

import java.math.BigDecimal;


/**
 * @author yunshan
 * @date 2019/5/13
 */
@Data
public class LaoWoYinKuiExcelDTO {


    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("发生日期")
    private String tradeDate;

    /**
     * 股票代码
     *
     * @最大长度 10
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券代码")
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度 60
     * @允许为空 NO
     * @是否索引 NO
     */
    @ExcelElement("证券名称")
    private String stockName;




    @ExcelElement(value = "买金额")
    private BigDecimal buyAmount;

    @ExcelElement(value = "卖金额")
    private BigDecimal sellAmount;

    @ExcelElement(value = "正盈利")
    private BigDecimal profitAmount;

    @ExcelElement(value = "盈亏比")
    private BigDecimal profitRate;

    @ExcelElement("连板情况")
    private String plankType;

    @ExcelElement("委托时间")
    private String buyTime;

    @ExcelElement("成交时间")
    private String dealTime;

    @ExcelElement("成交时间差")
    private String buyDealTime;

    @ExcelElement("是否炸板")
    private String breakType;

    @ExcelElement("买入方式")
    private String buyType;

    @ExcelElement("所属模式")
    private String buyMode;

    @ExcelElement("所属行业")
    private String blockName;

    @ExcelElement("因子日因子值")
    private BigDecimal factorValue;

    @ExcelElement("因子日相同行业前200数量")
    private Integer sameCount;

    @ExcelElement("因子日相同行业前200因子值累计")
    private BigDecimal total;


    private String blockCode;
}
