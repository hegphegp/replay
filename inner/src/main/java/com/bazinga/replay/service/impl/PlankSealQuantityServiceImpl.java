package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.PlankSealQuantityDAO;
import com.bazinga.replay.model.PlankSealQuantity;
import com.bazinga.replay.query.PlankSealQuantityQuery;
import com.bazinga.replay.service.PlankSealQuantityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈PlankSealQuantity Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-06
 */
@Service
public class PlankSealQuantityServiceImpl implements PlankSealQuantityService {

    @Autowired
    private PlankSealQuantityDAO plankSealQuantityDAO;

    @Override
    public PlankSealQuantity save(PlankSealQuantity record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        plankSealQuantityDAO.insert(record);
        return record;
    }

    @Override
    public PlankSealQuantity getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return plankSealQuantityDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(PlankSealQuantity record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return plankSealQuantityDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<PlankSealQuantity> listByCondition(PlankSealQuantityQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return plankSealQuantityDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(PlankSealQuantityQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return plankSealQuantityDAO.countByCondition(query);
    }

    @Override
    public PlankSealQuantity getByUniqueKey(String uniqueKey) {
        Assert.notNull(uniqueKey, "唯一键不能为空");
        return plankSealQuantityDAO.selectByUniqueKey(uniqueKey);
    }

    @Override
    public int updateByUniqueKey(PlankSealQuantity record) {
        Assert.notNull(record, "待更新记录不能为空");
        return plankSealQuantityDAO.updateByUniqueKey(record);
    }
}