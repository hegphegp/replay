package com.bazinga.dto;


import jnr.ffi.annotations.In;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Data
public class BlockEndRateDTO {
    private String blockCode;
    private BigDecimal endRate;
    private Integer level;
    public static List<BlockEndRateDTO> endRateSort(List<BlockEndRateDTO> list){
        Collections.sort(list,new BlockEndRateDTO.EndRateComparator());
        return list;
    }

    static class EndRateComparator implements Comparator<Object> {
        public int compare(Object object1,Object object2){
            BlockEndRateDTO p1 = (BlockEndRateDTO)object1;
            BlockEndRateDTO p2 = (BlockEndRateDTO)object2;
            if(p1.getEndRate()==null){
                if(p2.getEndRate()==null){
                    return 0;
                }
                return 1;
            }
            if(p2.getEndRate()==null){
                return -1;
            }
            int i = p2.getEndRate().compareTo(p1.getEndRate());
            return i;
        }
    }
}
