package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsPriceTrend〉<p>
 *
 * @author
 * @date 2023-03-02
 */
@lombok.Data
@lombok.ToString
public class ThsPriceTrend implements Serializable {

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
     * 题材id
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String nodeId;

    /**
     * 题材名称
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String nodeName;

    /**
     * 行业
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String industry;

    /**
     * 国家
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String country;

    /**
     * 指标名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String indexName;

    /**
     * 指标单位
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String indexUnit;

    /**
     * 指标ID
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String indexId;

    /**
     * 来源
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String fromSource;

    /**
     * 日期
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String dateStr;

    /**
     * 具体值
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal val;

    /**
     * 创建时间
     *
     * @允许为空   NO
     * @是否索引   YES
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