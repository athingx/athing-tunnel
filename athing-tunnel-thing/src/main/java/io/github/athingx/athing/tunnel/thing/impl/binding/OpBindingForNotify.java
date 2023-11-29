package io.github.athingx.athing.tunnel.thing.impl.binding;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.op.Decoder;
import io.github.athingx.athing.thing.api.op.OpBinder;
import io.github.athingx.athing.thing.api.op.OpBinding;
import io.github.athingx.athing.tunnel.thing.ThingTunnelOption;
import io.github.athingx.athing.tunnel.thing.impl.client.TunnelClient;
import io.github.athingx.athing.tunnel.thing.impl.client.protocol.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.athingx.athing.common.util.JsonObjectUtils.*;
import static java.nio.charset.StandardCharsets.UTF_8;

public class OpBindingForNotify implements OpBinding<OpBinder>, Constants {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ThingTunnelOption option;
    private final Map<String, TunnelClient> clientMap = new ConcurrentHashMap<>();

    public OpBindingForNotify(ThingTunnelOption option) {
        this.option = option;
    }

    // 连接操作
    private synchronized void opConnect(Thing thing, String tunnelId, JsonObject root) {

        final var remote = requireAsUri(root, "uri");
        final var token = requireAsString(root, "token");
        final var expire = requireAsInt(root, "token_expire");

        // 检查隧道是否已经连接
        if (clientMap.containsKey(tunnelId)) {
            logger.warn("{}/tunnel already connected! tunnel={};", thing.path(), tunnelId);
            return;
        }

        // 创建隧道客户端
        TunnelClient.newBuilder(tunnelId)
                .connectTimeoutMs(option.getHttpConnectTimeoutMs())
                .timeoutMs(option.getHttpTimeoutMs())
                .ends(option.getEnds())
                .buildAsync(token, remote, new TunnelClient.Handler() {
                    @Override
                    public void onConnected() {
                        logger.info("{}/tunnel connected! tunnel={};expire={};", thing.path(), tunnelId, expire);
                    }

                    @Override
                    public void onDisconnected(int code, String reason) {
                        clean();
                        logger.info("{}/tunnel disconnected! tunnel={};code={};reason={};",
                                thing.path(),
                                tunnelId,
                                code,
                                reason
                        );
                    }

                    @Override
                    public void onError(Throwable error) {
                        clean();
                        logger.error("{}/tunnel error! tunnel={};", thing.path(), tunnelId, error);
                    }

                    private void clean() {
                        synchronized (OpBindingForNotify.this) {
                            clientMap.remove(tunnelId);
                        }
                    }

                })
                .whenComplete((client, ex) -> {
                    if (null != ex) {
                        logger.error("{}/tunnel connect failure! tunnel={};", thing.path(), tunnelId, ex);
                    } else {
                        clientMap.put(tunnelId, client);
                    }
                });

    }

    // 断开操作
    private synchronized void opDisconnect(Thing thing, String tunnelId, JsonObject root) {
        final var reason = getAsString(root, "close_reason");
        disconnect(thing, tunnelId, reason);
    }

    private synchronized void disconnect(Thing thing, String tunnelId, String reason) {
        final var client = clientMap.remove(tunnelId);
        if (null != client) {
            client.close(reason).exceptionally(ex -> {
                client.abort();
                logger.warn("{}/tunnel close failure! tunnel={};", thing.path(), tunnelId, ex);
                return null;
            });
        }
    }

    private OpBinder wrap(Thing thing, OpBinder binder) {
        return () -> binder.unbind().thenRun(() ->
                new ArrayList<>(clientMap.keySet())
                        .forEach(tunnelId -> disconnect(thing, tunnelId, "unbind"))
        );
    }

    @Override
    public CompletableFuture<OpBinder> bind(Thing thing) {

        return thing.op()
                .decode(Decoder.decodeBytesToJson(UTF_8))
                .consumer("/sys/%s/secure_tunnel/notify".formatted(thing.path().toURN()), (topic, json) -> {

                    final var root = JsonParser.parseString(json).getAsJsonObject();
                    final var tunnelId = requireAsString(root, "tunnel_id");
                    final var operation = requireAsString(root, "operation");

                    // 连接操作
                    if ("connect".equals(operation)) {
                        opConnect(thing, tunnelId, root);
                    }

                    // 断开操作
                    else if ("close".equals(operation)) {
                        opDisconnect(thing, tunnelId, root);
                    }

                    // 其他操作不做支持
                    else {
                        logger.warn("{}/tunnel unsupported operation: {}! tunnel={};",
                                thing.path(),
                                operation,
                                tunnelId
                        );
                    }

                })
                .thenApply(binder -> wrap(thing, binder));

    }


}
