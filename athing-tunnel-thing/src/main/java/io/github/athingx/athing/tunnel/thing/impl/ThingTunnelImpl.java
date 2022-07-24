package io.github.athingx.athing.tunnel.thing.impl;

import io.github.athingx.athing.thing.api.op.OpBinder;
import io.github.athingx.athing.tunnel.thing.ThingTunnel;
import io.github.athingx.athing.tunnel.thing.impl.core.Tunnel;

public class ThingTunnelImpl implements ThingTunnel {

    private final Tunnel tunnel;
    private final OpBinder binder;

    public ThingTunnelImpl(Tunnel tunnel, OpBinder binder) {
        this.tunnel = tunnel;
        this.binder = binder;
    }

    @Override
    public void enable() {
        this.tunnel.connect();
    }

    @Override
    public void disable() {
        this.tunnel.disconnect();
    }

    @Override
    public boolean isEnable() {
        return this.tunnel.isConnected();
    }

    @Override
    public void close() throws Exception {
        this.binder.unbind()
                .thenAccept(none -> tunnel.destroy())
                .get();
    }
}
