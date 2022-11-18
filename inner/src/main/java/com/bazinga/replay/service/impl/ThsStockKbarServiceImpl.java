package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsStockKbarDAO;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.query.ThsStockKbarQuery;
import com.bazinga.replay.service.ThsStockKbarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsStockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-11-18
 */
@Service
public class ThsStockKbarServiceImpl implements ThsStockKbarService {

    @Autowired
    private ThsStockKbarDAO thsStockKbarDAO;

    @Override
    public ThsStockKbar save(ThsStockKbar record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsStockKbarDAO.insert(record);
        return record;
    }

    @Override
    public ThsStockKbar getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsStockKbarDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsStockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsStockKbarDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsStockKbar> listByCondition(ThsStockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsStockKbarDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsStockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsStockKbarDAO.countByCondition(query);
    }

    @Override
    public ThsStockKbar getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return thsStockKbarDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(ThsStockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        return thsStockKbarDAO.updateByUniqueKey(record);
    }
}