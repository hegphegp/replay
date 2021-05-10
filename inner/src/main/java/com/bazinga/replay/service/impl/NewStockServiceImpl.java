package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.NewStockDAO;
import com.bazinga.replay.model.NewStock;
import com.bazinga.replay.query.NewStockQuery;
import com.bazinga.replay.service.NewStockService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈NewStock Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-10
 */
@Service
public class NewStockServiceImpl implements NewStockService {

    @Autowired
    private NewStockDAO newStockDAO;

    @Override
    public NewStock save(NewStock record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        newStockDAO.insert(record);
        return record;
    }

    @Override
    public NewStock getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return newStockDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(NewStock record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return newStockDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<NewStock> listByCondition(NewStockQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return newStockDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(NewStockQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return newStockDAO.countByCondition(query);
    }
}
