package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.StockRehabilitationDAO;
import com.bazinga.replay.model.StockRehabilitation;
import com.bazinga.replay.query.StockRehabilitationQuery;
import com.bazinga.replay.service.StockRehabilitationService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈StockRehabilitation Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-03
 */
@Service
public class StockRehabilitationServiceImpl implements StockRehabilitationService {

    @Autowired
    private StockRehabilitationDAO stockRehabilitationDAO;

    @Override
    public StockRehabilitation save(StockRehabilitation record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        stockRehabilitationDAO.insert(record);
        return record;
    }

    @Override
    public StockRehabilitation getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return stockRehabilitationDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(StockRehabilitation record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return stockRehabilitationDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<StockRehabilitation> listByCondition(StockRehabilitationQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockRehabilitationDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(StockRehabilitationQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return stockRehabilitationDAO.countByCondition(query);
    }
}