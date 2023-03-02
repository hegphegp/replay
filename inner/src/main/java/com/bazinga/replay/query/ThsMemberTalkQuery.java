package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsMemberTalk 查询参数〉<p>
 *
 * @author
 * @date 2023-03-02
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class ThsMemberTalkQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * url
     */
    private String sourceUrl;

    /**
     * 事件id
     */
    private String eventId;

    /**
     * 机构编码
     */
    private String orgTypeCode;

    /**
     * 鹰鸽指数
     */
    private BigDecimal hawkDoveVal;

    /**
     * 机构名称
     */
    private String orgName;

    /**
     * 角色
     */
    private String roleZh;

    /**
     * 人员
     */
    private String member;

    /**
     * 发布日期
     */
    private String publishDate;

    /**
     * 入库时间 开始
     */
    private Date ctimeFrom;

    /**
     * 入库时间 结束
     */
    private Date ctimeTo;

    /**
     * 标题
     */
    private String title;

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