package io.github.athingx.athing.tunnel.thing;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.plugin.ThingPluginInstaller;
import io.github.athingx.athing.tunnel.thing.impl.ThingTunnelImpl;
import io.github.athingx.athing.tunnel.thing.impl.binding.OpBindingForNotify;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ThingTunnelInstaller implements ThingPluginInstaller<ThingTunnel> {

    private ThingTunnelOption option = new ThingTunnelOption();
    private Set<TargetEnd> ends = new HashSet<>();
    private Executor executor;

    public ThingTunnelInstaller option(ThingTunnelOption option) {
        this.option = option;
        return this;
    }

    public ThingTunnelInstaller executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public ThingTunnelInstaller ends(Set<TargetEnd> ends) {
        this.ends = ends;
        return this;
    }

    public ThingTunnelInstaller end(TargetEnd end) {
        this.ends.add(end);
        return this;
    }

    @Override
    public Meta<ThingTunnel> meta() {
        return new Meta<>(ThingTunnel.PLUGIN_ID, ThingTunnel.class);
    }

    @Override
    public CompletableFuture<ThingTunnel> install(Thing thing) {
        final var notifyF = new OpBindingForNotify(executor, option, ends).bind(thing);
        return CompletableFuture.allOf(notifyF)
                .thenApply(unused -> new ThingTunnelImpl(notifyF.join()));
    }

}
