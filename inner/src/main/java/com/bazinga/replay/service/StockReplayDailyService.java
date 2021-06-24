package com.bazinga.replay.service;

import com.bazinga.replay.model.StockReplayDaily;
import com.bazinga.replay.query.StockReplayDailyQuery;

import java.util.List;

/**
 * 〈StockReplayDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-24
 */
public interface StockReplayDailyService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockReplayDaily save(StockReplayDaily record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockReplayDaily getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockReplayDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockReplayDaily> listByCondition(StockReplayDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockReplayDailyQuery query);
}