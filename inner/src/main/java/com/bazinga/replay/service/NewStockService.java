package com.bazinga.replay.service;

import com.bazinga.replay.model.NewStock;
import com.bazinga.replay.query.NewStockQuery;

import java.util.List;

/**
 * 〈NewStock Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-10
 */
public interface NewStockService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    NewStock save(NewStock record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    NewStock getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(NewStock record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<NewStock> listByCondition(NewStockQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(NewStockQuery query);
}
