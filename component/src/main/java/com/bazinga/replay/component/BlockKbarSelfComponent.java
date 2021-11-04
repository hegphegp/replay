package com.bazinga.replay.component;

import com.bazinga.base.Sort;
import com.bazinga.replay.convert.KBarDTOConvert;
import com.bazinga.replay.dto.AdjFactorDTO;
import com.bazinga.replay.dto.BlockTotalInfoDTO;
import com.bazinga.replay.dto.KBarDTO;
import com.bazinga.replay.dto.PlankTypeDTO;
import com.bazinga.replay.model.*;
import com.bazinga.replay.query.StockKbarQuery;
import com.bazinga.replay.query.ThsBlockInfoQuery;
import com.bazinga.replay.query.ThsBlockStockDetailQuery;
import com.bazinga.replay.query.TradeDatePoolQuery;
import com.bazinga.replay.service.*;
import com.bazinga.util.DateTimeUtils;
import com.bazinga.util.DateUtil;
import com.bazinga.util.MarketUtil;
import com.bazinga.util.PriceUtil;
import com.google.common.collect.Lists;
import com.tradex.enums.KCate;
import com.tradex.model.suport.DataTable;
import com.tradex.util.TdxHqUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * @author yunshan
 * @date 2019/1/25
 */
@Component
@Slf4j
public class BlockKbarSelfComponent {

    @Autowired
    private CirculateInfoComponent circulateInfoComponent;
    @Autowired
    private CommonComponent commonComponent;
    @Autowired
    private ThsBlockInfoService thsBlockInfoService;
    @Autowired
    private ThsBlockStockDetailService thsBlockStockDetailService;
    @Autowired
    private StockKbarService stockKbarService;
    @Autowired
    private TradeDatePoolService tradeDatePoolService;


    public void initBlockKbarSelf(Date date){
        boolean isTradeDate = commonComponent.isTradeDate(date);
        if(!isTradeDate){
            return;
        }
        date = DateTimeUtils.getDate000000(date);
        List<ThsBlockInfo> thsBlockInfos = thsBlockInfoService.listByCondition(new ThsBlockInfoQuery());
        for (ThsBlockInfo thsBlockInfo:thsBlockInfos){
            String blockCode = thsBlockInfo.getBlockCode();
            String blockName = thsBlockInfo.getBlockName();
            try {
                ThsBlockStockDetailQuery query = new ThsBlockStockDetailQuery();
                query.setBlockCode(blockCode);
                List<ThsBlockStockDetail> details = thsBlockStockDetailService.listByCondition(query);
                if(CollectionUtils.isEmpty(details)){
                    continue;
                }
                calKbarInfo(details,100);

            }catch (Exception e){
                log.info("复盘数据 异常 stockCode:{} stockName:{} e：{}", blockCode, blockName,e);
            }

        }
    }

    public void calKbarInfo(List<ThsBlockStockDetail> details,int limit){
        TradeDatePoolQuery tradeDatePoolQuery = new TradeDatePoolQuery();
        tradeDatePoolQuery.setTradeDateTo(DateTimeUtils.getDate235959(new Date()));
        tradeDatePoolQuery.addOrderBy("trade_date", Sort.SortType.DESC);
        tradeDatePoolQuery.setLimit(limit);
        List<TradeDatePool> tradeDatePools = tradeDatePoolService.listByCondition(tradeDatePoolQuery);
        List<TradeDatePool> tradeDates = Lists.reverse(tradeDatePools);
        String startDateyyyymmdd = DateUtil.format(tradeDates.get(0).getTradeDate(),DateUtil.yyyyMMdd);
        String endDateyyyymmdd = DateUtil.format(tradeDates.get(0).getTradeDate(),DateUtil.yyyyMMdd);
        Map<String, BlockTotalInfoDTO> map = new HashMap<>();
        for (ThsBlockStockDetail detail:details){
            StockKbarQuery stockKbarQuery = new StockKbarQuery();
            stockKbarQuery.setStockCode(detail.getStockCode());
            stockKbarQuery.setKbarDateFrom(startDateyyyymmdd);
            stockKbarQuery.addOrderBy("kbar_date", Sort.SortType.ASC);
            List<StockKbar> stockKbars = stockKbarService.listByCondition(stockKbarQuery);


        }
    }



}
