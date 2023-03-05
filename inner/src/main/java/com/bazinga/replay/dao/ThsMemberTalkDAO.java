package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsMemberTalk;
import com.bazinga.replay.query.ThsMemberTalkQuery;

import java.util.List;

/**
 * 〈ThsMemberTalk DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
public interface ThsMemberTalkDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsMemberTalk record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsMemberTalk selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsMemberTalk record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsMemberTalk> selectByCondition(ThsMemberTalkQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsMemberTalkQuery query);

}