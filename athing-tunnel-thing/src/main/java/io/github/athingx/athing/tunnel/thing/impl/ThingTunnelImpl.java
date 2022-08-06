package io.github.athingx.athing.tunnel.thing.impl;

import io.github.athingx.athing.thing.api.op.OpBind;
import io.github.athingx.athing.tunnel.thing.ThingTunnel;
import io.github.athingx.athing.tunnel.thing.impl.core.Tunnel;

public class ThingTunnelImpl implements ThingTunnel {

    private final Tunnel tunnel;
    private final OpBind bind;

    public ThingTunnelImpl(Tunnel tunnel, OpBind bind) {
        this.tunnel = tunnel;
        this.bind = bind;
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
        this.bind.unbind()
                .thenAccept(none -> tunnel.destroy())
                .get();
    }
}
