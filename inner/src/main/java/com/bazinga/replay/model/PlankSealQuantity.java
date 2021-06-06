package com.bazinga.replay.model;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈PlankSealQuantity〉<p>
 *
 * @author
 * @date 2021-06-06
 */
@lombok.Data
@lombok.ToString
public class PlankSealQuantity implements Serializable {

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
     * @最大长度   15
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockCode;

    /**
     * 股票名称
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String stockName;

    /**
     * 交易日期
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String kbarDate;

    /**
     * 委托编号
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String orderNo;

    /**
     * 369
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String pairKey;

    /**
     * 对应封单值
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String pairValue;

    /**
     * 1 逐笔 0 非逐笔
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer detailType;

    /**
     * 封住1 
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer sealType;

    /**
     * 唯一键
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   YES
     * @唯一索引   uk_unique_key
     */
    private String uniqueKey;

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