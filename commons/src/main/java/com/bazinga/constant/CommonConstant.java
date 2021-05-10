package com.bazinga.constant;


import java.math.BigDecimal;

/**
 * @author yunshan
 * @date 2019/1/29
 */
public class CommonConstant {

    public static final int SUCESS = 1;

    public static final int FAIRLURE = 0;

    public static final BigDecimal UPPER_RATE = new BigDecimal("1.10");
    public static final BigDecimal UPPER_RATE300 = new BigDecimal("1.20");
    public static final BigDecimal STAR_UPPER_RATE = new BigDecimal("1.20");
    public static final BigDecimal SUDDEN_RATE = new BigDecimal("0.90");
    public static final BigDecimal SUDDEN_RATE300 = new BigDecimal("0.80");

    public static final BigDecimal STAR_MARKET_UPPER_RATE = new BigDecimal("1.20");
    public static final BigDecimal STAR_MARKET_SUDDEN_RATE = new BigDecimal("0.80");



    public static final BigDecimal ONE_POINT_ZERO_FIVE = new BigDecimal("1.05");

    public static final BigDecimal ONE_POINT_ZERO_FOUR = new BigDecimal("1.04");

    public static final BigDecimal ONE_POINT_ZERO_TWO = new BigDecimal("1.02");

    public static final BigDecimal ONE_POINT_ZERO_THREE = new BigDecimal("1.03");

    public static final BigDecimal ONE_POINT_FIFTEEN = new BigDecimal("1.15");
    public static final BigDecimal ONE_POINT_FOURTEEN = new BigDecimal("1.14");

    public static final BigDecimal ONE_POINT_ZERO_SEVEN = new BigDecimal("1.07");

    public static final BigDecimal ONE_POINT_ZERO_EIGHT = new BigDecimal("1.08");

    public static final BigDecimal ONE_POINT_ZERO_NINE = new BigDecimal("1.09");

    public static final BigDecimal ONE_POINT_ONE_EIGHT = new BigDecimal("1.18");

    public static final BigDecimal POINT_NINE_FIVE = new BigDecimal("0.95");

    public static final BigDecimal POINT_NINE_EIGHT = new BigDecimal("0.98");

    public static final BigDecimal DECIMAL_HUNDRED = new BigDecimal("100");

    public static final BigDecimal POINT_FIVE = new BigDecimal("0.50");

    public static final int HUNDRED= 100;

    public static final int TEN_THOUSAND = 10000;

    public static final int BIG_BEFORE_QUANTITY= 20000000;

    public static final Long ONE_MILLION = 1000000L;

    public static final Long LESS_ONE_MILLION = 980000L;

    public static final Long ONE_BILLION = 100000000L;

    public static final Long MAX_ORDER_QUANTITY = 999500L;

    public static final Long THREE_MILLION = 3000000L;
    public static final Long NEED_BEFORE_CONTROL= 5000000L;

    /**
     * 最大成交量,单位: 手
     */
    public static final int MAX_TRADE_QUANTITY = 13000;

    public static final int FIRST_PLANK_TRADE_QUANTITY = 10000;

    /**
     * 热点板块数量
     */
    public static final int HOT_BLOCK_COUNT = 10;

    /**
     * 最大拆单数量
     */
    public static final int MAX_SPLIT_ORDER_NUM = 6;

    public static final String CYBZ_CODE = "399006";


}
