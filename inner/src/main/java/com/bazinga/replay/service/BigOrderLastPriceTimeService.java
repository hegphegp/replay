package com.bazinga.replay.service;

import com.bazinga.replay.model.BigOrderLastPriceTime;
import com.bazinga.replay.query.BigOrderLastPriceTimeQuery;

import java.util.List;

/**
 * 〈BigOrderLastPriceTime Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-09-09
 */
public interface BigOrderLastPriceTimeService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    BigOrderLastPriceTime save(BigOrderLastPriceTime record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    BigOrderLastPriceTime getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(BigOrderLastPriceTime record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BigOrderLastPriceTime> listByCondition(BigOrderLastPriceTimeQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(BigOrderLastPriceTimeQuery query);
}