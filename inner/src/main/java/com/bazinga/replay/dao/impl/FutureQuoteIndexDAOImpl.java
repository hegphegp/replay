package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.FutureQuoteIndexDAO;
import com.bazinga.replay.model.FutureQuoteIndex;
import com.bazinga.replay.query.FutureQuoteIndexQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈FutureQuoteIndex DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-12-15
  */
@Repository
public class FutureQuoteIndexDAOImpl extends SqlSessionDaoSupport implements FutureQuoteIndexDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.FutureQuoteIndexDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(FutureQuoteIndex record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public FutureQuoteIndex selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(FutureQuoteIndex record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<FutureQuoteIndex> selectByCondition(FutureQuoteIndexQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(FutureQuoteIndexQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}