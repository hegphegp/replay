package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.StockRehabilitationQuery;

import java.util.List;

/**
 * 〈StockRehabilitation DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-03
 */
public interface StockRehabilitationDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockRehabilitation record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockRehabilitation selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockRehabilitation record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockRehabilitation> selectByCondition(StockRehabilitationQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockRehabilitationQuery query);

}