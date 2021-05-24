package com.bazinga.replay.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 〈BlockInfo〉<p>
 *
 * @author
 * @date 2019-04-25
 */
@lombok.Data
@lombok.ToString
public class BlockInfo implements Serializable {

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
     * 板块代码
     *
     * @最大长度   10
     * @允许为空   NO
     * @是否索引   NO
     */
    private String blockCode;

    /**
     * 板块名称
     *
     * @最大长度   60
     * @允许为空   YES
     * @是否索引   NO
     */
    private String blockName;

    /**
     * 板块个股总数
     *
     * @允许为空   NO
     * @是否索引   NO
     */
    private Integer totalCount;

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