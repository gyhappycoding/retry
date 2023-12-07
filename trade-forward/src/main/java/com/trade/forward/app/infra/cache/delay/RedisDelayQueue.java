package com.trade.forward.app.infra.cache.delay;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.integration.redis.util.RedisLockRegistry;

/**
 * <p>文件名称：RedisDelayQueue  </p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司：</p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/3/9 下午12:32.
 */
public class RedisDelayQueue {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedisDelayQueue.class);

    private long DEFAULT_WAIT_TIME = 150;

    private String topic;

    private Lock redisLock;

    private RedisTemplate redisTemplate;

    /**
     * 初始化延迟队列
     *
     * @param topic 主题
     */
    public void init(String topic, RedisTemplate redisTemplate) {

        this.topic = topic;
        this.redisTemplate = redisTemplate;
        RedisLockRegistry redisLockRegistry = new RedisLockRegistry(redisTemplate.getConnectionFactory(),
            "task:delay:lock:" + topic);
        this.redisLock = redisLockRegistry.obtain(topic);
    }

    /**
     * 将元素插入延迟队列
     *
     * @param e     the element to add
     * @param delay 基于当前时间的延迟执行时间，单位： MILLISECONDS
     * @return 新增结果
     */
    public boolean add(String e, long delay) {

        try {
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.add(topic, e, System.currentTimeMillis() + delay);
        } catch (Exception ex) {
            LOGGER.error("error add new element to redisDelayQueue,topic={}||element={}||delay={}",
                topic, e, delay, ex);
        }

        return false;
    }

    /**
     * 将元素插入延迟队列
     *
     * @param e         the element to add
     * @param scoreTime nanoTime
     * @return 新增结果
     */
    public boolean addWithScore(String e, long scoreTime) {

        try {
            ZSetOperations<String, String> zSetOperations = redisTemplate.opsForZSet();
            return zSetOperations.add(topic, e, scoreTime);
        } catch (Exception ex) {
            LOGGER.error("error addWithScore new element to redisDelayQueue,topic={}||element={}||delay={}",
                topic, e, scoreTime, ex);
        }

        return false;
    }

    /**
     * 获取符合条件的元素
     *
     * @return 返回并删除符合时间要求的元素
     * @throws Exception 异常情况
     */
    public String poll() throws Exception {

        boolean locked = false;

        try {

            locked = redisLock.tryLock(DEFAULT_WAIT_TIME, TimeUnit.MILLISECONDS);
            if (!locked) {
                return null;
            }

            ZSetOperations zSetOperations = redisTemplate.opsForZSet();
            Set set = zSetOperations.rangeWithScores(topic, 0, 0);

            if (set.isEmpty()) {
                return null;
            }

            String value = (String) ((ZSetOperations.TypedTuple) set.toArray()[0]).getValue();
            Double score = ((ZSetOperations.TypedTuple) set.toArray()[0]).getScore();

            if (score != null && score.longValue() <= System.currentTimeMillis()) {
                zSetOperations.remove(topic, value);
                return value;
            }

            return null;
        } finally {
            if (locked) {
                redisLock.unlock();
            }
        }
    }
}
