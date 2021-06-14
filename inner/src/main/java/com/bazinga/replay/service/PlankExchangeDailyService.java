package com.bazinga.replay.service;

import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.query.PlankExchangeDailyQuery;

import java.util.List;

/**
 * 〈PlankExchangeDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-14
 */
public interface PlankExchangeDailyService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    PlankExchangeDaily save(PlankExchangeDaily record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    PlankExchangeDaily getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(PlankExchangeDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<PlankExchangeDaily> listByCondition(PlankExchangeDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(PlankExchangeDailyQuery query);
}