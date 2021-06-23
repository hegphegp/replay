package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsBlockStockDetailDAO;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsBlockStockDetail DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-06-23
  */
@Repository
public class ThsBlockStockDetailDAOImpl extends SqlSessionDaoSupport implements ThsBlockStockDetailDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsBlockStockDetailDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsBlockStockDetail record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsBlockStockDetail selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsBlockStockDetail record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsBlockStockDetail> selectByCondition(ThsBlockStockDetailQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsBlockStockDetailQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}