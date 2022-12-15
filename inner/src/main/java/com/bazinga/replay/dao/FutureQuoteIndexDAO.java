package com.bazinga.replay.dao;

import com.bazinga.replay.model.FutureQuoteIndex;
import com.bazinga.replay.query.FutureQuoteIndexQuery;

import java.util.List;

/**
 * 〈FutureQuoteIndex DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-12-15
 */
public interface FutureQuoteIndexDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(FutureQuoteIndex record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    FutureQuoteIndex selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(FutureQuoteIndex record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<FutureQuoteIndex> selectByCondition(FutureQuoteIndexQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(FutureQuoteIndexQuery query);

}