package com.bazinga.replay.dao;

import com.bazinga.replay.model.NewStock;
import com.bazinga.replay.query.NewStockQuery;

import java.util.List;

/**
 * 〈NewStock DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-10
 */
public interface NewStockDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(NewStock record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    NewStock selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(NewStock record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<NewStock> selectByCondition(NewStockQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(NewStockQuery query);

}
