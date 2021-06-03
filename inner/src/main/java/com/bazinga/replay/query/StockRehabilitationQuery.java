package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockRehabilitation 查询参数〉<p>
 *
 * @author
 * @date 2021-06-03
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class StockRehabilitationQuery extends PagingQuery implements Serializable {

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
     * 
     */
    private String tradeDateStamp;

    /**
     * 1 1连板 2 2连板 3 3连板 4 4连板 5 5板及以上 10 其他版型  0 昨日没有上板 昨日版型
     */
    private Integer yesterdayPlankType;

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