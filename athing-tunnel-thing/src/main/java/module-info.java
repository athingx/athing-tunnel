module athing.tunnel.thing {

    exports io.github.athingx.athing.tunnel.thing;
    exports io.github.athingx.athing.tunnel.thing.builder;

    opens io.github.athingx.athing.tunnel.thing.impl.domain to com.google.gson, athing.common;
    opens io.github.athingx.athing.tunnel.thing.impl.core.protocol to com.google.gson;

    requires athing.thing.api;
    requires org.slf4j;
    requires io.netty.transport;
    requires io.netty.codec.http;
    requires io.netty.handler;
    requires io.netty.codec;
    requires io.netty.buffer;
    requires io.netty.common;
    requires com.google.gson;

}