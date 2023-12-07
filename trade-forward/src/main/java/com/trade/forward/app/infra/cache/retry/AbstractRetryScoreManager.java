package com.trade.forward.app.infra.cache.retry;

import com.trade.forward.app.infra.cache.delay.DelayContext;
import lombok.extern.slf4j.Slf4j;

/**
 * <p>文件名称：AbstractRetryScoreManager</p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/12/24.
 */
@Slf4j
public abstract class AbstractRetryScoreManager extends AbstractRetryManager {

    @Override
    public void init() {

        super.init();
    }

    /***
     * 加入延时队列
     * @param context
     * @param delayTime(秒)
     */
    @Override
    protected void add(DelayContext context, long delayTime) {

        if (proxyDelayQueue == null) {
            return;
        }

        long nanoDelayTime = System.currentTimeMillis() + delayTime * 1000;
        proxyDelayQueue.putWithScore(context, nanoDelayTime);
    }

    /***
     * 加入延时队列(score时间设置)
     * scoreTime + retryPolicy.getNextDelayTime(context) * 1000
     * @param context  内容
     * @param scoreTime 当前毫秒时间
     */
    public void addWithDelayScore(DelayContext context, long scoreTime) {

        if (proxyDelayQueue == null) {
            return;
        }

        long delayTime = scoreTime
            + retryPolicy.getNextDelayTime(context) * 1000;
        proxyDelayQueue.putWithScore(context, delayTime);
    }

    /***
     * 加入延时队列(score时间设置)
     * @param context  内容
     * @param scoreTime 当前毫秒时间
     * @param delayPeriod 延迟周期（秒）
     *
     */
    public void addWithDelayScore(DelayContext context, long scoreTime, long delayPeriod) {

        if (proxyDelayQueue == null) {
            return;
        }

        long delayTime = scoreTime + delayPeriod * 1000;
        proxyDelayQueue.putWithScore(context, delayTime);
    }
}
