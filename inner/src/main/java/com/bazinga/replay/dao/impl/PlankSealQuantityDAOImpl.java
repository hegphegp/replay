package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.PlankSealQuantityDAO;
import com.bazinga.replay.model.PlankSealQuantity;
import com.bazinga.replay.query.PlankSealQuantityQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈PlankSealQuantity DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-06-06
  */
@Repository
public class PlankSealQuantityDAOImpl extends SqlSessionDaoSupport implements PlankSealQuantityDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.PlankSealQuantityDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(PlankSealQuantity record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public PlankSealQuantity selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(PlankSealQuantity record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<PlankSealQuantity> selectByCondition(PlankSealQuantityQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(PlankSealQuantityQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public PlankSealQuantity selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(PlankSealQuantity record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}