package com.bazinga.replay.convert;

import com.bazinga.replay.dto.CirculateInfoDTO;
import com.google.common.collect.Lists;
import com.tradex.model.suport.DataTable;

import java.util.List;

public class CiculateInfoDTOConvert {

    public static List<CirculateInfoDTO> convert(DataTable dataTable){
        List<CirculateInfoDTO> list = Lists.newArrayList();
        if(dataTable==null||dataTable.rows()==0){
            return list;
        }
        int rows = dataTable.rows();
        for(int i=0;i<rows;i++){
            String[] row = dataTable.getRow(i);
            CirculateInfoDTO circulateInfoDTO = new CirculateInfoDTO();
            circulateInfoDTO.setStock(row[0]);
            circulateInfoDTO.setStockName(row[2]);
            list.add(circulateInfoDTO);
        }
        return list;

    }


}
