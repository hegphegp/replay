package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.BigOrderLastPriceTimeDAO;
import com.bazinga.replay.model.BigOrderLastPriceTime;
import com.bazinga.replay.query.BigOrderLastPriceTimeQuery;
import com.bazinga.replay.service.BigOrderLastPriceTimeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈BigOrderLastPriceTime Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-09-09
 */
@Service
public class BigOrderLastPriceTimeServiceImpl implements BigOrderLastPriceTimeService {

    @Autowired
    private BigOrderLastPriceTimeDAO bigOrderLastPriceTimeDAO;

    @Override
    public BigOrderLastPriceTime save(BigOrderLastPriceTime record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        bigOrderLastPriceTimeDAO.insert(record);
        return record;
    }

    @Override
    public BigOrderLastPriceTime getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return bigOrderLastPriceTimeDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(BigOrderLastPriceTime record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return bigOrderLastPriceTimeDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<BigOrderLastPriceTime> listByCondition(BigOrderLastPriceTimeQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bigOrderLastPriceTimeDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(BigOrderLastPriceTimeQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return bigOrderLastPriceTimeDAO.countByCondition(query);
    }
}