package com.bazinga.dto;

import com.bazinga.replay.dto.MarketMoneyDTO;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class GatherAmountLevelBuyDTO {
    private String stockCode;
    private String stockName;
    private String tradeDate;
    private BigDecimal gatherAmount = BigDecimal.ZERO;
    private Integer gatherLevel;
    private BigDecimal openRate;
    private BigDecimal buyAmount;
    private BigDecimal buyRate;
    private BigDecimal realBuyPrice;
    private Integer realBuyDataType = 0;
    private boolean buyIsPlank = false;
    private BigDecimal profitEnd;
    private BigDecimal morProfit;

    public static List<GatherAmountLevelBuyDTO> gatherAmountSort(List<GatherAmountLevelBuyDTO> list){
        Collections.sort(list,new GatherAmountComparator());
        return list;
    }

    static class GatherAmountComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            GatherAmountLevelBuyDTO p1 = (GatherAmountLevelBuyDTO)object1;
            GatherAmountLevelBuyDTO p2 = (GatherAmountLevelBuyDTO)object2;
            int result = p1.getGatherAmount().compareTo(p2.getGatherAmount());
            return -result;
        }
    }
}
