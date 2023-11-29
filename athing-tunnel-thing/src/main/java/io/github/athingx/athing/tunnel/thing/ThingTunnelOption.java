package io.github.athingx.athing.tunnel.thing;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ThingTunnelOption {

    private long httpConnectTimeoutMs = TimeUnit.SECONDS.toMillis(30L);
    private long httpTimeoutMs = TimeUnit.SECONDS.toMillis(30L);
    private Set<TargetEnd> ends = new HashSet<>();

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

    public Set<TargetEnd> getEnds() {
        return ends;
    }

    public ThingTunnelOption setEnds(Set<TargetEnd> ends) {
        this.ends = ends;
        return this;
    }

}
