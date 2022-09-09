package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BigOrderLastPriceTimeDAO;
import com.bazinga.replay.model.BigOrderLastPriceTime;
import com.bazinga.replay.query.BigOrderLastPriceTimeQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈BigOrderLastPriceTime DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-09-09
  */
@Repository
public class BigOrderLastPriceTimeDAOImpl extends SqlSessionDaoSupport implements BigOrderLastPriceTimeDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BigOrderLastPriceTimeDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BigOrderLastPriceTime record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public BigOrderLastPriceTime selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BigOrderLastPriceTime record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BigOrderLastPriceTime> selectByCondition(BigOrderLastPriceTimeQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BigOrderLastPriceTimeQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}