package com.trade.forward.app.infra.cache.delay;

import com.alibaba.fastjson.JSON;
import com.trade.forward.app.common.utils.ExecutorUtil;
import com.trade.forward.app.common.utils.ThreadPoolFactory;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;


/**
 * <p>文件名称：DelayQueueConsumer</p>
 * <p>文件描述：</p>
 * <p>版权所有： </p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/8/20 下午12:13.
 */
@Slf4j
public class DelayQueueConsumer {

    @Setter
    private StringRedisTemplate redisTemplate;

    @Setter
    private String topic;

    private Semaphore semaphore = null;

    private volatile RedisDelayQueue delayQueue;

    @Setter
    private DelayCallback delayCallback;

    @Setter
    private int concurrency;

    private volatile boolean isRunning = false;

    /***
     * 延迟队列构造函数
     */
    public DelayQueueConsumer() {

    }

    /***
     * 延迟队列构造函数
     * @param topic
     * @param redisTemplate
     */
    public DelayQueueConsumer(String topic,
        StringRedisTemplate redisTemplate,
        DelayCallback delayCallback,
        int concurrency) {

        this.topic = topic;
        this.redisTemplate = redisTemplate;
        this.delayCallback = delayCallback;
        this.concurrency = concurrency;
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("topic is null!");
        }
    }

    public void init() {

        if (isRunning) {
            return;
        }

        this.semaphore = new Semaphore(concurrency);
        this.delayQueue = new RedisDelayQueue();
        this.delayQueue.init(topic, redisTemplate);
        this.isRunning = true;

        Thread t = new Thread(() -> {

            for (; ; ) {

                if (!isRunning) {
                    break;
                }
                process();
            }
        });

        t.start();

        //优雅关闭
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {

            isRunning = false;
            ExecutorUtil.gracefulShutdown(ThreadPoolFactory.getRetryExecutor(), 3000);
        }));
    }

    /***
     * 核心业务处理
     */
    private void process() {

        final boolean lockFlag = lock();

        try {

            DelayContext value = poll();

            if (value == null) {

                if (lockFlag) {
                    unLock();
                }
                return;
            }

            ThreadPoolFactory.getRetryExecutor().execute(() -> {
                try {
                    delayCallback.execute(value);
                } finally {
                    if (lockFlag) {
                        unLock();
                    }
                }
            });

        } catch (RejectedExecutionException ex) {

            if (lockFlag) {
                unLock();
            }
        }
    }

    private boolean lock() {

        boolean lockFlag = false;
        for (; ; ) {

            try {

                if (!semaphore.tryAcquire(30, TimeUnit.MILLISECONDS)) {
                    continue;
                }

                lockFlag = true;
                break;
            } catch (Throwable e) {
                log.warn("获取信号量", e);
            }
        }
        return lockFlag;
    }

    private void unLock() {

        semaphore.release();
    }

    private DelayContext poll() {

        try {

            if (!isRunning) {
                return null;
            }

            String txtContext = this.delayQueue.poll();
            if (txtContext == null) {
                TimeUnit.MILLISECONDS.sleep(500L);
                return null;
            }

            return JSON.parseObject(txtContext, DelayContext.class);

        } catch (Throwable e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }
}
