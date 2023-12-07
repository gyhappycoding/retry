package com.trade.forward.app.infra.cache.delay;

import com.trade.forward.app.common.utils.CheckerUtil;
import lombok.Setter;
import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * <p>文件名称：DelayQueueProducer</p>
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
public class DelayQueueProducer {

    /**
     * 2038年1月1日0点0分0秒
     */
    private static final long MAX_SCORE_TIME = 2145888000000L;

    @Setter
    private String topic;

    /**
     * 单位秒,默认10分钟
     */
    @Setter
    private long delayTime = 600;

    @Setter
    private StringRedisTemplate redisTemplate;

    private volatile RedisDelayQueue delayQueue;

    /***
     * 延迟队列构造函数
     */
    public DelayQueueProducer() {

    }

    /***
     * 延迟队列构造函数
     * @param topic
     * @param delayTime
     * @param redisTemplate
     */
    public DelayQueueProducer(String topic,
        Long delayTime,
        StringRedisTemplate redisTemplate) {

        this.topic = topic;
        this.delayTime = delayTime;
        this.redisTemplate = redisTemplate;
        if (StringUtils.isBlank(topic)) {
            throw new IllegalArgumentException("topic is null!");
        }
    }

    public void init() {

        this.delayQueue = new RedisDelayQueue();
        this.delayQueue.init(topic, redisTemplate);
    }

    /***
     * 放入延时队列延迟(全局时间配置)
     * @param context 延时内容
     * @return
     */
    public boolean put(DelayContext context) {

        return put(context, delayTime);
    }

    /***
     * 放入延时队列延迟(局部时间配置)
     * @param context 延时内容
     * @param scoreTime  延时时间设置(秒)
     * @return
     */
    public boolean putWithScore(DelayContext context, long scoreTime) {

        check(context);

        if (scoreTime > MAX_SCORE_TIME || scoreTime < 0) {
            CheckerUtil.checkArgument(scoreTime > 0 && scoreTime <= MAX_SCORE_TIME,
                "延时队列scoreTime，非毫秒时间戳");
        }

        String txtContext = JSON.toJSONString(context);

        int times = 3;
        while (times-- > 0) {
            boolean flag = this.delayQueue.addWithScore(txtContext, scoreTime);
            if (flag) {
                return flag;
            }
        }

        return false;
    }


    /***
     * 放入延时队列延迟(局部时间配置)
     * @param context 延时内容
     * @param varDelayTime  延时时间设置(秒)
     * @return
     */
    public boolean put(DelayContext context, long varDelayTime) {

        check(context);

        long tempDelayTime = varDelayTime * 1000;
        String txtContext = JSON.toJSONString(context);

        int times = 3;
        while (times-- > 0) {
            boolean flag = this.delayQueue.add(txtContext, tempDelayTime);
            if (flag) {
                return flag;
            }
        }

        return false;
    }

    private void check(DelayContext context) {

        if (context == null) {
            throw new IllegalArgumentException("context is null");
        } else if (StringUtils.isBlank(context.getKey())) {
            throw new IllegalArgumentException("context key is null");
        } else if (StringUtils.isBlank(context.getValue())) {
            throw new IllegalArgumentException("context value is null");
        }
    }
}
