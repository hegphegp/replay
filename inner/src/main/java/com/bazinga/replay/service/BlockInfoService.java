package com.bazinga.replay.service;

import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.query.BlockInfoQuery;

import java.util.List;

/**
 * 〈BlockInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
public interface BlockInfoService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    BlockInfo save(BlockInfo record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    BlockInfo getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(BlockInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BlockInfo> listByCondition(BlockInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(BlockInfoQuery query);
}