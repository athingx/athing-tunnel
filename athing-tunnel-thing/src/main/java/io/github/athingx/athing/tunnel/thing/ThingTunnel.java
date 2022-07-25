package io.github.athingx.athing.tunnel.thing;

/**
 * 设备隧道
 */
public interface ThingTunnel extends AutoCloseable {

    /**
     * 启用隧道
     */
    void enable();

    /**
     * 禁用隧道
     */
    void disable();

    /**
     * 隧道是否启用
     *
     * @return TRUE | FALSE
     */
    boolean isEnable();

}
