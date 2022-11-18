package com.bazinga.replay.dao;

import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.query.ThsCirculateInfoQuery;

import java.util.List;

/**
 * 〈ThsCirculateInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-11-18
 */
public interface ThsCirculateInfoDAO {

    /**
     * 新增一条记录
     *
     * @param record 保存对象
     */
    int insert(ThsCirculateInfo record);

    /**
     * 根据主键查询
     *
     * @param id 数据库主键
     */
    ThsCirculateInfo selectByPrimaryKey(Long id);

    /**
     * 根据主键更新数据
     *
     * @param record 更新参数
     */
    int updateByPrimaryKeySelective(ThsCirculateInfo record);

    /**
     * 根据查询参数查询数据
     *
     * @param query 查询参数
     */
    List<ThsCirculateInfo> selectByCondition(ThsCirculateInfoQuery query);

    /**
     * 根据查询参数查询数据总量
     *
     * @param query 查询参数
     */
    Integer countByCondition(ThsCirculateInfoQuery query);

    /**
     * 唯一键uniqueKey 查询
     *
     * @param uniqueKey 查询参数
     */
    ThsCirculateInfo selectByUniqueKey(String uniqueKey);

    /**
     * 唯一键uniqueKey 更新
     *
     * @param record 更新参数
     */
    int updateByUniqueKey(ThsCirculateInfo record);

}