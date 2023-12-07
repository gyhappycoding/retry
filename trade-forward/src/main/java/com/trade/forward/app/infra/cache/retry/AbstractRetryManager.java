package com.trade.forward.app.infra.cache.retry;

import com.alibaba.fastjson.JSON;
import com.trade.forward.app.infra.cache.delay.DelayCallback;
import com.trade.forward.app.infra.cache.delay.DelayContext;
import com.trade.forward.app.infra.cache.delay.ProxyDelayQueue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>文件名称：AbstractRetryManager  </p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/10/19.
 */
@Slf4j
public abstract class AbstractRetryManager {

    @Setter
    private String topic;

    @Setter
    private StringRedisTemplate redisTemplate;

    @Setter
    protected RetryPolicy retryPolicy;

    @Setter
    protected int concurrency = 30;

    protected ProxyDelayQueue proxyDelayQueue;

    public void init() {

        this.proxyDelayQueue = new ProxyDelayQueue(topic,
            1L,
            redisTemplate,
            concurrency,
            new DelayCallback() {

                @Override
                public void execute(DelayContext context) {

                    try {

                        boolean reslutFlag = exec(context);
                        if (reslutFlag || !retryPolicy.isValid(context)) {
                            return;
                        }
                    } catch (Throwable ex) {
                        log.warn(JSON.toJSONString(context) + "," + ex.getMessage(), ex);
                    }

                    //重试次数控制
                    context.setCurExecuteCnt(context.getCurExecuteCnt() + 1);
                    add(context, retryPolicy.getNextDelayTime(context));
                }
            });
        this.proxyDelayQueue.init();
    }

    /***
     * 加入延时队列
     * @param context
     * @param delayTime
     */
    protected void add(DelayContext context, long delayTime) {

        if (proxyDelayQueue == null) {
            return;
        }

        proxyDelayQueue.put(context, delayTime);
    }

    /***
     * 加入延时队列
     * @param context
     */
    public void add(DelayContext context) {

        add(context, retryPolicy.getNextDelayTime(context));
    }

    /***
     *  执行回调处理
     * @param context
     */
    protected abstract boolean exec(DelayContext context);

}
