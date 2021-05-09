package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockKbarDAO;
import com.bazinga.replay.model.StockKbar;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.service.StockKbarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
@Service
public class StockKbarServiceImpl implements StockKbarService {

    @Autowired
    private StockKbarDAO stockKbarDAO;

    @Override
    public StockKbar save(StockKbar record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockKbarDAO.insert(record);
        return record;
    }

    @Override
    public StockKbar getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockKbarDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockKbarDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockKbar> listByCondition(StockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockKbarDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockKbarDAO.countByCondition(query);
    }

    @Override
    public StockKbar getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return stockKbarDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(StockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        return stockKbarDAO.updateByUniqueKey(record);
    }
}
