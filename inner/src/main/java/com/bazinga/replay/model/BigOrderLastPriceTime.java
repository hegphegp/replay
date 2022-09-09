package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈BigOrderLastPriceTime〉<p>
 *
 * @author
 * @date 2022-09-09
 */
@lombok.Data
@lombok.ToString
public class BigOrderLastPriceTime implements Serializable {

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
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   YES
     */
    private String stockCode;

    /**
     * 
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 交易时间
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   YES
     */
    private String tradeDate;

    /**
     * 唯一索引
     *
     * @最大长度   200
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   uk_unique_key
     */
    private String uniqueKey;

    /**
     * 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal limitUpPrice;

    /**
     * 
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String firstSellTradeSellNo;

    /**
     * 
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String firstSellTradeBuyNo;

    /**
     * 
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String sellTimeTrade;

    /**
     * 交易时间
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String timeTrade;

    /**
     * 买单编号
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeBuyNo;

    /**
     * 最后一笔价格
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal tradePrice;

    /**
     * 成交数量(单位手)
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Long tradeVolume;

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