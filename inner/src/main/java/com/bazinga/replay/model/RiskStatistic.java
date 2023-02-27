package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈RiskStatistic〉<p>
 *
 * @author
 * @date 2023-02-27
 */
@lombok.Data
@lombok.ToString
public class RiskStatistic implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   PRIMARY
     */
    private Long id;

    /**
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
     */
    private String tradeDate;

    /**
     * 实际结果0 空  1多
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Integer realRiskType;

    /**
     * 计算结果0 空  1多
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Integer calRiskType;

    /**
     * 沪深300bias6数值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal bias6Hs300;

    /**
     * 沪深300bias12数值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal bias12Hs300;

    /**
     * 沪深300bias24数值
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal bias24Hs300;

    /**
     * 1 第一版  2 第二版
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer strategyType;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;

    /**
     * 更新时间
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date updateTime;


}