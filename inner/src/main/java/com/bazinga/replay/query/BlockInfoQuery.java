package com.bazinga.replay.query;


import com.bazinga.base.PagingQuery;

import java.io.Serializable;
import java.util.Date;

/**
 * 〈BlockInfo 查询参数〉<p>
 *
 * @author
 * @date 2019-04-25
 */
@lombok.Data
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
public class BlockInfoQuery extends PagingQuery implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 板块代码
     */
    private String blockCode;

    /**
     * 板块名称
     */
    private String blockName;

    /**
     * 板块个股总数
     */
    private Integer totalCount;

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