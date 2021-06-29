package com.bazinga.replay.model;

import java.util.Date;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockReplayDaily〉<p>
 *
 * @author
 * @date 2021-06-24
 */
@lombok.Data
@lombok.ToString
public class StockReplayDaily implements Serializable {

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
     * 
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockCode;

    /**
     * 
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date tradeDate;

    /**
     * 板数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer plankDays;

    /**
     * 断板天数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer breakDays;

    /**
     * 连板类型 n天m板
     *
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String plankType;

    /**
     * 0 尾盘炸板  1 尾盘封住
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer endStatus;

    /**
     * 0 不是一字  1 是一字
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer beautifulPlankStatus;

    private Integer openPlankStatus;

    /**
     * 平均溢价
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal sellAvg;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date insertTime;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date createTime;

    /**
     * 
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private Date updateTime;


}