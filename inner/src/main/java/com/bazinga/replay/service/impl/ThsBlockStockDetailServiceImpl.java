package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsBlockStockDetailDAO;
import com.bazinga.replay.model.ThsBlockStockDetail;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.service.ThsBlockStockDetailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsBlockStockDetail Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2021-06-23
 */
@Service
public class ThsBlockStockDetailServiceImpl implements ThsBlockStockDetailService {

    @Autowired
    private ThsBlockStockDetailDAO thsBlockStockDetailDAO;

    @Override
    public ThsBlockStockDetail save(ThsBlockStockDetail record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsBlockStockDetailDAO.insert(record);
        return record;
    }

    @Override
    public ThsBlockStockDetail getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsBlockStockDetailDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsBlockStockDetail record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsBlockStockDetailDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsBlockStockDetail> listByCondition(ThsBlockStockDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockStockDetailDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsBlockStockDetailQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsBlockStockDetailDAO.countByCondition(query);
    }
}