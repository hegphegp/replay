package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsPriceSubjectDAO;
import com.bazinga.replay.model.ThsPriceSubject;
import com.bazinga.replay.query.ThsPriceSubjectQuery;
import com.bazinga.replay.service.ThsPriceSubjectService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsPriceSubject Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
@Service
public class ThsPriceSubjectServiceImpl implements ThsPriceSubjectService {

    @Autowired
    private ThsPriceSubjectDAO thsPriceSubjectDAO;

    @Override
    public ThsPriceSubject save(ThsPriceSubject record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsPriceSubjectDAO.insert(record);
        return record;
    }

    @Override
    public ThsPriceSubject getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsPriceSubjectDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsPriceSubject record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsPriceSubjectDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsPriceSubject> listByCondition(ThsPriceSubjectQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsPriceSubjectDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsPriceSubjectQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsPriceSubjectDAO.countByCondition(query);
    }
}