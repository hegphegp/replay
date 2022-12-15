package com.bazinga.replay.service;

import com.bazinga.replay.model.FutureQuoteIndex;
import com.bazinga.replay.query.FutureQuoteIndexQuery;

import java.util.List;

/**
 * 〈FutureQuoteIndex Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-12-15
 */
public interface FutureQuoteIndexService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    FutureQuoteIndex save(FutureQuoteIndex record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    FutureQuoteIndex getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(FutureQuoteIndex record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<FutureQuoteIndex> listByCondition(FutureQuoteIndexQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(FutureQuoteIndexQuery query);
}