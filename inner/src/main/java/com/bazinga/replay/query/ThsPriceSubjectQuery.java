package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsPriceSubject 查询参数〉<p>
 *
 * @author
 * @date 2023-03-02
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class ThsPriceSubjectQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 题材id
     */
    private String nodeId;

    /**
     * 题材名称
     */
    private String nodeName;

    /**
     * 板块个股总数
     */
    private String industry;

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