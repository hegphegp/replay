package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.StockRehabilitationDAO;
import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.StockRehabilitationQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈StockRehabilitation DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-06-03
  */
@Repository
public class StockRehabilitationDAOImpl extends SqlSessionDaoSupport implements StockRehabilitationDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.StockRehabilitationDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(StockRehabilitation record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public StockRehabilitation selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(StockRehabilitation record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<StockRehabilitation> selectByCondition(StockRehabilitationQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(StockRehabilitationQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}