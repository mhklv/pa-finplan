package org.mchklv.finplan.server;

import java.io.BufferedInputStream;
import java.io.InputStream;

public class ResourceManager {
    public static InputStream getKeyStoreStream() {
        InputStream privKeyStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("keystore.jks");
        return new BufferedInputStream(privKeyStream);
    }
}
