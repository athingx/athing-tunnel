package io.github.athingx.athing.tunnel.thing.impl.client;

import io.github.athingx.athing.tunnel.thing.TargetEnd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;

public class TunnelClient {

    private final WebSocket socket;

    public TunnelClient(WebSocket socket) {
        this.socket = socket;
    }

    public void abort() {
        socket.abort();
    }

    public CompletableFuture<TunnelClient> close(String reason) {
        return socket.sendClose(1000, reason).thenApply(ignored -> this);
    }

    public static Builder newBuilder(String tunnelId) {
        return new Builder() {

            private long timeoutMs = TimeUnit.SECONDS.toMillis(30L);
            private long connectTimeoutMs = TimeUnit.SECONDS.toMillis(30L);
            private Executor executor;
            private Set<TargetEnd> ends;

            @Override
            public Builder timeoutMs(long timeoutMs) {
                this.timeoutMs = timeoutMs;
                return this;
            }

            @Override
            public Builder connectTimeoutMs(long timeoutMs) {
                this.connectTimeoutMs = timeoutMs;
                return this;
            }

            @Override
            public Builder executor(Executor executor) {
                this.executor = executor;
                return this;
            }

            @Override
            public Builder ends(Set<TargetEnd> ends) {
                this.ends = ends;
                return this;
            }

            @Override
            public CompletableFuture<TunnelClient> buildAsync(String token, URI remote, Handler handler) {

                requireNonNull(token, "token is required!");
                requireNonNull(remote, "remote is required!");
                requireNonNull(handler, "handler is required!");
                requireNonNull(executor, "executor is required!");
                requireNonNull(ends, "ends is required!");

                final var client = HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .connectTimeout(Duration.ofMillis(connectTimeoutMs))
                        .build();

                return client.newWebSocketBuilder()
                        .header("tunnel-access-token", token)
                        .subprotocols("subprotocol", "aliyun.iot.securetunnel-v1.1")
                        .connectTimeout(Duration.ofMillis(timeoutMs))
                        .buildAsync(remote, new TunnelWebSocketListener(executor, tunnelId, ends, handler))
                        .thenApply(TunnelClient::new);
            }
        };
    }

    public interface Builder {

        Builder timeoutMs(long timeoutMs);

        Builder connectTimeoutMs(long timeoutMs);

        Builder executor(Executor executor);

        Builder ends(Set<TargetEnd> ends);

        CompletableFuture<TunnelClient> buildAsync(String token, URI remote, Handler handler);

    }

    public interface Handler {

        void onConnected();

        void onDisconnected(int code, String reason);

        void onError(Throwable error);

    }

}
