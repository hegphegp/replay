package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsPriceTrend;
import com.bazinga.replay.query.ThsPriceTrendQuery;

import java.util.List;

/**
 * 〈ThsPriceTrend DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
public interface ThsPriceTrendDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsPriceTrend record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsPriceTrend selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsPriceTrend record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsPriceTrend> selectByCondition(ThsPriceTrendQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsPriceTrendQuery query);

}