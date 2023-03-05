package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsMemberTalkDAO;
import com.bazinga.replay.model.ThsMemberTalk;
import com.bazinga.replay.query.ThsMemberTalkQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsMemberTalk DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2023-03-02
  */
@Repository
public class ThsMemberTalkDAOImpl extends SqlSessionDaoSupport implements ThsMemberTalkDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsMemberTalkDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsMemberTalk record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsMemberTalk selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsMemberTalk record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsMemberTalk> selectByCondition(ThsMemberTalkQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsMemberTalkQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}