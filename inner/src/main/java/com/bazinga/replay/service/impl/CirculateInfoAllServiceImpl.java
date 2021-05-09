package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.CirculateInfoAllDAO;
import com.bazinga.replay.model.CirculateInfoAll;
import com.bazinga.replay.query.CirculateInfoAllQuery;
import com.bazinga.replay.service.CirculateInfoAllService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈CirculateInfoAll Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-05-09
 */
@Service
public class CirculateInfoAllServiceImpl implements CirculateInfoAllService {

    @Autowired
    private CirculateInfoAllDAO circulateInfoAllDAO;

    @Override
    public CirculateInfoAll save(CirculateInfoAll record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        circulateInfoAllDAO.insert(record);
        return record;
    }

    @Override
    public CirculateInfoAll getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return circulateInfoAllDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(CirculateInfoAll record) {
        Assert.notNull(record, "待更新记录不能为空");
        return circulateInfoAllDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<CirculateInfoAll> listByCondition(CirculateInfoAllQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return circulateInfoAllDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(CirculateInfoAllQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return circulateInfoAllDAO.countByCondition(query);
    }
}
