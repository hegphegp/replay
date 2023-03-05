package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈SpecialStockKbar〉<p>
 *
 * @author
 * @date 2023-03-05
 */
@lombok.Data
@lombok.ToString
public class SpecialStockKbar implements Serializable {

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
     * 股票代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   YES
     */
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 交易日期
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   YES
     */
    private String kbarDate;

    /**
     * 时间
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String kbarTime;

    /**
     * 唯一索引
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   uk_unique_key
     */
    private String uniqueKey;

    /**
     * 开盘价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal openPrice;

    /**
     * 收盘价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal closePrice;

    /**
     * 最高价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal highPrice;

    /**
     * 最低价
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal lowPrice;

    /**
     * 开盘价
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal adjOpenPrice;

    /**
     * 收盘价
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal adjClosePrice;

    /**
     * 最高价
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal adjHighPrice;

    /**
     * 最低价
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal adjLowPrice;

    /**
     * 复权因子
     *
     * @允许为空   YES
     * @是否索引   NO
     */
    private BigDecimal adjFactor;

    /**
     * 1 一分钟k线 10 十分钟k线
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer kbarType;

    /**
     * 成交数量(单位手)
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long tradeQuantity;

    /**
     * 成交额
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal tradeAmount;

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