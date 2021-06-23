package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;

import java.util.List;

/**
 * 〈ThsBlockStockDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-23
 */
public interface ThsBlockStockDetailService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsBlockStockDetail save(ThsBlockStockDetail record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsBlockStockDetail getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsBlockStockDetail record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockStockDetail> listByCondition(ThsBlockStockDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsBlockStockDetailQuery query);
}