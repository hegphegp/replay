package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsPriceTrendDAO;
import com.bazinga.replay.model.ThsPriceTrend;
import com.bazinga.replay.query.ThsPriceTrendQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * °¥ThsPriceTrend DAO°µ<p>
  * °¥π¶ƒ‹œÍœ∏√Ë ˆ°µ
  *
  * @author
  * @date 2023-03-02
  */
@Repository
public class ThsPriceTrendDAOImpl extends SqlSessionDaoSupport implements ThsPriceTrendDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsPriceTrendDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsPriceTrend record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsPriceTrend selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsPriceTrend record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsPriceTrend> selectByCondition(ThsPriceTrendQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsPriceTrendQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}