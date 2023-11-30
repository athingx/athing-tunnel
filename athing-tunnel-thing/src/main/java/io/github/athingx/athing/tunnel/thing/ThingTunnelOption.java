package io.github.athingx.athing.tunnel.thing;

import java.util.concurrent.TimeUnit;

public class ThingTunnelOption {

    private long httpConnectTimeoutMs = TimeUnit.SECONDS.toMillis(30L);
    private long httpTimeoutMs = TimeUnit.SECONDS.toMillis(30L);

    public long getHttpTimeoutMs() {
        return httpTimeoutMs;
    }

    public ThingTunnelOption setHttpTimeoutMs(long httpTimeoutMs) {
        this.httpTimeoutMs = httpTimeoutMs;
        return this;
    }

    public long getHttpConnectTimeoutMs() {
        return httpConnectTimeoutMs;
    }

    public ThingTunnelOption setHttpConnectTimeoutMs(long httpConnectTimeoutMs) {
        this.httpConnectTimeoutMs = httpConnectTimeoutMs;
        return this;
    }

}
