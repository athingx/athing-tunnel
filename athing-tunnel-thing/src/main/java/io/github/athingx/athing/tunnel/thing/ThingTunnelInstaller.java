package io.github.athingx.athing.tunnel.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.plugin.ThingPluginInstaller;
import io.github.athingx.athing.tunnel.thing.impl.ThingTunnelImpl;
import io.github.athingx.athing.tunnel.thing.impl.binding.OpBindingForNotify;

import java.util.concurrent.CompletableFuture;

public class ThingTunnelInstaller implements ThingPluginInstaller<ThingTunnel> {

    private ThingTunnelOption option = new ThingTunnelOption();

    public ThingTunnelInstaller option(ThingTunnelOption option) {
        this.option = option;
        return this;
    }

    @Override
    public Meta<ThingTunnel> meta() {
        return new Meta<>(ThingTunnel.PLUGIN_ID, ThingTunnel.class);
    }

    @Override
    public CompletableFuture<ThingTunnel> install(Thing thing) {
        final var notifyF = new OpBindingForNotify(option).bind(thing);
        return CompletableFuture.allOf(notifyF)
                .thenApply(unused -> new ThingTunnelImpl(notifyF.join()));
    }

}
