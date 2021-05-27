package com.bazinga.replay.service;

import com.bazinga.replay.model.BlockStockDetail;
import com.bazinga.replay.query.BlockStockDetailQuery;

import java.util.List;

/**
 * 〈BlockStockDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
public interface BlockStockDetailService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    BlockStockDetail save(BlockStockDetail record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    BlockStockDetail getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(BlockStockDetail record);


    int deleteByBlockCode(String blockCode);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BlockStockDetail> listByCondition(BlockStockDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(BlockStockDetailQuery query);
}