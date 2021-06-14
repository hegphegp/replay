package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈PlankExchangeDaily〉<p>
 *
 * @author
 * @date 2021-06-14
 */
@lombok.Data
@lombok.ToString
public class PlankExchangeDaily implements Serializable {

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
     * 交易日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDate;

    /**
     * 1 首板
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer plankType;

    /**
     * 最大成交额日期
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String maxExchangeMoneyDate;

    /**
     * 最大成交额
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal exchangeMoney;

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