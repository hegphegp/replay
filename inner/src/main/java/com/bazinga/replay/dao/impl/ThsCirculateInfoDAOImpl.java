package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.ThsCirculateInfoDAO;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.query.ThsCirculateInfoQuery;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

import org.springframework.util.Assert;

 /**
  * 〈ThsCirculateInfo DAO〉<p>
  * 〈功能详细描述〉
  *
  * @author
  * @date 2022-11-18
  */
@Repository
public class ThsCirculateInfoDAOImpl extends SqlSessionDaoSupport implements ThsCirculateInfoDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.ThsCirculateInfoDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(ThsCirculateInfo record) {
        return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
    }

    @Override
    public ThsCirculateInfo selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(ThsCirculateInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<ThsCirculateInfo> selectByCondition(ThsCirculateInfoQuery query) {

        return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(ThsCirculateInfoQuery query) {

        return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public ThsCirculateInfo selectByUniqueKey(String uniqueKey) {
        return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByUniqueKey", uniqueKey);
    }

    @Override
    public int updateByUniqueKey(ThsCirculateInfo record) {
        return this.getSqlSession().update( MAPPER_NAME + ".updateByUniqueKey", record);
    }
}