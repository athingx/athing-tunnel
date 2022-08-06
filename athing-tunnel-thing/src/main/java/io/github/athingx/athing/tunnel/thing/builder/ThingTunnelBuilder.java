package io.github.athingx.athing.tunnel.thing.builder;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.tunnel.thing.ThingTunnel;
import io.github.athingx.athing.tunnel.thing.impl.ThingTunnelImpl;
import io.github.athingx.athing.tunnel.thing.impl.binding.BindForDebug;
import io.github.athingx.athing.tunnel.thing.impl.core.Tunnel;
import io.github.athingx.athing.tunnel.thing.impl.core.TunnelConfig;

import java.net.URI;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static io.github.athingx.athing.tunnel.thing.impl.core.util.CheckUtils.check;
import static java.util.Objects.requireNonNull;

public class ThingTunnelBuilder {

    private String secret;
    private int thread = 1;
    private String remote = "wss://backend-iotx-remote-debug.aliyun.com:443";
    private long connectTimeoutMs = 10L * 1000;
    private long handshakeTimeoutMs = 10L * 1000;
    private long pingIntervalMs = 30L * 1000;
    private long reconnectIntervalMs = 30L * 1000;
    private long idleIntervalMs = 15L * 60 * 1000;
    private final Set<URI> providers = new LinkedHashSet<>();

    public ThingTunnelBuilder secret(String secret) {
        this.secret = secret;
        return this;
    }

    public ThingTunnelBuilder thread(int thread) {
        this.thread = thread;
        return this;
    }

    public ThingTunnelBuilder remote(String remote) {
        this.remote = remote;
        return this;
    }

    public ThingTunnelBuilder connectTimeoutMs(long connectTimeoutMs) {
        this.connectTimeoutMs = connectTimeoutMs;
        return this;
    }

    public ThingTunnelBuilder handshakeTimeoutMs(long handshakeTimeoutMs) {
        this.handshakeTimeoutMs = handshakeTimeoutMs;
        return this;
    }

    public ThingTunnelBuilder pingIntervalMs(long pingIntervalMs) {
        this.pingIntervalMs = pingIntervalMs;
        return this;
    }

    public ThingTunnelBuilder reconnectIntervalMs(long reconnectIntervalMs) {
        this.reconnectIntervalMs = reconnectIntervalMs;
        return this;
    }

    public ThingTunnelBuilder idleIntervalMs(long idleIntervalMs) {
        this.idleIntervalMs = idleIntervalMs;
        return this;
    }

    public ThingTunnelBuilder provider(String uri) {
        this.providers.add(URI.create(uri));
        return this;
    }

    private void setupAccess(TunnelConfig config, Thing thing) {
        config.setAccess(new TunnelConfig.Access(
                thing.path().getProductId(),
                thing.path().getThingId(),
                secret
        ));
    }

    private void setupThread(TunnelConfig config) {
        config.setThreads(thread);
    }

    private void setupConnect(TunnelConfig config) {
        final var connect = config.getConnect();
        connect.setConnectTimeoutMs(connectTimeoutMs);
        connect.setHandshakeTimeoutMs(handshakeTimeoutMs);
        connect.setIdleIntervalMs(idleIntervalMs);
        connect.setRemote(remote);
        connect.setPingIntervalMs(pingIntervalMs);
        connect.setReconnectIntervalMs(reconnectIntervalMs);
    }

    private void setupService(TunnelConfig config) {
        final var services = config.getServices();
        providers.forEach(provider -> {

            final var type = provider.getScheme();
            final var host = provider.getHost();
            final var port = provider.getPort();
            final var dict = Arrays
                    .stream(provider.getQuery().split("&"))
                    .map(segment -> segment.split("="))
                    .filter(pairs -> pairs.length == 2)
                    .collect(Collectors.toMap(pairs -> pairs[0], pairs -> pairs[1], (a, b) -> b));

            final var name = dict.getOrDefault("name", type);
            final var service = new TunnelConfig.Service(name, type, host, port);

            if (dict.containsKey("connectTimeout")) {
                service.getOption().setConnectTimeoutMs(Long.parseLong(dict.get("connectTimeout")));
            }

            services.add(service);

        });
    }

    public CompletableFuture<ThingTunnel> build(Thing thing) throws Exception {

        requireNonNull(secret, "secret is required");
        requireNonNull(remote, "remote is required");
        check(!providers.isEmpty(), "provider is required");

        final var config = new TunnelConfig();
        setupAccess(config, thing);
        setupThread(config);
        setupConnect(config);
        setupService(config);

        final var name = "%s/tunnel".formatted(thing.path());
        final var tunnel = new Tunnel(name, config);

        final var group = thing.op().binding();
        group.bindFor(new BindForDebug(thing, tunnel));

        return group
                .commit()
                .thenApply(bind -> new ThingTunnelImpl(tunnel, bind));
    }

}
