package com.trade.forward.app.infra.cache.retry;

import com.trade.forward.app.infra.cache.delay.DelayContext;
import lombok.Setter;


/**
 * <p>文件名称：DescendingRetryPolicy  </p>
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
public class DescendingRetryPolicy implements RetryPolicy {

    @Setter
    private int[] delaySeconds = {0, 30, 60, 120, 240, 600, 1200};

    @Override
    public boolean isValid(DelayContext delayContext) {
        return delayContext != null && delayContext.getCurExecuteCnt() < delaySeconds.length;
    }

    @Override
    public Integer getNextDelayTime(DelayContext delayContext) {

        int nextExecuteCnt = delayContext.getCurExecuteCnt();
        if (nextExecuteCnt < delaySeconds.length) {
            return delaySeconds[nextExecuteCnt];
        }
        return null;
    }

}
