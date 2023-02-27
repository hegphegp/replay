package com.bazinga.replay.service;

import com.bazinga.replay.model.RiskStatistic;
import com.bazinga.replay.query.RiskStatisticQuery;

import java.util.List;

/**
 * 〈RiskStatistic Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-02-27
 */
public interface RiskStatisticService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    RiskStatistic save(RiskStatistic record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    RiskStatistic getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(RiskStatistic record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<RiskStatistic> listByCondition(RiskStatisticQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(RiskStatisticQuery query);
}