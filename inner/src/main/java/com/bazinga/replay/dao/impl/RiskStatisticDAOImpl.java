package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.RiskStatisticDAO;
import com.bazinga.replay.model.RiskStatistic;
import com.bazinga.replay.query.RiskStatisticQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈RiskStatistic DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2023-02-27
  */
@Repository
public class RiskStatisticDAOImpl extends SqlSessionDaoSupport implements RiskStatisticDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.RiskStatisticDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(RiskStatistic record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public RiskStatistic selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(RiskStatistic record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<RiskStatistic> selectByCondition(RiskStatisticQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(RiskStatisticQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}