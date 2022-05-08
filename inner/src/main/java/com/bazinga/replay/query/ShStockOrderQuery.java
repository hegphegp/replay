package com.bazinga.replay.query;

import com.bazinga.base.ShardingQuery;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 〈ShStockOrder 查询参数〉<p>
 *
 * @author
 * @date 2022-04-26
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class ShStockOrderQuery extends ShardingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 
     */
    private String thscode;

    /**
     * 
     */
    private String market;

    /**
     * 
     */
    private String secType;

    /**
     *  开始
     */
    private Date dateTradeFrom;

    /**
     *  结束
     */
    private Date dateTradeTo;

    /**
     * 
     */
    private String timeTrade;

    /**
     * 
     */
    private BigDecimal orderSeq;

    /**
     * 
     */
    private BigDecimal orderAisle;

    /**
     * 
     */
    private String orderType;

    /**
     * 
     */
    private BigDecimal originalOrderSeq;

    /**
     * 
     */
    private BigDecimal orderPrice;

    /**
     * 
     */
    private BigDecimal remainOrderVolume;

    /**
     * 
     */
    private String orderDirection;

    /**
     * 
     */
    private BigDecimal businessSeq;

    /**
     * 
     */
    private BigDecimal seq;

    /**
     * 
     */
    private BigDecimal mseq;


}