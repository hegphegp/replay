package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsPriceSubject;
import com.bazinga.replay.query.ThsPriceSubjectQuery;

import java.util.List;

/**
 * 〈ThsPriceSubject Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
public interface ThsPriceSubjectService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsPriceSubject save(ThsPriceSubject record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsPriceSubject getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsPriceSubject record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsPriceSubject> listByCondition(ThsPriceSubjectQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsPriceSubjectQuery query);
}