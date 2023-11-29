package io.github.athingx.athing.tunnel.thing.impl.util;

import java.io.Closeable;

public class IOUtils {

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (java.io.IOException e) {
                // ignore
            }
        }
    }

}
