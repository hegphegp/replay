package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;

import java.util.List;

/**
 * 〈ThsBlockStockDetail DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-23
 */
public interface ThsBlockStockDetailDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsBlockStockDetail record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsBlockStockDetail selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsBlockStockDetail record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsBlockStockDetail> selectByCondition(ThsBlockStockDetailQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsBlockStockDetailQuery query);

}