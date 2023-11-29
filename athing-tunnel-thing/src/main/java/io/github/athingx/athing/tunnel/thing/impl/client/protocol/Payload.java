package io.github.athingx.athing.tunnel.thing.impl.client.protocol;

import java.nio.ByteBuffer;

public record Payload(ByteBuffer buffer) implements Frame.Data {

    @Override
    public ByteBuffer toByteBuffer() {
        return buffer;
    }

}
