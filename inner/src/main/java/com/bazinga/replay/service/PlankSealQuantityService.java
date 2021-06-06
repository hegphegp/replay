package com.bazinga.replay.service;

import com.bazinga.replay.model.PlankSealQuantity;
import com.bazinga.replay.query.PlankSealQuantityQuery;

import java.util.List;

/**
 * 〈PlankSealQuantity Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-06
 */
public interface PlankSealQuantityService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    PlankSealQuantity save(PlankSealQuantity record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    PlankSealQuantity getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(PlankSealQuantity record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<PlankSealQuantity> listByCondition(PlankSealQuantityQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(PlankSealQuantityQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    PlankSealQuantity getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(PlankSealQuantity record);
}