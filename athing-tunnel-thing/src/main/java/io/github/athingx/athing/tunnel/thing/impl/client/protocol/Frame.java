package io.github.athingx.athing.tunnel.thing.impl.client.protocol;

import java.nio.ByteBuffer;

public record Frame<T extends Frame.Data>(Header header, T data) {

    public interface Data {

        ByteBuffer toByteBuffer();

    }

}
