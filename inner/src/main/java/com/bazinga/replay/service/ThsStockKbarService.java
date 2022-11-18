package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.query.ThsStockKbarQuery;

import java.util.List;

/**
 * 〈ThsStockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-11-18
 */
public interface ThsStockKbarService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsStockKbar save(ThsStockKbar record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsStockKbar getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsStockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsStockKbar> listByCondition(ThsStockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsStockKbarQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    ThsStockKbar getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(ThsStockKbar record);
}