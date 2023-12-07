package com.trade.forward.app.infra.cache.delay;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>文件名称：ProxyDelayQueue</p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/3/8 下午7:31.
 */
@Slf4j
public class ProxyDelayQueue {

    private DelayQueueConsumer consumer;

    private DelayQueueProducer producer;

    /***
     * 延迟队列构造函数
     * @param topic
     * @param delayTime
     * @param redisTemplate
     */
    public ProxyDelayQueue(String topic,
        Long delayTime,
        StringRedisTemplate redisTemplate,
        int concurrency,
        DelayCallback delayCallback
    ) {

        this.consumer = new DelayQueueConsumer(topic, redisTemplate, delayCallback, concurrency);
        this.producer = new DelayQueueProducer(topic, delayTime, redisTemplate);
    }

    public void init() {

        this.producer.init();
        this.consumer.init();
    }

    /***
     * 放入延时队列延迟(全局时间配置)
     * @param context 延时内容
     * @return
     */
    public boolean put(DelayContext context) {

        return this.producer.put(context);
    }

    /***
     * 放入延时队列延迟(局部时间配置)
     * @param context 延时内容
     * @param varDelayTime  延时时间设置(秒)
     * @return
     */
    public boolean put(DelayContext context, long varDelayTime) {

        return this.producer.put(context, varDelayTime);
    }

    /***
     * 放入延时队列延迟(局部时间配置)
     * @param context 延时内容
     * @param scoreTime  延时时间设置(秒)
     * @return
     */
    public boolean putWithScore(DelayContext context, long scoreTime) {

        return this.producer.putWithScore(context, scoreTime);
    }
}
