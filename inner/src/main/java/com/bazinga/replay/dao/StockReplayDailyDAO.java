package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockReplayDaily;
import com.bazinga.replay.query.StockReplayDailyQuery;

import java.util.List;

/**
 * 〈StockReplayDaily DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-24
 */
public interface StockReplayDailyDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockReplayDaily record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockReplayDaily selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockReplayDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockReplayDaily> selectByCondition(StockReplayDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockReplayDailyQuery query);

}