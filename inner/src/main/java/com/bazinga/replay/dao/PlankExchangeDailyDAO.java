package com.bazinga.replay.dao;

import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.query.PlankExchangeDailyQuery;

import java.util.List;

/**
 * 〈PlankExchangeDaily DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-14
 */
public interface PlankExchangeDailyDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(PlankExchangeDaily record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    PlankExchangeDaily selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(PlankExchangeDaily record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<PlankExchangeDaily> selectByCondition(PlankExchangeDailyQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(PlankExchangeDailyQuery query);

}