package com.bazinga.replay.dao;

import com.bazinga.replay.model.SpecialStockKbar;
import com.bazinga.replay.query.SpecialStockKbarQuery;

import java.util.List;

/**
 * 〈SpecialStockKbar DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-05
 */
public interface SpecialStockKbarDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(SpecialStockKbar record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    SpecialStockKbar selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(SpecialStockKbar record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<SpecialStockKbar> selectByCondition(SpecialStockKbarQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(SpecialStockKbarQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    SpecialStockKbar selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(SpecialStockKbar record);

}