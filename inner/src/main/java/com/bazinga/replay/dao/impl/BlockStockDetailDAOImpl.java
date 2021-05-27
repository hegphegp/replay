package com.bazinga.replay.dao.impl;

import com.bazinga.replay.dao.BlockStockDetailDAO;
import com.bazinga.replay.model.BlockStockDetail;
import com.bazinga.replay.query.BlockStockDetailQuery;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.support.SqlSessionDaoSupport;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.List;

/**
 * 〈BlockStockDetail DAO〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
@Repository
public class BlockStockDetailDAOImpl extends SqlSessionDaoSupport implements BlockStockDetailDAO {

    private final String MAPPER_NAME = "com.bazinga.replay.dao.BlockStockDetailDAO";

    @Resource
    @Override
    public void setSqlSessionFactory(SqlSessionFactory sqlSessionFactory) {
        super.setSqlSessionFactory(sqlSessionFactory);
    }

    @Override
    public int insert(BlockStockDetail record) {
        return this.getSqlSession().insert(MAPPER_NAME + ".insert", record);
    }

    @Override
    public BlockStockDetail selectByPrimaryKey(Long id) {
        return this.getSqlSession().selectOne(MAPPER_NAME + ".selectByPrimaryKey", id);
    }

    @Override
    public int updateByPrimaryKeySelective(BlockStockDetail record) {
        return this.getSqlSession().update(MAPPER_NAME + ".updateByPrimaryKeySelective", record);
    }

    @Override
    public List<BlockStockDetail> selectByCondition(BlockStockDetailQuery query) {

        return this.getSqlSession().selectList(MAPPER_NAME + ".selectByCondition", query);
    }

    @Override
    public Integer countByCondition(BlockStockDetailQuery query) {

        return (Integer) this.getSqlSession().selectOne(MAPPER_NAME + ".countByCondition", query);
    }

    @Override
    public int deleteByBlockCode(String blockCode) {
        return this.getSqlSession().delete(MAPPER_NAME + ".deleteByBlockCode", blockCode);

    }
}