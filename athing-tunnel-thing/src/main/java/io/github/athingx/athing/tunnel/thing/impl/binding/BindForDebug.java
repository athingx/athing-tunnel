package io.github.athingx.athing.tunnel.thing.impl.binding;

import io.github.athingx.athing.thing.api.Thing;
import io.github.athingx.athing.thing.api.op.OpBind;
import io.github.athingx.athing.thing.api.op.OpGroupBindFor;
import io.github.athingx.athing.thing.api.op.OpGroupBinding;
import io.github.athingx.athing.tunnel.thing.impl.core.Tunnel;
import io.github.athingx.athing.tunnel.thing.impl.domain.Debug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;

import static io.github.athingx.athing.thing.api.function.ThingFn.mappingJsonFromByte;
import static io.github.athingx.athing.thing.api.function.ThingFn.mappingJsonToType;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BindForDebug implements OpGroupBindFor<OpBind> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Thing thing;
    private final Tunnel tunnel;

    public BindForDebug(Thing thing, Tunnel tunnel) {
        this.thing = thing;
        this.tunnel = tunnel;
    }

    @Override
    public CompletableFuture<OpBind> bindFor(OpGroupBinding group) {
        return group.binding("/sys/%s/edge/debug/switch".formatted(thing.path().toURN()))
                .map(mappingJsonFromByte(UTF_8))
                .map(mappingJsonToType(Debug.class))
                .bind((topic, debug) -> {
                    if (debug.isEnable()) {
                        tunnel.connect();
                        logger.debug("{}/tunnel switch -> enable!", thing.path());
                    } else {
                        tunnel.disconnect();
                        logger.debug("{}/tunnel switch -> disable!", thing.path());
                    }
                });
    }

}
