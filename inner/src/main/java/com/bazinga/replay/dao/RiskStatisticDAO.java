package com.bazinga.replay.dao;

import com.bazinga.replay.model.RiskStatistic;
import com.bazinga.replay.query.RiskStatisticQuery;

import java.util.List;

/**
 * 〈RiskStatistic DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-02-27
 */
public interface RiskStatisticDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(RiskStatistic record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    RiskStatistic selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(RiskStatistic record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<RiskStatistic> selectByCondition(RiskStatisticQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(RiskStatisticQuery query);

}