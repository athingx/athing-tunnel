package io.github.athingx.athing.tunnel.thing;

import java.net.InetSocketAddress;
import java.net.SocketAddress;


/**
 * 目标端
 *
 * @param type    类型
 * @param address 地址
 */
public record TargetEnd(String type, SocketAddress address, Option option) {

    public TargetEnd(String type, SocketAddress address) {
        this(type, address, new Option());
    }

    public static TargetEnd valueOf(String type, String host, int port, Option option) {
        return new TargetEnd(type, new InetSocketAddress(host, port), option);
    }

    public static TargetEnd valueOf(String type, String host, int port) {
        return new TargetEnd(type, new InetSocketAddress(host, port));
    }

    public static class Option {

        private long timeoutMs = 30000L;
        private long connectTimeoutMs = 30000L;

        public long getTimeoutMs() {
            return timeoutMs;
        }

        public Option setTimeoutMs(long timeoutMs) {
            this.timeoutMs = timeoutMs;
            return this;
        }

        public long getConnectTimeoutMs() {
            return connectTimeoutMs;
        }

        public Option setConnectTimeoutMs(long connectTimeoutMs) {
            this.connectTimeoutMs = connectTimeoutMs;
            return this;
        }
    }

}
