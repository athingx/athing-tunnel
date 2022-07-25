package io.github.athingx.athing.tunnel.thing.impl.core.util;

/**
 * 检查工具类
 */
public class CheckUtils {

    /**
     * 检查
     *
     * @param test  条件
     * @param cause 错误原因
     */
    public static void check(boolean test, RuntimeException cause) {
        if (!test) {
            throw cause;
        }
    }

    /**
     * 检查
     *
     * @param test    条件
     * @param message 错误消息
     */
    public static void check(boolean test, String message) {
        check(test, new IllegalArgumentException(message));
    }

}
