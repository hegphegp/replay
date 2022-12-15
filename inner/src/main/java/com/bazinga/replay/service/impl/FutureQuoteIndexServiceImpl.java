package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.FutureQuoteIndexDAO;
import com.bazinga.replay.model.FutureQuoteIndex;
import com.bazinga.replay.query.FutureQuoteIndexQuery;
import com.bazinga.replay.service.FutureQuoteIndexService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈FutureQuoteIndex Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-12-15
 */
@Service
public class FutureQuoteIndexServiceImpl implements FutureQuoteIndexService {

    @Autowired
    private FutureQuoteIndexDAO futureQuoteIndexDAO;

    @Override
    public FutureQuoteIndex save(FutureQuoteIndex record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        futureQuoteIndexDAO.insert(record);
        return record;
    }

    @Override
    public FutureQuoteIndex getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return futureQuoteIndexDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(FutureQuoteIndex record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return futureQuoteIndexDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<FutureQuoteIndex> listByCondition(FutureQuoteIndexQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return futureQuoteIndexDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(FutureQuoteIndexQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return futureQuoteIndexDAO.countByCondition(query);
    }
}