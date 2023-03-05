package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsPriceSubjectDAO;
import com.bazinga.replay.model.ThsPriceSubject;
import com.bazinga.replay.query.ThsPriceSubjectQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsPriceSubject DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2023-03-02
  */
@Repository
public class ThsPriceSubjectDAOImpl extends SqlSessionDaoSupport implements ThsPriceSubjectDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsPriceSubjectDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsPriceSubject record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsPriceSubject selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsPriceSubject record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsPriceSubject> selectByCondition(ThsPriceSubjectQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsPriceSubjectQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}