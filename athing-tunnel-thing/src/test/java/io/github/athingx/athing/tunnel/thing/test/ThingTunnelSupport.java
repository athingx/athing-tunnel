package io.github.athingx.athing.tunnel.thing.test;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.client.DefaultMqttClientFactory;
import io.github.athingx.athing.tunnel.thing.TargetEnd;
import io.github.athingx.athing.tunnel.thing.ThingTunnel;
import io.github.athingx.athing.tunnel.thing.ThingTunnelInstaller;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ThingTunnelSupport implements LoadingProperties {

    protected static volatile Thing thing;
    protected static volatile ThingTunnel thingTunnel;

    @BeforeClass
    public static void _before() throws Exception {

        thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .client(new DefaultMqttClientFactory()
                        .remote(THING_REMOTE)
                        .secret(THING_SECRET))
                .build();

        thingTunnel = thing.plugins()
                .install(new ThingTunnelInstaller()
                        .executor(thing.executor())
                        .end(new TargetEnd.SocketEnd("_SSH", "127.0.0.1", 22))
                )
                .get();

    }

    @AfterClass
    public static void _after() {
        thing.destroy();
    }

}
