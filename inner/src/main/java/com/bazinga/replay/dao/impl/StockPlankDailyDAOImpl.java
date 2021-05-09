package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockPlankDailyDAO;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.query.StockPlankDailyQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockPlankDaily DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-05-09
  */
@Repository
public class StockPlankDailyDAOImpl extends SqlSessionDaoSupport implements StockPlankDailyDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockPlankDailyDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockPlankDaily record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockPlankDaily selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockPlankDaily record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockPlankDaily> selectByCondition(StockPlankDailyQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockPlankDailyQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}
