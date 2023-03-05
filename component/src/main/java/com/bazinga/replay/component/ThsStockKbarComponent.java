package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.model.ThsStockKbar;
import com.bazinga.replay.query.ThsStockKbarQuery;
import com.bazinga.replay.service.StockKbarService;
import com.bazinga.replay.service.ThsStockKbarService;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.List;

@Component
@Slf4j
public class ThsStockKbarComponent {

    @Autowired
    private ThsStockKbarService thsStockKbarService;
    public List<ThsStockKbar> getDayStockKbars(String tradeDate){
        ThsStockKbarQuery thsStockKbarQuery = new ThsStockKbarQuery();
        thsStockKbarQuery.setKbarDate(tradeDate);
        List<ThsStockKbar> thsStockKbars = thsStockKbarService.listByCondition(thsStockKbarQuery);
        return thsStockKbars;
    }

    public List<ThsStockKbar> getAllStockKBars(String stockCode){
        try {
            ThsStockKbarQuery query = new ThsStockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(query);
            List<ThsStockKbar> result = Lists.newArrayList();
            for (ThsStockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            return result;
        }catch (Exception e){
            return null;
        }
    }

    public List<ThsStockKbar> getStockKBarsDeleteNewDays(String stockCode){
        try {
            ThsStockKbarQuery query = new ThsStockKbarQuery();
            query.setStockCode(stockCode);
            query.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<ThsStockKbar> stockKbars = thsStockKbarService.listByCondition(query);
            List<ThsStockKbar> result = Lists.newArrayList();
            for (ThsStockKbar stockKbar:stockKbars){
                if(stockKbar.getTradeQuantity()>0){
                    result.add(stockKbar);
                }
            }
            List<ThsStockKbar> best = deleteNewStockTimes(stockKbars, 2000);
            return best;
        }catch (Exception e){
            return null;
        }
    }

    //包括新股最后一个一字板
    public List<ThsStockKbar> deleteNewStockTimes(List<ThsStockKbar> list, int size){
        List<ThsStockKbar> datas = Lists.newArrayList();
        if(CollectionUtils.isEmpty(list)){
            return datas;
        }
        ThsStockKbar first = null;
        if(list.size()<size){
            BigDecimal preEndPrice = null;
            int i = 0;
            for (ThsStockKbar dto:list){
                if(preEndPrice!=null&&i==0){
                    if(!(dto.getHighPrice().equals(dto.getLowPrice()))){
                        i++;
                        datas.add(first);
                    }
                }
                if(i!=0){
                    datas.add(dto);
                }
                preEndPrice = dto.getClosePrice();
                first = dto;
            }
        }else{
            return list;
        }
        return datas;
    }


}
