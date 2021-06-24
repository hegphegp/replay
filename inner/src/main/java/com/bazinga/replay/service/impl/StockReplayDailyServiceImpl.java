package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockReplayDailyDAO;
import com.bazinga.replay.model.StockReplayDaily;
import com.bazinga.replay.query.StockReplayDailyQuery;
import com.bazinga.replay.service.StockReplayDailyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockReplayDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-24
 */
@Service
public class StockReplayDailyServiceImpl implements StockReplayDailyService {

    @Autowired
    private StockReplayDailyDAO stockReplayDailyDAO;

    @Override
    public StockReplayDaily save(StockReplayDaily record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockReplayDailyDAO.insert(record);
        return record;
    }

    @Override
    public StockReplayDaily getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockReplayDailyDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockReplayDaily record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockReplayDailyDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockReplayDaily> listByCondition(StockReplayDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockReplayDailyDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockReplayDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockReplayDailyDAO.countByCondition(query);
    }
}