package com.bazinga.replay.service;

import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;

import java.util.List;

/**
 * 〈StockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
public interface StockKbarService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    StockKbar save(StockKbar record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    StockKbar getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(StockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockKbar> listByCondition(StockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(StockKbarQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockKbar getByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockKbar record);

    void deleteByStockCode(String stockCode);
}
