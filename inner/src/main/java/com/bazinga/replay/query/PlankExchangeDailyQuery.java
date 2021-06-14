package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈PlankExchangeDaily 查询参数〉<p>
 *
 * @author
 * @date 2021-06-14
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class PlankExchangeDailyQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String stockCode;

    /**
     * 
     */
    private String stockName;

    /**
     * 交易日期
     */
    private String tradeDate;

    /**
     * 1 首板
     */
    private Integer plankType;

    /**
     * 最大成交额日期
     */
    private String maxExchangeMoneyDate;

    /**
     * 最大成交额
     */
    private BigDecimal exchangeMoney;

    /**
     *  开始
     */
    private Date createTimeFrom;

    /**
     *  结束
     */
    private Date createTimeTo;

    /**
     *  开始
     */
    private Date updateTimeFrom;

    /**
     *  结束
     */
    private Date updateTimeTo;


}