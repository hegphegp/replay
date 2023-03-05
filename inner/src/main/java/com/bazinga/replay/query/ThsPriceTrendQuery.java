package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsPriceTrend 查询参数〉<p>
 *
 * @author
 * @date 2023-03-02
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class ThsPriceTrendQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题材id
     */
    private String nodeId;

    /**
     * 题材名称
     */
    private String nodeName;

    /**
     * 行业
     */
    private String industry;

    /**
     * 国家
     */
    private String country;

    /**
     * 指标名称
     */
    private String indexName;

    /**
     * 指标单位
     */
    private String indexUnit;

    /**
     * 指标ID
     */
    private String indexId;

    /**
     * 来源
     */
    private String fromSource;

    /**
     * 日期
     */
    private String dateStr;

    /**
     * 具体值
     */
    private BigDecimal val;

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