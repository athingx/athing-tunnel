package io.github.athingx.athing.tunnel.thing.impl.core.channel;

import io.github.athingx.athing.tunnel.thing.impl.core.TunnelConfig;
import io.github.athingx.athing.tunnel.thing.impl.core.protocol.TunnelMessage;
import io.github.athingx.athing.tunnel.thing.impl.core.protocol.TunnelMessageType;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.TimeUnit;

/**
 * 隧道Ping处理器
 */
public class TunnelPingHandler extends SimpleChannelInboundHandler<TunnelMessage> {

    private final TunnelConfig config;

    public TunnelPingHandler(TunnelConfig config) {
        this.config = config;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TunnelMessage msg) {
        ctx.fireChannelRead(msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

        // RA握手完成
        if (evt == TunnelHandshakeHandler.TunnelHandshakeStateEvent.TUNNEL_HANDSHAKE_COMPLETE) {
            ctx.channel().eventLoop().schedule(
                    new Runnable() {
                        @Override
                        public void run() {
                            if (ctx.channel().isActive()) {
                                onTerminalPing(ctx);
                                ctx.channel().eventLoop().schedule(this, config.getConnect().getPingIntervalMs(), TimeUnit.MILLISECONDS);
                            }
                        }
                    },
                    config.getConnect().getPingIntervalMs(),
                    TimeUnit.MILLISECONDS
            );
        }

        super.userEventTriggered(ctx, evt);
    }

    /**
     * 发送心跳
     *
     * @param ctx ctx
     */
    private void onTerminalPing(ChannelHandlerContext ctx) {
        ctx.channel().writeAndFlush(
                new TunnelMessage.Builder()
                        .type(TunnelMessageType.MSG_TYPE_PING)
                        .build()
        );
    }

}
