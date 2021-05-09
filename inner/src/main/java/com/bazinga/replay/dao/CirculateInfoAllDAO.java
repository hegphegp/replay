package com.bazinga.replay.dao;

import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;

import java.util.List;

/**
 * 〈CirculateInfoAll DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
public interface CirculateInfoAllDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(CirculateInfoAll record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    CirculateInfoAll selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(CirculateInfoAll record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<CirculateInfoAll> selectByCondition(CirculateInfoAllQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(CirculateInfoAllQuery query);

}
