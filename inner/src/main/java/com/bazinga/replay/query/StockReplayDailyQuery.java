package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockReplayDaily 查询参数〉<p>
 *
 * @author
 * @date 2021-06-24
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockReplayDailyQuery extends PagingQuery implements Serializable {

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
     *  开始
     */
    private Date tradeDateFrom;

    /**
     *  结束
     */
    private Date tradeDateTo;

    /**
     * 板数
     */
    private Integer plankDays;

    /**
     * 断板天数
     */
    private Integer breakDays;

    /**
     * 连板类型 n天m板
     */
    private String plankType;

    /**
     * 0 尾盘炸板  1 尾盘封住
     */
    private Integer endStatus;

    /**
     * 0 不是一字  1 是一字
     */
    private Integer beautifulPlankStatus;

    private Integer openPlankStatus;

    /**
     * 平均溢价
     */
    private BigDecimal sellAvg;

    /**
     *  开始
     */
    private Date insertTimeFrom;

    /**
     *  结束
     */
    private Date insertTimeTo;

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