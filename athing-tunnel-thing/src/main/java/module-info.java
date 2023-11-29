module athing.tunnel.thing {

    exports io.github.athingx.athing.tunnel.thing;
    opens io.github.athingx.athing.tunnel.thing.impl.client.protocol to marcono1234.gson.recordadapter;

    requires java.net.http;
    requires com.google.gson;
    requires athing.thing.api;
    requires org.slf4j;

}