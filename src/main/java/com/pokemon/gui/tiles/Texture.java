package com.pokemon.gui.tiles;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.pokemon.resources.GameResources;

/**
 * Immutable wrapper around a loaded tile texture. The tile id is the map token
 * (e.g. four-digit Emerald tile id), not derived from the file path.
 */
public class Texture {

    private final BufferedImage image;
    private final String tileId;

    public Texture(String tileId, BufferedImage image) {
        this.tileId = tileId;
        this.image = image;
    }

    /**
     * Loads a PNG from the classpath, e.g. {@code art/tiles/exterior/0410.png}.
     */
    public static Texture load(String tileId, String classpathRelativePath) throws IOException {
        try (InputStream in = GameResources.requireStream(classpathRelativePath)) {
            BufferedImage img = ImageIO.read(in);
            if (img == null) {
                throw new IOException("Could not decode image: " + classpathRelativePath);
            }
            return new Texture(tileId, img);
        }
    }

    public BufferedImage getImage() {
        return this.image;
    }

    public String getID() {
        return this.tileId;
    }
}
