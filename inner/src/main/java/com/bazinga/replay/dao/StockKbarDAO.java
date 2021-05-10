package com.bazinga.replay.dao;

import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;

import java.util.List;

/**
 * 〈StockKbar DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
public interface StockKbarDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(StockKbar record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    StockKbar selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(StockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<StockKbar> selectByCondition(StockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(StockKbarQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    StockKbar selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(StockKbar record);
    /**
     * 根据股票代码删除 k线数据
     * @param stockCode 股票代码
     */
    void deleteByStockCode(String stockCode);
}
