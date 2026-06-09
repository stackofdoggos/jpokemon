package com.pokemon.gui.text.emerald;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.pokemon.resources.GameResources;

/**
 * Draws {@link EmeraldTextAssetPaths#DIALOGUE_FRAME_PNG} (a 240-wide strip
 * generated from pokeemerald's {@code WindowFunc_DrawDialogueFrame} tiling) at
 * native coordinates (0, {@link EmeraldTextConstants#FRAME_TOP_Y}). If the
 * image is missing, delegates to {@link PlaceholderDialogueFrameRenderer}.
 */
public final class ImageDialogueFrameRenderer implements EmeraldDialogueFrameRenderer {

    private final BufferedImage image;
    private final PlaceholderDialogueFrameRenderer fallback = new PlaceholderDialogueFrameRenderer();

    public ImageDialogueFrameRenderer() {
        this(EmeraldTextAssetPaths.DIALOGUE_FRAME_PNG);
    }

    public ImageDialogueFrameRenderer(String classpathRelativePath) {
        BufferedImage loaded = null;
        try (InputStream in = GameResources.openStream(classpathRelativePath)) {
            if (in != null) {
                loaded = ImageIO.read(in);
            }
        } catch (Exception ignored) {
            loaded = null;
        }
        this.image = loaded;
    }

    @Override
    public void drawFrame(Graphics2D g) {
        if (image == null) {
            fallback.drawFrame(g);
        } else {
            g.drawImage(image, 0, EmeraldTextConstants.FRAME_TOP_Y, null);
        }
    }
}
