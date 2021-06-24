package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.query.ThsBlockInfoQuery;

import java.util.List;

/**
 * 〈ThsBlockInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-23
 */
public interface ThsBlockInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsBlockInfo save(ThsBlockInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsBlockInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsBlockInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockInfo> listByCondition(ThsBlockInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsBlockInfoQuery query);
}