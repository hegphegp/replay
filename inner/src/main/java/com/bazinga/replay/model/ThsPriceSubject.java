package com.bazinga.replay.model;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsPriceSubject〉<p>
 *
 * @author
 * @date 2023-03-02
 */
@lombok.Data
@lombok.ToString
public class ThsPriceSubject implements Serializable {

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
     * 板块个股总数
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String industry;

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