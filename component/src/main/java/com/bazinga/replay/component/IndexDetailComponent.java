package com.bazinga.replay.component;

import com.bazinga.replay.model.IndexDetail;
import com.bazinga.replay.model.ThsCirculateInfo;
import com.bazinga.replay.query.IndexDetailQuery;
import com.bazinga.replay.query.ThsCirculateInfoQuery;
import com.bazinga.replay.service.IndexDetailService;
import com.bazinga.replay.service.ThsCirculateInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class IndexDetailComponent {
    @Autowired
    private IndexDetailService indexDetailService;

    /**
     *
     * @param tradeDate 2022-01-01
     * @param stockCode
     * @param indexCode
     * @return
     */
    public IndexDetail getIndexDetailUK(String tradeDate,String stockCode,String indexCode){
        IndexDetailQuery query = new IndexDetailQuery();
        query.setStockCode(stockCode);
        query.setIndexCode(indexCode);
        query.setKbarDate(tradeDate);
        List<IndexDetail> indexDetails = indexDetailService.listByCondition(query);
        if(CollectionUtils.isEmpty(indexDetails)){
            return null;
        }
        return indexDetails.get(0);
    }




}
