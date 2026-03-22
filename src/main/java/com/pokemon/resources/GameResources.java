package com.pokemon.resources;

import java.io.IOException;
import java.io.InputStream;

/**
 * Opens classpath resources under {@code src/main/resources} (JAR root). Paths
 * are {@code art/...} without a leading slash in the argument.
 */
public final class GameResources {

    private GameResources() {
    }

    /**
     * @param classpathRelativePath e.g. {@code art/tilemaps/route101.txt}
     */
    public static InputStream openStream(String classpathRelativePath) {
        String path = normalize(classpathRelativePath);
        return GameResources.class.getResourceAsStream(path);
    }

    public static InputStream requireStream(String classpathRelativePath) throws IOException {
        InputStream in = openStream(classpathRelativePath);
        if (in == null) {
            throw new IOException("Resource not found: " + classpathRelativePath);
        }
        return in;
    }

    private static String normalize(String classpathRelativePath) {
        if (classpathRelativePath == null || classpathRelativePath.isEmpty()) {
            return "/";
        }
        return classpathRelativePath.startsWith("/") ? classpathRelativePath : "/" + classpathRelativePath;
    }
}
