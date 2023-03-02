package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsPriceTrend;
import com.bazinga.replay.query.ThsPriceTrendQuery;

import java.util.List;

/**
 * 〈ThsPriceTrend Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
public interface ThsPriceTrendService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsPriceTrend save(ThsPriceTrend record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsPriceTrend getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsPriceTrend record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsPriceTrend> listByCondition(ThsPriceTrendQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsPriceTrendQuery query);
}