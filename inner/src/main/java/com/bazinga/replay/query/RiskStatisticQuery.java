package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈RiskStatistic 查询参数〉<p>
 *
 * @author
 * @date 2023-02-27
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class RiskStatisticQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 交易日期
     */
    private String tradeDate;

    /**
     * 实际结果0 空  1多
     */
    private Integer realRiskType;

    /**
     * 计算结果0 空  1多
     */
    private Integer calRiskType;

    /**
     * 沪深300bias6数值
     */
    private BigDecimal bias6Hs300;

    /**
     * 沪深300bias12数值
     */
    private BigDecimal bias12Hs300;

    /**
     * 沪深300bias24数值
     */
    private BigDecimal bias24Hs300;

    /**
     * 1 第一版  2 第二版
     */
    private Integer strategyType;

    /**
     * 创建时间 开始
     */
    private Date createTimeFrom;

    /**
     * 创建时间 结束
     */
    private Date createTimeTo;

    /**
     * 更新时间 开始
     */
    private Date updateTimeFrom;

    /**
     * 更新时间 结束
     */
    private Date updateTimeTo;


}