package com.trade.forward.app.infra.cache.retry;

import com.alibaba.fastjson.JSON;
import com.trade.forward.app.infra.cache.delay.DelayContext;
import com.trade.forward.app.infra.cache.delay.DelayRetryCallback;
import com.trade.forward.app.infra.cache.delay.ProxyDelayQueue;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.Arrays;

/**
 * <p>文件名称：DelayRetryContainer  </p>
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
public class DelayRetryContainer {

    @Setter
    private String topic;

    @Setter
    private StringRedisTemplate redisTemplate;

    @Setter
    private String retrySequence;

    @Setter
    private DelayRetryCallback retryCallback;


    private ProxyDelayQueue proxyDelayQueue;

    private RetryPolicy retryPolicy;

    @Setter
    private int concurrency = 30;

    public void init() {

        if (StringUtils.isNotBlank(retrySequence)) {

            retryPolicy = parseRetryPolicy(retrySequence);
        } else {

            retryPolicy = new RetryPolicy.DefaultDescending();
        }

        this.proxyDelayQueue = new ProxyDelayQueue(topic,
            1L,
            redisTemplate,
            concurrency,
            context -> {
                try {
                    boolean resultFlag = retryCallback.execute(context);
                    if (resultFlag) {
                        return;
                    }

                } catch (Throwable ex) {

                    log.warn(JSON.toJSONString(context) + "," + ex.getMessage(), ex);
                }

                //重试次数控制
                context.setCurExecuteCnt(context.getCurExecuteCnt() + 1);

                if (!retryPolicy.isValid(context)) {

                    log.info("延迟重试结束");

                    retryCallback.onFail(context);

                    return;
                }

                log.info("延迟重试进行次数：{}", context.getCurExecuteCnt());

                add(context, retryPolicy.getNextDelayTime(context));

            });

        this.proxyDelayQueue.init();
    }

    private RetryPolicy parseRetryPolicy(String descendingPolicy) {

        int[] delaySeconds = Arrays.stream(descendingPolicy.split(",")).mapToInt(Integer::parseInt).toArray();

        DescendingRetryPolicy retryPolicy = new DescendingRetryPolicy();
        retryPolicy.setDelaySeconds(delaySeconds);

        return retryPolicy;

    }

    /***
     * 加入延时队列
     * @param context
     * @param delayTime
     */
    private final void add(DelayContext context, long delayTime) {

        if (proxyDelayQueue == null) {
            return;
        }

        proxyDelayQueue.put(context, delayTime);
    }

    /***
     * 加入延时队列
     * @param context
     */
    public final void add(DelayContext context) {

        add(context, retryPolicy.getNextDelayTime(context));
    }

}
