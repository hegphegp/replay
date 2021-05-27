package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BlockStockDetailDAO;
import com.bazinga.replay.model.BlockStockDetail;
import com.bazinga.replay.query.BlockStockDetailQuery;
import com.bazinga.replay.service.BlockStockDetailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BlockStockDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
@Service
public class BlockStockDetailServiceImpl implements BlockStockDetailService {

    @Autowired
    private BlockStockDetailDAO blockStockDetailDAO;

    @Override
    public BlockStockDetail save(BlockStockDetail record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        blockStockDetailDAO.insert(record);
        return record;
    }

    @Override
    public BlockStockDetail getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return blockStockDetailDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BlockStockDetail record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return blockStockDetailDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public int deleteByBlockCode(String blockCode) {
        Assert.hasText(blockCode,"板块代码不能为空");

        return blockStockDetailDAO.deleteByBlockCode(blockCode);
    }


    @Override
    public List<BlockStockDetail> listByCondition(BlockStockDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return blockStockDetailDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BlockStockDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return blockStockDetailDAO.countByCondition(query);
    }
}