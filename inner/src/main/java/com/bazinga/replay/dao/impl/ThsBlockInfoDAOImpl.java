package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsBlockInfoDAO;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.query.ThsBlockInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsBlockInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2021-06-23
  */
@Repository
public class ThsBlockInfoDAOImpl extends SqlSessionDaoSupport implements ThsBlockInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsBlockInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsBlockInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsBlockInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsBlockInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsBlockInfo> selectByCondition(ThsBlockInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsBlockInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }
}