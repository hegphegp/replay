package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.TradeDatePoolDAO;
import com.bazinga.replay.model.TradeDatePool;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.TradeDatePoolService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈TradeDatePool Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
@Service
public class TradeDatePoolServiceImpl implements TradeDatePoolService {

    @Autowired
    private TradeDatePoolDAO tradeDatePoolDAO;

    @Override
    public TradeDatePool save(TradeDatePool record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        tradeDatePoolDAO.insert(record);
        return record;
    }

    @Override
    public TradeDatePool getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return tradeDatePoolDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(TradeDatePool record) {
        Assert.notNull(record, "待更新记录不能为空");
        return tradeDatePoolDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<TradeDatePool> listByCondition(TradeDatePoolQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return tradeDatePoolDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(TradeDatePoolQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return tradeDatePoolDAO.countByCondition(query);
    }
}
