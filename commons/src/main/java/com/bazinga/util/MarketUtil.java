package com.bazinga.util;

import com.bazinga.enums.MarketTypeEnum;
import org.apache.commons.lang.StringUtils;

/**
 * @author huliang
 * @version $Id: DateUtil.java, v 0.1 2011-12-19 下午7:23:39 huliang Exp $
 */
public class MarketUtil {

    public static Integer getMarketCode(String stockCode) {
       if(StringUtils.isEmpty(stockCode)){
           return null;
       }
       if(stockCode.startsWith("688")){
           return MarketTypeEnum.STAR_MARKET.getCode();
       }
       return MarketTypeEnum.GENERAL.getCode();
    }

    public static boolean isChuangYe(String stockCode) {
        if(StringUtils.isBlank(stockCode)){
            return false;
        }
        if(stockCode.startsWith("30")){
            return true;
        }
        return false;
    }

    public static boolean isKeChuang(String stockCode) {
        if(StringUtils.isEmpty(stockCode)){
            return false;
        }
        if(stockCode.startsWith("688")){
            return true;
        }
        return false;
    }

    public static boolean isMain(String stockCode) {
        if(StringUtils.isEmpty(stockCode)){
            return false;
        }
        if(isKeChuang(stockCode)||isChuangYe(stockCode)){
            return false;
        }
        return true;
    }

    public static boolean isAStocks(String stockCode) {
        if(StringUtils.isBlank(stockCode)){
            return false;
        }
        if(isShMain(stockCode)||isSzMain(stockCode)||isChuangYe(stockCode)){
            return true;
        }
        return false;
    }

    public static boolean isShMain(String stockCode) {
        if(StringUtils.isEmpty(stockCode)){
            return false;
        }
        if(stockCode.startsWith("60")){
            return true;
        }
        return false;
    }
    public static boolean isSzMain(String stockCode) {
        if(StringUtils.isEmpty(stockCode)){
            return false;
        }
        if(stockCode.startsWith("00")){
            return true;
        }
        return false;
    }

    public static boolean isSh(String stockCode) {
        if(StringUtils.isEmpty(stockCode)){
            return false;
        }
        if(stockCode.startsWith("6")){
            return true;
        }
        return false;
    }
    public static boolean isSz(String stockCode) {
        if(StringUtils.isEmpty(stockCode)){
            return false;
        }
        if(stockCode.startsWith("00")){
            return true;
        }
        if(stockCode.startsWith("30")){
            return true;
        }
        return false;
    }


    public static String thsToGeneralStock(String thsStock){
        if(StringUtils.isBlank(thsStock)){
            return null;
        }
        String replace = thsStock.replace(".SH", "");
        String general = replace.replace(".SZ", "");
        return general;
    }

    public static String generalToThsStock(String stock){
        if(StringUtils.isBlank(stock)){
            return null;
        }
        if(MarketUtil.isSh(stock)){
            return stock+".SH";
        }
        if(MarketUtil.isSh(stock)){
            return stock+".SZ";
        }
        return null;
    }
}
