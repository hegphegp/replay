package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.query.ThsStockKbarQuery;

import java.util.List;

/**
 * 〈ThsStockKbar DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-11-18
 */
public interface ThsStockKbarDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsStockKbar record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsStockKbar selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsStockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsStockKbar> selectByCondition(ThsStockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsStockKbarQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    ThsStockKbar selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(ThsStockKbar record);

}