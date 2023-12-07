package com.trade.forward.app.infra.cache.delay;

/**
 * <p>文件名称：DelayCallback </p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/3/8 下午7:49.
 */


public interface DelayCallback {

    void execute(DelayContext value);
}
