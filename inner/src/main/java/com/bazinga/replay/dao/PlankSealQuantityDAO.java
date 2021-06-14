package com.bazinga.replay.dao;

import com.bazinga.replay.model.PlankSealQuantity;
import com.bazinga.replay.query.PlankSealQuantityQuery;

import java.util.List;

/**
 * 〈PlankSealQuantity DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-06
 */
public interface PlankSealQuantityDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(PlankSealQuantity record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    PlankSealQuantity selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(PlankSealQuantity record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<PlankSealQuantity> selectByCondition(PlankSealQuantityQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(PlankSealQuantityQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    PlankSealQuantity selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(PlankSealQuantity record);

}