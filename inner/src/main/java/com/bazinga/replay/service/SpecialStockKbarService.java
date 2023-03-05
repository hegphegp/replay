package com.bazinga.replay.service;

import com.bazinga.replay.model.SpecialStockKbar;
import com.bazinga.replay.query.SpecialStockKbarQuery;

import java.util.List;

/**
 * 〈SpecialStockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-05
 */
public interface SpecialStockKbarService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    SpecialStockKbar save(SpecialStockKbar record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    SpecialStockKbar getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(SpecialStockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<SpecialStockKbar> listByCondition(SpecialStockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(SpecialStockKbarQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    SpecialStockKbar getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(SpecialStockKbar record);
}