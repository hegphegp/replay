package com.tradex.util;


import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author yunshan
 * @date 2019/3/11
 */
public class RoundRobinUtil {

    private final static AtomicInteger next = new AtomicInteger(0);

    public static Long getRoundRobinIndex(int size){
        return next.getAndIncrement() % size + 1L;

    }

}
