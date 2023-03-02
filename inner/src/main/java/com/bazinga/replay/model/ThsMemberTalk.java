package com.bazinga.replay.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Date;
import java.util.Date;

import java.io.Serializable;

/**
 * 〈ThsMemberTalk〉<p>
 *
 * @author
 * @date 2023-03-02
 */
@lombok.Data
@lombok.ToString
public class ThsMemberTalk implements Serializable {

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
     * url
     *
     * @最大长度   100
     * @允许为空   NO
     * @是否索引   NO
     */
    private String sourceUrl;

    /**
     * 事件id
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String eventId;

    /**
     * 机构编码
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String orgTypeCode;

    /**
     * 鹰鸽指数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private BigDecimal hawkDoveVal;

    /**
     * 机构名称
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String orgName;

    /**
     * 角色
     *
     * @最大长度   60
     * @允许为空   NO
     * @是否索引   NO
     */
    private String roleZh;

    /**
     * 人员
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String member;

    /**
     * 发布日期
     *
     * @最大长度   20
     * @允许为空   NO
     * @是否索引   NO
     */
    private String publishDate;

    /**
     * 入库时间
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Date ctime;

    /**
     * 标题
     *
     * @最大长度   200
     * @允许为空   NO
     * @是否索引   NO
     */
    private String title;

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