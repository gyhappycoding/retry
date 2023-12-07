package com.trade.forward.app.infra.cache.retry;

import com.trade.forward.app.infra.cache.delay.DelayContext;


/**
 * <p>文件名称：RetryPolicy  </p>
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
public interface RetryPolicy {

    /**
     * 任务是否还有效，如果无效 直接丢弃 请注意判空
     *
     * @param delayContext
     * @return
     */
    boolean isValid(DelayContext delayContext);

    /***
     * 下一次延时时间
     * @param delayContext
     * @return
     */
    public Integer getNextDelayTime(DelayContext delayContext);

    /**
     * 默认的降幂策略
     */
    class DefaultDescending implements RetryPolicy {

        //单位秒
        private static int[] RETRY_DELAY_MINS = {0, 30, 60, 120, 240, 600, 1200};

        @Override
        public boolean isValid(DelayContext delayContext) {

            return delayContext != null && delayContext.getCurExecuteCnt() < RETRY_DELAY_MINS.length;
        }

        public Integer getNextDelayTime(DelayContext delayContext) {

            int nextExecuteCnt = delayContext.getCurExecuteCnt();
            if (nextExecuteCnt < RETRY_DELAY_MINS.length) {
                return RETRY_DELAY_MINS[nextExecuteCnt];
            }
            return null;
        }
    }
}
