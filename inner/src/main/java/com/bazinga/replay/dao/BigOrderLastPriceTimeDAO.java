package com.bazinga.replay.dao;

import com.bazinga.replay.model.BigOrderLastPriceTime;
import com.bazinga.replay.query.BigOrderLastPriceTimeQuery;

import java.util.List;

/**
 * 〈BigOrderLastPriceTime DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-09-09
 */
public interface BigOrderLastPriceTimeDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(BigOrderLastPriceTime record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    BigOrderLastPriceTime selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(BigOrderLastPriceTime record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<BigOrderLastPriceTime> selectByCondition(BigOrderLastPriceTimeQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(BigOrderLastPriceTimeQuery query);

}