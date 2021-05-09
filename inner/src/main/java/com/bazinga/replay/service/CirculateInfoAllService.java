package com.bazinga.replay.service;

import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;

import java.util.List;

/**
 * 〈CirculateInfoAll Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
public interface CirculateInfoAllService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    CirculateInfoAll save(CirculateInfoAll record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    CirculateInfoAll getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(CirculateInfoAll record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<CirculateInfoAll> listByCondition(CirculateInfoAllQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(CirculateInfoAllQuery query);
}
