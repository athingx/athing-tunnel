package io.github.athingx.athing.tunnel.thing.impl;

import io.github.athingx.athing.thing.api.op.OpBinder;
import io.github.athingx.athing.tunnel.thing.ThingTunnel;

import java.util.concurrent.CompletableFuture;

public class ThingTunnelImpl implements ThingTunnel {

    private final OpBinder notify;

    public ThingTunnelImpl(OpBinder notify) {
        this.notify = notify;
    }

    @Override
    public CompletableFuture<Void> uninstall() {
        return CompletableFuture.allOf(notify.unbind());
    }

}
