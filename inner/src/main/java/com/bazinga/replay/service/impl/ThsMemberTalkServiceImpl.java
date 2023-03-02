package com.bazinga.replay.service.impl;

import com.bazinga.replay.dao.ThsMemberTalkDAO;
import com.bazinga.replay.model.ThsMemberTalk;
import com.bazinga.replay.query.ThsMemberTalkQuery;
import com.bazinga.replay.service.ThsMemberTalkService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Date;
import java.util.List;

/**
 * 〈ThsMemberTalk Service〉<p>
 * 〈功能详细描述〉
 *
 * @author
 * @date 2023-03-02
 */
@Service
public class ThsMemberTalkServiceImpl implements ThsMemberTalkService {

    @Autowired
    private ThsMemberTalkDAO thsMemberTalkDAO;

    @Override
    public ThsMemberTalk save(ThsMemberTalk record) {
        Assert.notNull(record, "待插入记录不能为空");
        record.setCreateTime(new Date());
        thsMemberTalkDAO.insert(record);
        return record;
    }

    @Override
    public ThsMemberTalk getById(Long id) {
        Assert.notNull(id, "主键不能为空");
        return thsMemberTalkDAO.selectByPrimaryKey(id);
    }

    @Override
    public int updateById(ThsMemberTalk record) {
        Assert.notNull(record, "待更新记录不能为空");
        record.setUpdateTime(new Date());
        return thsMemberTalkDAO.updateByPrimaryKeySelective(record);
    }

    @Override
    public List<ThsMemberTalk> listByCondition(ThsMemberTalkQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsMemberTalkDAO.selectByCondition(query);
    }

    @Override
    public int countByCondition(ThsMemberTalkQuery query) {
        Assert.notNull(query, "查询条件不能为空");
        return thsMemberTalkDAO.countByCondition(query);
    }
}