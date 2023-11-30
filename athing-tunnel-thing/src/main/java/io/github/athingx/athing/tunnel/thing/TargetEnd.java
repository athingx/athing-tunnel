package io.github.athingx.athing.tunnel.thing;

import io.github.athingx.athing.tunnel.thing.impl.util.IOUtils;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.channels.ByteChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import static io.github.athingx.athing.tunnel.thing.impl.util.IOUtils.connect;

public interface TargetEnd {

    String type();

    CompletableFuture<ByteChannel> connectAsync(Executor executor);

    record SocketEnd(String type, SocketAddress address, Option option) implements TargetEnd {

        public SocketEnd(String type, SocketAddress address) {
            this(type, address, new Option());
        }

        public SocketEnd(String type, String host, int port, Option option) {
            this(type, new InetSocketAddress(host, port), option);
        }

        public SocketEnd(String type, String host, int port) {
            this(type, new InetSocketAddress(host, port));
        }

        public static class Option {

            private long soTimeoutMs = TimeUnit.SECONDS.toMillis(30L);
            private long soConnectTimeoutMs = TimeUnit.SECONDS.toMillis(30L);
            private boolean isTcpNoDelay = true;
            private boolean isKeepAlive = true;

            public Option setSoTimeoutMs(long soTimeoutMs) {
                this.soTimeoutMs = soTimeoutMs;
                return this;
            }

            public Option setSoConnectTimeoutMs(long soConnectTimeoutMs) {
                this.soConnectTimeoutMs = soConnectTimeoutMs;
                return this;
            }

            public Option setTcpNoDelay(boolean isNoDelay) {
                this.isTcpNoDelay = isNoDelay;
                return this;
            }

            public Option setKeepAlive(boolean isKeepAlive) {
                this.isKeepAlive = isKeepAlive;
                return this;
            }

        }

        @Override
        public CompletableFuture<ByteChannel> connectAsync(Executor executor) {
            final var future = new CompletableFuture<ByteChannel>();
            executor.execute(() -> {
                SocketChannel channel = null;
                try {
                    channel = SocketChannel.open();
                    channel.socket().setKeepAlive(option().isKeepAlive);
                    channel.socket().setTcpNoDelay(option().isTcpNoDelay);
                    channel.socket().setSoTimeout((int) TimeUnit.SECONDS.toSeconds(option().soTimeoutMs));
                    connect(channel, address(), option().soConnectTimeoutMs);
                    future.complete(channel);
                } catch (Throwable cause) {
                    IOUtils.closeQuietly(channel);
                    future.completeExceptionally(cause);
                }
            });
            return future;
        }

    }

}

