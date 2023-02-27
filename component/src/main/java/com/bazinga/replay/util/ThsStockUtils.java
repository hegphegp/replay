package com.bazinga.replay.util;

import org.apache.commons.lang3.StringUtils;

/**
 * @author yunshan
 * @date 2019/1/9
 */
public class ThsStockUtils {


    /**
     * thsStockCode to common stockCode
     *
     * @return
     */
    public static String thsToCommonStockCode(String thsStockCode) {
        if(StringUtils.isBlank(thsStockCode)){
            return thsStockCode;
        }
        String replaceSZ = thsStockCode.replace(".SZ","");
        String stockCode = replaceSZ.replace(".SH","");
        return stockCode;
    }

    /**
     * common stockCode to thsStockCode
     *
     * @return
     */
    public static String commonStockCodeToThs(String stockCode) {
        if(StringUtils.isBlank(stockCode)){
            return stockCode;
        }
        String thsStockCode = null;
        if(stockCode.startsWith("6")){
            thsStockCode = stockCode+".SH";
        }else{
            thsStockCode = stockCode+".SZ";
        }
        return thsStockCode;
    }

    public static void main(String[] args) {
    }


}
