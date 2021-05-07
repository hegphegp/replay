package com.tradex.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class CommonAccountDTOCache {


    public static Map<Long, ReentrantLock> QUOTE_TWO_LOCK_MAP = new ConcurrentHashMap<>(8);
    public static Map<Long, ReentrantLock> QUOTE_ONE_LOCK_MAP = new ConcurrentHashMap<>(8);
}
