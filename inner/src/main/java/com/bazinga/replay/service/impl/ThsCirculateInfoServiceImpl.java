package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsCirculateInfoDAO;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.service.ThsCirculateInfoService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsCirculateInfo Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2022-11-18
 */
@Service
public class ThsCirculateInfoServiceImpl implements ThsCirculateInfoService {

    @Autowired
    private ThsCirculateInfoDAO thsCirculateInfoDAO;

    @Override
    public ThsCirculateInfo save(ThsCirculateInfo record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsCirculateInfoDAO.insert(record);
        return record;
    }

    @Override
    public ThsCirculateInfo getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsCirculateInfoDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsCirculateInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        return thsCirculateInfoDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsCirculateInfo> listByCondition(ThsCirculateInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsCirculateInfoDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsCirculateInfoQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsCirculateInfoDAO.countByCondition(query);
    }

    @Override
    public ThsCirculateInfo getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return thsCirculateInfoDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(ThsCirculateInfo record) {
        Assert.notNull(record, "待更新记录不能为空");
        return thsCirculateInfoDAO.updateByUniqueKey(record);
    }
}