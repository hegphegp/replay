package com.bazinga.util;

import java.util.concurrent.*;

/**
 * @author yunshan
 * @date 2018/12/22
 */
public class ThreadPoolUtils {

    private static int keepAliveMin = 5;

    private static RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            try {
                executor.getQueue().put(r);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    };

    public static ExecutorService createSingle(int queueSize) {
        return create(1, 1, queueSize);
    }

    public static ExecutorService createSingle(int queueSize, String threadPrefix) {
        return create(1, 1, queueSize, threadPrefix);
    }

    public static ExecutorService create(int coreSize, int maxSize, int queueSize) {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveMin,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(queueSize),
                rejectedExecutionHandler);
    }

    public static ExecutorService create(int coreSize, int maxSize, int queueSize, String threadPrefix) {
        return new ThreadPoolExecutor(
                coreSize,
                maxSize,
                keepAliveMin,
                TimeUnit.MINUTES,
                new ArrayBlockingQueue<Runnable>(queueSize),
                new NamedThreadFactory(threadPrefix),
                rejectedExecutionHandler);
    }

    public static ScheduledExecutorService createScheduled(int coreSize, String threadPrefix) {
        return new ScheduledThreadPoolExecutor(4,
                new NamedThreadFactory(threadPrefix),
                rejectedExecutionHandler);
    }
}
