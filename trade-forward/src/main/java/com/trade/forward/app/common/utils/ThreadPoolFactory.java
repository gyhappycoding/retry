package com.trade.forward.app.common.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>文件名称：DelayContext</p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/3/9 下午12:32.
 */
public class ThreadPoolFactory {

    private static final Logger logger = LoggerFactory.getLogger("common");

    /**
     * jvm钩子打印线程池执行状态
     */
    static {
        Runtime.getRuntime().addShutdownHook(
            new Thread(ThreadPoolFactory::printThreadPoolFactoryStatus)
        );
    }

    /**
     * 重试线程池
     */
    private static final Executor RETRY_EXECUTOR = new ThreadPoolExecutor(
        30,
        30,
        60000,
        TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>(2000),
        new BasicThreadFactory.Builder().namingPattern("retry-queue-pool-%d").build());


    public static Executor getRetryExecutor() {

        return RETRY_EXECUTOR;
    }


    /**
     * 打印线程池的状态
     */
    private static void printThreadPoolFactoryStatus() {

        printExecutorStatus("RETRY_EXECUTOR", (ThreadPoolExecutor) RETRY_EXECUTOR);

    }

    private static void printExecutorStatus(String executorName, ThreadPoolExecutor executor, boolean shutdownNow) {

        logger.info("{}#activeCount:{},poolSize:{},queueSize:{}", executorName, executor.getActiveCount(),
            executor.getPoolSize(), executor.getQueue().size());

        if (shutdownNow) {
            executor.shutdownNow();
        } else {
            ExecutorUtil.gracefulShutdown(executor, 5000);
        }
    }

    private static void printExecutorStatus(String executorName, ThreadPoolExecutor executor) {

        printExecutorStatus(executorName, executor, false);
    }

}

 