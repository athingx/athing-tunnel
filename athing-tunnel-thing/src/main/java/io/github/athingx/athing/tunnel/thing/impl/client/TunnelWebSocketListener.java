package io.github.athingx.athing.tunnel.thing.impl.client;

import io.github.athingx.athing.common.gson.GsonFactory;
import io.github.athingx.athing.tunnel.thing.TargetEnd;
import io.github.athingx.athing.tunnel.thing.impl.client.protocol.*;
import io.github.athingx.athing.tunnel.thing.impl.util.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.nio.ByteOrder.BIG_ENDIAN;
import static java.nio.charset.StandardCharsets.UTF_8;

public class TunnelWebSocketListener implements WebSocket.Listener, Constants {

    private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.allocate(0);
    private static final CompletionStage<?> NONE_COMPLETED_STAGE = CompletableFuture.completedStage(null);
    private static final int MAX_HEADER_SIZE_2K = 2 * 1024;
    private static final int MAX_BODY_SIZE_4K = 4 * 1024;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final String tunnelId;
    private final Set<TargetEnd> ends;
    private final TunnelClient.Handler handler;


    private final ByteBuffer buffer = ByteBuffer.allocate(MAX_HEADER_SIZE_2K + MAX_BODY_SIZE_4K).order(BIG_ENDIAN);
    private final AtomicLong frameIdentityRef = new AtomicLong(1000);
    private final Map<String, Session> sessionMap = new ConcurrentHashMap<>();


    public TunnelWebSocketListener(String tunnelId, Set<TargetEnd> ends, TunnelClient.Handler handler) {
        this.tunnelId = tunnelId;
        this.ends = ends;
        this.handler = handler;
    }

    public void cleanSession() {
        final var cleanSessionIdSet = new HashSet<String>();
        sessionMap.forEach((sessionId, session) -> {
            cleanSessionIdSet.add(sessionId);
            session.close(false);
        });
        cleanSessionIdSet.forEach(sessionMap::remove);
    }

    @Override
    public void onOpen(WebSocket socket) {
        handler.onConnected();
        socket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(WebSocket socket, int code, String reason) {
        cleanSession();
        handler.onDisconnected(code, reason);
        return null;
    }

    @Override
    public void onError(WebSocket socket, Throwable error) {
        cleanSession();
        handler.onError(error);
    }

    private static ByteBuffer encodeFrame(Frame<?> frame) {
        final var hBytes = GsonFactory.getGson().toJson(frame.header()).getBytes(UTF_8);
        final var bBuffer = Optional.ofNullable(frame.data())
                .map(Frame.Data::toByteBuffer)
                .orElse(EMPTY_BUFFER);
        return ByteBuffer.allocate(Short.BYTES + hBytes.length + bBuffer.remaining()).order(BIG_ENDIAN)
                .putShort((short) hBytes.length)
                .put(hBytes)
                .put(bBuffer)
                .flip();
    }

    private static Frame<?> decodeFrame(ByteBuffer buffer) {

        // 解码帧头
        final var length = buffer.getShort();
        final var header = GsonFactory.getGson().fromJson(
                new String(buffer.array(), Short.BYTES, length, UTF_8),
                Header.class
        );
        buffer.position(Short.BYTES + length);

        // 解码帧数据
        final var frameType = header.frameType();
        final Frame.Data data;

        // 会话创建
        if (frameType == FRAME_TYPE_CREATE_SESSION) {
            data = new Payload(ByteBuffer.allocate(0));
        }

        // 会话关闭和通用应答
        else if (frameType == FRAME_TYPE_CLOSE_SESSION || frameType == FRAME_TYPE_COMMON_RESPONSE) {
            data = GsonFactory.getGson().fromJson(
                    new String(buffer.array(), buffer.position(), buffer.remaining(), UTF_8),
                    Response.class
            );
        }

        // 数据传输
        else if (frameType == FRAME_TYPE_DATA_TRANSPORT) {
            data = new Payload(buffer);
        }

        // 不认识的帧类型
        else {
            throw new IllegalArgumentException("unknown frame type: " + frameType);
        }

        // 完成解码
        return new Frame<>(header, data);

    }

    private static CompletableFuture<WebSocket> reply(WebSocket socket, Header header, int code, String reason) {
        return socket.sendBinary(encodeFrame(new Frame<>(header.toReply(), new Response(code, reason))), true);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket socket, ByteBuffer data, boolean last) {

        // 等待完整的帧数据
        buffer.put(data);
        if (!last) {
            socket.request(1);
            return null;
        }

        buffer.flip();

        // 解析帧数据
        final var frame = decodeFrame(buffer);

        // 处理帧
        return Optional.<CompletionStage<?>>ofNullable(onFrame(socket, frame))
                .orElse(NONE_COMPLETED_STAGE)
                .thenAccept(unused -> {
                    buffer.clear();
                    socket.request(1);
                });
    }

    private TargetEnd balancingTargetEnd(Header header) {
        final var founds = ends.stream()
                .filter(end -> end.type().equals(header.serviceType()))
                .toArray(TargetEnd[]::new);
        return founds.length > 0
                ? founds[(header.sessionId().hashCode() % founds.length)]
                : null;
    }

    private CompletionStage<?> onFrame(WebSocket socket, Frame<?> frame) {

        // 创建会话
        if (frame.header().frameType() == FRAME_TYPE_CREATE_SESSION) {

            final var sessionId = frame.header().sessionId();
            final var end = balancingTargetEnd(frame.header());
            if (null == end) {
                return reply(
                        socket,
                        frame.header(),
                        CODE_CREATE_SESSION_REJECT,
                        "service-type: %s not supported!".formatted(frame.header().serviceType())
                );
            }

            try {

                // 创建会话
                final var session = new Session(sessionId, end, socket).connect();
                sessionMap.put(sessionId, session);

                // 响应会话创建
                return reply(socket, frame.header(), CODE_CREATE_SESSION_SUCCESS, "success")
                        .thenApply(unused -> {
                            session.start();
                            logger.info("tunnel-client://{}/{} session opened! service={};",
                                    tunnelId,
                                    sessionId,
                                    frame.header().serviceType()
                            );
                            return unused;
                        })
                        .exceptionally(cause -> {
                            if (sessionMap.remove(sessionId) == session) {
                                session.close(false);
                            }
                            return null;
                        });

            } catch (IOException cause) {
                logger.warn("tunnel-client://{}/{} session open error!",
                        tunnelId,
                        sessionId,
                        cause
                );
                return reply(socket, frame.header(), CODE_CREATE_SESSION_FAILURE, cause.getMessage());
            }

        }

        // 关闭会话
        else if (frame.header().frameType() == FRAME_TYPE_CLOSE_SESSION) {
            final var sessionId = frame.header().sessionId();
            final var session = sessionMap.remove(sessionId);
            final var response = (Response) frame.data();
            if (null != session) {
                session.close(false);
                logger.info("tunnel-client://{}/{} session closed! code={};reason={};",
                        tunnelId,
                        sessionId,
                        response.code(),
                        response.message()
                );
            }
        }

        // 数据传输
        else if (frame.header().frameType() == FRAME_TYPE_DATA_TRANSPORT) {
            final var sessionId = frame.header().sessionId();
            final var session = sessionMap.get(sessionId);
            if (null != session) {
                try {
                    final var buffer = frame.data().toByteBuffer();
                    while (buffer.hasRemaining()) {
                        if (session.write(buffer) <= 0) {
                            throw new EOFException("write EOF!");
                        }
                    }
                } catch (IOException cause) {
                    logger.warn("tunnel-client://{}/{} session transport data error!",
                            tunnelId,
                            sessionId,
                            cause
                    );
                    if (sessionMap.remove(sessionId) == session) {
                        session.close(true);
                    }
                }
            }
        }

        return null;
    }


    /**
     * 会话
     */
    private class Session {

        private final String sessionId;
        private final TargetEnd end;
        private final WebSocket socket;
        private final SocketChannel channel;
        private final AtomicReference<State> stateRef = new AtomicReference<>(State.INIT);
        private final Thread worker;

        Session(String sessionId, TargetEnd end, WebSocket socket) throws IOException {
            this.sessionId = sessionId;
            this.end = end;
            this.socket = socket;
            this.channel = SocketChannel.open();
            this.worker = init();
        }

        private Thread init() {
            final var name = "tunnel-client://%s/%s/reader".formatted(tunnelId, sessionId);
            return new Thread(() -> {
                logger.info("{} started!", name);
                final var buffer = ByteBuffer.allocate(MAX_BODY_SIZE_4K);
                try {
                    while (!Thread.currentThread().isInterrupted()) {

                        // 读取数据
                        if (channel.read(buffer) <= 0) {
                            throw new EOFException("read EOF!");
                        }
                        buffer.flip();

                        // 写入数据
                        while (buffer.hasRemaining()) {
                            final var frame = new Frame<>(
                                    new Header(
                                            frameIdentityRef.incrementAndGet(),
                                            FRAME_TYPE_DATA_TRANSPORT,
                                            sessionId,
                                            end.type()
                                    ),
                                    new Payload(buffer)
                            );
                            socket.sendBinary(encodeFrame(frame), true).join();
                        }

                        // 清理数据缓冲区
                        buffer.clear();
                    }
                } catch (Exception cause) {

                    if (cause instanceof ClosedByInterruptException) {
                        Thread.currentThread().interrupt();
                    } else {
                        logger.warn("tunnel-client://{}/{} transport data error!",
                                tunnelId,
                                sessionId,
                                cause
                        );
                    }

                    close(true);
                } finally {
                    logger.info("{} stopped!", name);
                }
            }, name);
        }

        void start() {
            if (stateRef.compareAndSet(State.INIT, State.STARTED)) {
                worker.start();
            }
        }

        void stop() {
            if (stateRef.compareAndSet(State.STARTED, State.STOPPED)) {
                worker.interrupt();
            }
        }

        Session connect() throws IOException {
            channel.connect(end.address());
            return this;
        }

        void close(boolean isNotify) {
            stop();
            IOUtils.closeQuietly(channel);
            if (isNotify) {
                final var frame = new Frame<>(
                        new Header(
                                frameIdentityRef.incrementAndGet(),
                                FRAME_TYPE_CLOSE_SESSION,
                                sessionId,
                                end.type()
                        ),
                        new Response(CODE_CLOSED_SESSION_BY_CLIENT, "closed by client")
                );
                socket.sendBinary(encodeFrame(frame), true).join();
            }
        }

        int write(ByteBuffer buffer) throws IOException {
            return channel.write(buffer);
        }

        private enum State {
            INIT, STARTED, STOPPED
        }

    }

}
