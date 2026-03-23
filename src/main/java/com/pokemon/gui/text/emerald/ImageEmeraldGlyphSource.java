package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.pokemon.resources.GameResources;

/**
 * Loads {@link EmeraldTextAssetPaths#FONT_NORMAL_PNG}: 16×16 grid of 8×16 native
 * cells, indexed by {@code charCode & 0xFF}. Layout width remains
 * {@link EmeraldTextConstants#FONT_NORMAL_MAX_WIDTH} until per-glyph widths are
 * added. If the resource is missing, delegates drawing to
 * {@link PlaceholderEmeraldGlyphSource}.
 */
public final class ImageEmeraldGlyphSource implements EmeraldGlyphSource {

    private static final int CELL_W = 8;
    private static final int CELL_H = 16;
    private static final int COLS = 16;
    private static final int ROWS = 16;

    private final BufferedImage sheet;
    private final PlaceholderEmeraldGlyphSource fallback = new PlaceholderEmeraldGlyphSource();

    public ImageEmeraldGlyphSource() {
        this(EmeraldTextAssetPaths.FONT_NORMAL_PNG);
    }

    public ImageEmeraldGlyphSource(String classpathRelativePath) {
        BufferedImage loaded = null;
        try (InputStream in = GameResources.openStream(classpathRelativePath)) {
            if (in != null) {
                loaded = ImageIO.read(in);
            }
        } catch (Exception ignored) {
            loaded = null;
        }
        this.sheet = loaded;
    }

    public boolean hasSheet() {
        return sheet != null;
    }

    @Override
    public int glyphWidth(byte charCode) {
        return EmeraldTextConstants.FONT_NORMAL_MAX_WIDTH;
    }

    @Override
    public int glyphHeight(byte charCode) {
        return EmeraldTextConstants.FONT_NORMAL_MAX_HEIGHT;
    }

    @Override
    public void drawGlyph(Graphics2D g, byte charCode, int x, int y, Color fg, Color bg, Color shadow) {
        if (sheet == null) {
            fallback.drawGlyph(g, charCode, x, y, fg, bg, shadow);
            return;
        }
        int idx = charCode & 0xff;
        int col = idx % COLS;
        int row = idx / COLS;
        int sx = col * CELL_W;
        int sy = row * CELL_H;
        if (sx + CELL_W > sheet.getWidth() || sy + CELL_H > sheet.getHeight()) {
            fallback.drawGlyph(g, charCode, x, y, fg, bg, shadow);
            return;
        }
        BufferedImage sub = sheet.getSubimage(sx, sy, CELL_W, CELL_H);
        g.drawImage(sub, x, y, glyphWidth(charCode), glyphHeight(charCode), null);
    }
}
