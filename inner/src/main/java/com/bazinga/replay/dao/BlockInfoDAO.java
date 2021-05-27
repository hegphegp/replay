package com.bazinga.replay.dao;

import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.query.BlockInfoQuery;

import java.util.List;

/**
 * 〈BlockInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
public interface BlockInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(BlockInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    BlockInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(BlockInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BlockInfo> selectByCondition(BlockInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(BlockInfoQuery query);

}