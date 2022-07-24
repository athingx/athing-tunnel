package io.github.athingx.athing.tunnel.thing;

public interface ThingTunnel extends AutoCloseable {

    void enable();

    void disable();

    boolean isEnable();

}
