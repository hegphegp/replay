package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.query.StockPlankDailyQuery;

import java.util.List;

/**
 * 〈StockPlankDaily DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
public interface StockPlankDailyDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockPlankDaily record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockPlankDaily selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockPlankDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockPlankDaily> selectByCondition(StockPlankDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockPlankDailyQuery query);

}
