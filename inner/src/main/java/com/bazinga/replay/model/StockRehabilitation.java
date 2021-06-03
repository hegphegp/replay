package com.bazinga.replay.model;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈StockRehabilitation〉<p>
 *
 * @author
 * @date 2021-06-03
 */
@lombok.Data
@lombok.ToString
public class StockRehabilitation implements Serializable {

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
     * @最大长度   255
     * @允许为空   NO
     * @是否索引   NO
     */
    private String tradeDateStamp;

    /**
     * 1 1连板 2 2连板 3 3连板 4 4连板 5 5板及以上 10 其他版型  0 昨日没有上板 昨日版型
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer yesterdayPlankType;

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