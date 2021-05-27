package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BlockInfoDAO;
import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.query.BlockInfoQuery;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 〈BlockInfo DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
@Repository
public class BlockInfoDAOImpl extends SqlSessionDaoSupport implements BlockInfoDAO {

   private final String MAPPER_NAME = "com.bazinga.replay.dao.BlockInfoDAO";

   @Resource
   @Override
   public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
       super.setSqlSessionFactory(sqlSessionFactory);
   }

   @Override
   public int insert(BlockInfo record) {
       return this.getSqlSession().insert( MAPPER_NAME + ".insert", record);
   }

   @Override
   public BlockInfo selectByPrimaryKey(Long id) {
       return this.getSqlSession().selectOne( MAPPER_NAME + ".selectByPrimaryKey", id);
   }

   @Override
   public int updateByPrimaryKeySelective(BlockInfo record) {
       return this.getSqlSession().update( MAPPER_NAME + ".updateByPrimaryKeySelective", record);
   }

   @Override
   public List<BlockInfo> selectByCondition(BlockInfoQuery query) {

       return this.getSqlSession().selectList( MAPPER_NAME + ".selectByCondition", query);
   }

   @Override
   public Integer countByCondition(BlockInfoQuery query) {

       return (Integer)this.getSqlSession().selectOne( MAPPER_NAME + ".countByCondition", query);
   }
}