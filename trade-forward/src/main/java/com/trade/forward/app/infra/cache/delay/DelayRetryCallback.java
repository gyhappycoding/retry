package com.trade.forward.app.infra.cache.delay;

/**
 * <p>文件名称：DelayRetryCallback</p>
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
public interface DelayRetryCallback {

	/**
	 * 执行重试的业务操作
	 * @param value
	 * @return
	 */
	boolean execute(DelayContext value);

	/**
	 * 重试最终失败时的回调处理
	 * @param value
	 */
	void onFail(DelayContext value);
}
