package io.github.athingx.athing.tunnel.thing.test;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.ThingPath;
import io.github.athingx.athing.thing.builder.ThingBuilder;
import io.github.athingx.athing.thing.builder.mqtt.AliyunMqttClientFactory;
import io.github.athingx.athing.tunnel.thing.ThingTunnel;
import io.github.athingx.athing.tunnel.thing.builder.ThingTunnelBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class ThingTunnelSupport implements LoadingProperties {

    protected static volatile Thing thing;
    protected static volatile ThingTunnel thingTunnel;

    @BeforeClass
    public static void _before() throws Exception {

        thing = new ThingBuilder(new ThingPath(PRODUCT_ID, THING_ID))
                .clientFactory(new AliyunMqttClientFactory()
                        .remote(THING_REMOTE)
                        .secret(THING_SECRET))
                .build();

        thingTunnel = new ThingTunnelBuilder()
                .secret(THING_SECRET)
                .provider("ssh://127.0.0.1:22?name=LOCAL_SSH&connectTimeout=30000")
                .build(thing)
                .get();

    }

    @AfterClass
    public static void _after() {
        thing.destroy();
    }

}
