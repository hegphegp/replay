package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈PlankSealQuantity 查询参数〉<p>
 *
 * @author
 * @date 2021-06-06
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class PlankSealQuantityQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 股票名称
     */
    private String stockName;

    /**
     * 交易日期
     */
    private String kbarDate;

    /**
     * 委托编号
     */
    private String orderNo;

    /**
     * 369
     */
    private String pairKey;

    /**
     * 对应封单值
     */
    private String pairValue;

    /**
     * 1 逐笔 0 非逐笔
     */
    private Integer detailType;

    /**
     * 封住1 
     */
    private Integer sealType;

    /**
     * 唯一键
     */
    private String uniqueKey;

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