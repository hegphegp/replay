package com.bazinga.replay.dao;

import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;

import java.util.List;

/**
 * 〈TradeDatePool DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
public interface TradeDatePoolDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(TradeDatePool record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    TradeDatePool selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(TradeDatePool record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<TradeDatePool> selectByCondition(TradeDatePoolQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(TradeDatePoolQuery query);

}
