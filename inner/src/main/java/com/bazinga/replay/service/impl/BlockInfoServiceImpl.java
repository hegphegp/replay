package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BlockInfoDAO;
import com.bazinga.replay.model.BlockInfo;
import com.bazinga.replay.query.BlockInfoQuery;
import com.bazinga.replay.service.BlockInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BlockInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2019-04-25
 */
@Service
public class BlockInfoServiceImpl implements BlockInfoService {

    @Autowired
    private BlockInfoDAO blockInfoDAO;

    @Override
    public BlockInfo save(BlockInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        blockInfoDAO.insert(record);
        return record;
    }

    @Override
    public BlockInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return blockInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BlockInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return blockInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BlockInfo> listByCondition(BlockInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return blockInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BlockInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return blockInfoDAO.countByCondition(query);
    }
}