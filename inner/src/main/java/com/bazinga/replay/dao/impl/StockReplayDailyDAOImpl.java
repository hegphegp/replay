package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockReplayDailyDAO;
import com.bazinga.replay.model.StockReplayDaily;
import com.bazinga.replay.query.StockReplayDailyQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockReplayDaily DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-06-24
  */
@Repository
public class StockReplayDailyDAOImpl extends SqlSessionDaoSupport implements StockReplayDailyDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockReplayDailyDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockReplayDaily record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockReplayDaily selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockReplayDaily record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockReplayDaily> selectByCondition(StockReplayDailyQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockReplayDailyQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}