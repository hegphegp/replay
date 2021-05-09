package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockPlankDailyDAO;
import com.bazinga.replay.model.StockPlankDaily;
import com.bazinga.replay.query.StockPlankDailyQuery;
import com.bazinga.replay.service.StockPlankDailyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockPlankDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
@Service
public class StockPlankDailyServiceImpl implements StockPlankDailyService {

    @Autowired
    private StockPlankDailyDAO stockPlankDailyDAO;

    @Override
    public StockPlankDaily save(StockPlankDaily record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockPlankDailyDAO.insert(record);
        return record;
    }

    @Override
    public StockPlankDaily getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockPlankDailyDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockPlankDaily record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockPlankDailyDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockPlankDaily> listByCondition(StockPlankDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockPlankDailyDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockPlankDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockPlankDailyDAO.countByCondition(query);
    }
}
