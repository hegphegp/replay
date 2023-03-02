package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsPriceSubject;
import com.bazinga.replay.query.ThsPriceSubjectQuery;

import java.util.List;

/**
 * 〈ThsPriceSubject DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
public interface ThsPriceSubjectDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsPriceSubject record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsPriceSubject selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsPriceSubject record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsPriceSubject> selectByCondition(ThsPriceSubjectQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsPriceSubjectQuery query);

}