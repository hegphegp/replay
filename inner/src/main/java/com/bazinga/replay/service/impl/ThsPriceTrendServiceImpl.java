package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsPriceTrendDAO;
import com.bazinga.replay.model.ThsPriceTrend;
import com.bazinga.replay.query.ThsPriceTrendQuery;
import com.bazinga.replay.service.ThsPriceTrendService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsPriceTrend Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
@Service
public class ThsPriceTrendServiceImpl implements ThsPriceTrendService {

    @Autowired
    private ThsPriceTrendDAO thsPriceTrendDAO;

    @Override
    public ThsPriceTrend save(ThsPriceTrend record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsPriceTrendDAO.insert(record);
        return record;
    }

    @Override
    public ThsPriceTrend getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsPriceTrendDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsPriceTrend record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsPriceTrendDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsPriceTrend> listByCondition(ThsPriceTrendQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsPriceTrendDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsPriceTrendQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsPriceTrendDAO.countByCondition(query);
    }
}