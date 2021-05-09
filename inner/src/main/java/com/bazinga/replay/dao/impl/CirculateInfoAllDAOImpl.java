package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.CirculateInfoAllDAO;
import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈CirculateInfoAll DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-05-09
  */
@Repository
public class CirculateInfoAllDAOImpl extends SqlSessionDaoSupport implements CirculateInfoAllDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.CirculateInfoAllDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(CirculateInfoAll record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public CirculateInfoAll selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(CirculateInfoAll record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<CirculateInfoAll> selectByCondition(CirculateInfoAllQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(CirculateInfoAllQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}
