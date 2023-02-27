package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.RiskStatisticDAO;
import com.bazinga.replay.model.RiskStatistic;
import com.bazinga.replay.query.RiskStatisticQuery;
import com.bazinga.replay.service.RiskStatisticService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈RiskStatistic Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-02-27
 */
@Service
public class RiskStatisticServiceImpl implements RiskStatisticService {

    @Autowired
    private RiskStatisticDAO riskStatisticDAO;

    @Override
    public RiskStatistic save(RiskStatistic record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        riskStatisticDAO.insert(record);
        return record;
    }

    @Override
    public RiskStatistic getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return riskStatisticDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(RiskStatistic record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return riskStatisticDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<RiskStatistic> listByCondition(RiskStatisticQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return riskStatisticDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(RiskStatisticQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return riskStatisticDAO.countByCondition(query);
    }
}