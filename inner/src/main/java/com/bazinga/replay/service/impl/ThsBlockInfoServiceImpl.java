package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsBlockInfoDAO;
import com.bazinga.replay.model.ThsBlockInfo;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.service.ThsBlockInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsBlockInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-23
 */
@Service
public class ThsBlockInfoServiceImpl implements ThsBlockInfoService {

    @Autowired
    private ThsBlockInfoDAO thsBlockInfoDAO;

    @Override
    public ThsBlockInfo save(ThsBlockInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsBlockInfoDAO.insert(record);
        return record;
    }

    @Override
    public ThsBlockInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsBlockInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsBlockInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsBlockInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsBlockInfo> listByCondition(ThsBlockInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsBlockInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockInfoDAO.countByCondition(query);
    }
}