package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.NewStockDAO;
import com.bazinga.replay.model.NewStock;
import com.bazinga.replay.query.NewStockQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈NewStock DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-05-10
  */
@Repository
public class NewStockDAOImpl extends SqlSessionDaoSupport implements NewStockDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.NewStockDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(NewStock record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public NewStock selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(NewStock record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<NewStock> selectByCondition(NewStockQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(NewStockQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}
