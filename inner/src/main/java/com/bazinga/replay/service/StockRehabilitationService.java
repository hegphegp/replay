package com.bazinga.replay.service;

import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.StockRehabilitationQuery;

import java.util.List;

/**
 * 〈StockRehabilitation Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-03
 */
public interface StockRehabilitationService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockRehabilitation save(StockRehabilitation record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockRehabilitation getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockRehabilitation record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockRehabilitation> listByCondition(StockRehabilitationQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockRehabilitationQuery query);
}