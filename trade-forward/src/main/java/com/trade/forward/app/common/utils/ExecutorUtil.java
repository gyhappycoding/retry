package com.trade.forward.app.common.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * <p>文件名称：ExecutorUtil</p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/3/10 下午12:32.
 */
public class ExecutorUtil {

    public static boolean isShutdown(Executor executor) {

        return executor instanceof ExecutorService && ((ExecutorService) executor).isShutdown();
    }

    public static void gracefulShutdown(Executor executor, int timeout) {

        if (executor instanceof ExecutorService && !isShutdown(executor)) {
            ExecutorService es = (ExecutorService) executor;
            try {
                es.shutdown();
            } catch (Exception e) {
                return;
            }
            try {
                if (!es.awaitTermination(timeout, TimeUnit.MILLISECONDS)) {
                    es.shutdownNow();
                }
            } catch (InterruptedException e) {
                es.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

    }

}

 