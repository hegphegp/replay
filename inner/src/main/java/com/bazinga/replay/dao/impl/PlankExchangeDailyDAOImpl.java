package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.PlankExchangeDailyDAO;
import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.query.PlankExchangeDailyQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈PlankExchangeDaily DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-06-14
  */
@Repository
public class PlankExchangeDailyDAOImpl extends SqlSessionDaoSupport implements PlankExchangeDailyDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.PlankExchangeDailyDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(PlankExchangeDaily record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public PlankExchangeDaily selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(PlankExchangeDaily record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<PlankExchangeDaily> selectByCondition(PlankExchangeDailyQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(PlankExchangeDailyQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}