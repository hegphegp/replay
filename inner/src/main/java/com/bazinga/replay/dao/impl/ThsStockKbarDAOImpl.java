package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsStockKbarDAO;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.query.ThsStockKbarQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsStockKbar DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-11-18
  */
@Repository
public class ThsStockKbarDAOImpl extends SqlSessionDaoSupport implements ThsStockKbarDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsStockKbarDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsStockKbar record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsStockKbar selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsStockKbar record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsStockKbar> selectByCondition(ThsStockKbarQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsStockKbarQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public ThsStockKbar selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(ThsStockKbar record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}