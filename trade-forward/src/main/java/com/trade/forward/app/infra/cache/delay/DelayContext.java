package com.trade.forward.app.infra.cache.delay;

import lombok.Data;
import java.io.Serializable;
import java.util.Map;

/**
 * <p>文件名称：DelayContext</p>
 * <p>文件描述：</p>
 * <p>版权所有：</p>
 * <p>公   司： </p>
 * <p>内容摘要： </p>
 * <p>其他说明： </p>
 *
 * @author guyan
 * @version 1.0
 * @Date :Created by 2022/3/9 下午12:32.
 */

@Data
public class DelayContext implements Serializable {

    private static final long serialVersionUID = -5286043761268637502L;

    /***
     * 业务key
     */
    private String key;

    /**
     * 延迟的具体参数
     */
    private String value;

    /**
     * 【禁止操作】 当前执行次数(兼容历史，框架自动操作，业务不能操作)
     */
    private int curExecuteCnt;

    /**
     * 最大执行次数
     */
    private int retryTimes;

    /***
     * 创建时间
     */
    private long creatTime;

    private Map<String, String> parameterMap;

}
