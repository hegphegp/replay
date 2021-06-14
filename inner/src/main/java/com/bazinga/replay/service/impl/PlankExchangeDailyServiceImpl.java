package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.PlankExchangeDailyDAO;
import com.bazinga.replay.model.PlankExchangeDaily;
import com.bazinga.replay.query.PlankExchangeDailyQuery;
import com.bazinga.replay.service.PlankExchangeDailyService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈PlankExchangeDaily Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-14
 */
@Service
public class PlankExchangeDailyServiceImpl implements PlankExchangeDailyService {

    @Autowired
    private PlankExchangeDailyDAO plankExchangeDailyDAO;

    @Override
    public PlankExchangeDaily save(PlankExchangeDaily record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        plankExchangeDailyDAO.insert(record);
        return record;
    }

    @Override
    public PlankExchangeDaily getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return plankExchangeDailyDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(PlankExchangeDaily record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return plankExchangeDailyDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<PlankExchangeDaily> listByCondition(PlankExchangeDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return plankExchangeDailyDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(PlankExchangeDailyQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return plankExchangeDailyDAO.countByCondition(query);
    }
}