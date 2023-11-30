package io.github.athingx.athing.tunnel.thing.impl.util;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (java.io.IOException e) {
                // ignore
            }
        }
    }

    public static void connect(SocketChannel channel, SocketAddress address, long soConnectTimeoutMs) throws IOException {

        // 保存现场
        final var isBlocking = channel.isBlocking();

        // 连接
        try (final var selector = Selector.open()) {

            channel.configureBlocking(false);
            channel.register(selector, SelectionKey.OP_CONNECT);
            channel.connect(address);

            // 等待连接超时
            if (selector.select(soConnectTimeoutMs) <= 0) {
                throw new IOException("connect timeout!");
            }

            // 处理连接
            for (final var key : selector.selectedKeys()) {
                if (key.isConnectable()) {
                    if (!channel.finishConnect()) {
                        throw new IOException("finish connect failure!");
                    }
                    key.cancel();
                    return;
                }
            }

            // 这里不应该到达
            throw new IOException("connect failure!");
        }

        // 恢复现场
        finally {
            channel.configureBlocking(isBlocking);
        }

    }

}
