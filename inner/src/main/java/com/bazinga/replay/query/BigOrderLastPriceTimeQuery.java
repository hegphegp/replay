package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈BigOrderLastPriceTime 查询参数〉<p>
 *
 * @author
 * @date 2022-09-09
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class BigOrderLastPriceTimeQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 股票代码
     */
    private String stockCode;

    /**
     * 
     */
    private String stockName;

    /**
     * 交易时间
     */
    private String tradeDate;

    /**
     * 唯一索引
     */
    private String uniqueKey;

    /**
     * 
     */
    private BigDecimal limitUpPrice;

    /**
     * 
     */
    private String firstSellTradeSellNo;

    /**
     * 
     */
    private String firstSellTradeBuyNo;

    /**
     * 
     */
    private String sellTimeTrade;

    /**
     * 交易时间
     */
    private String timeTrade;

    /**
     * 买单编号
     */
    private String tradeBuyNo;

    /**
     * 最后一笔价格
     */
    private BigDecimal tradePrice;

    /**
     * 成交数量(单位手)
     */
    private Long tradeVolume;

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