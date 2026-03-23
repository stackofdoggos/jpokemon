package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * {@code FONT_NORMAL} glyph metrics and drawing in native (logical GBA) pixels.
 */
public interface EmeraldGlyphSource {

    int glyphWidth(byte charCode);

    int glyphHeight(byte charCode);

    void drawGlyph(Graphics2D g, byte charCode, int x, int y, Color fg, Color bg, Color shadow);
}
