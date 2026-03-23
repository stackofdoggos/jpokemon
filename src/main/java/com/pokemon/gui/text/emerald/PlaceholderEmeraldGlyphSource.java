package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Procedural glyphs: fixed {@link EmeraldTextConstants#FONT_NORMAL_MAX_WIDTH}×
 * {@link EmeraldTextConstants#FONT_NORMAL_MAX_HEIGHT} boxes plus a 1px shadow offset.
 */
public final class PlaceholderEmeraldGlyphSource implements EmeraldGlyphSource {

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
        int w = glyphWidth(charCode);
        int h = glyphHeight(charCode);
        g.setColor(shadow);
        g.fillRect(x + 1, y + 1, w, h);
        g.setColor(fg);
        g.fillRect(x, y, w, h);
    }
}
