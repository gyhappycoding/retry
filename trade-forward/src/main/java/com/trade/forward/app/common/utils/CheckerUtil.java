package com.trade.forward.app.common.utils;

import org.springframework.lang.Nullable;

/**
 * <p>文件名称：CheckerUtil</p>
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
public class CheckerUtil {

    /**
     * 请求参数校验
     *
     * @param expression   是否符合条件;
     * @param errorMessage 异常说明;
     * @throws IllegalArgumentException if {@code expression} is false
     */
    public static void checkArgument(boolean expression, @Nullable Object errorMessage) {

        if (!expression) {
            throw new IllegalArgumentException(String.valueOf(errorMessage));
        }
    }
}

