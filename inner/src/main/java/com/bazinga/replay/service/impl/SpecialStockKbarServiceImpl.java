package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.SpecialStockKbarDAO;
import com.bazinga.replay.model.SpecialStockKbar;
import com.bazinga.replay.query.SpecialStockKbarQuery;
import com.bazinga.replay.service.SpecialStockKbarService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈SpecialStockKbar Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-05
 */
@Service
public class SpecialStockKbarServiceImpl implements SpecialStockKbarService {

    @Autowired
    private SpecialStockKbarDAO specialStockKbarDAO;

    @Override
    public SpecialStockKbar save(SpecialStockKbar record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        specialStockKbarDAO.insert(record);
        return record;
    }

    @Override
    public SpecialStockKbar getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return specialStockKbarDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(SpecialStockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return specialStockKbarDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<SpecialStockKbar> listByCondition(SpecialStockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return specialStockKbarDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(SpecialStockKbarQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return specialStockKbarDAO.countByCondition(query);
    }

    @Override
    public SpecialStockKbar getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return specialStockKbarDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(SpecialStockKbar record) {
        Assert.notNull(record, "待更新记录不能为空");
        return specialStockKbarDAO.updateByUniqueKey(record);
    }
}