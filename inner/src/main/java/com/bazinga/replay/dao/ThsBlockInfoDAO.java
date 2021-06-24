package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.query.ThsBlockInfoQuery;

import java.util.List;

/**
 * 〈ThsBlockInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-23
 */
public interface ThsBlockInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsBlockInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsBlockInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsBlockInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockInfo> selectByCondition(ThsBlockInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsBlockInfoQuery query);

}