package com.bazinga.replay.service;

import com.bazinga.replay.model.ThsMemberTalk;
import com.bazinga.replay.query.ThsMemberTalkQuery;

import java.util.List;

/**
 * 〈ThsMemberTalk Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
public interface ThsMemberTalkService {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    ThsMemberTalk save(ThsMemberTalk record);

    /**
     * 根据ID查询
     *
     * @param id 数据库ID
     */
    ThsMemberTalk getById(Long id);

    /**
     * 根据id更新一条数据
     *
     * @param record 更新参数
     */
    int updateById(ThsMemberTalk record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsMemberTalk> listByCondition(ThsMemberTalkQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    int countByCondition(ThsMemberTalkQuery query);
}