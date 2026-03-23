package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Approximates {@code WindowFunc_DrawDialogueFrame} footprint: inner text fill plus
 * decorative margin strips so the box aligns with Emerald tile positions.
 */
public final class PlaceholderDialogueFrameRenderer implements EmeraldDialogueFrameRenderer {

    private static final Color OUTER = new Color(0x28, 0x50, 0x38);
    private static final Color INNER_FILL = EmeraldTextConstants.TEXT_BG;
    private static final Color ACCENT = new Color(0x40, 0x78, 0x58);

    /** Outer decoration top (tile row 14). */
    private static final int OUTER_TOP = (EmeraldTextConstants.WINDOW_TILEMAP_TOP - 1) * EmeraldTextConstants.NATIVE_TILE_PX;
    /** Left edge of decoration (tile column 0). */
    private static final int OUTER_LEFT = (EmeraldTextConstants.WINDOW_TILEMAP_LEFT - 2) * EmeraldTextConstants.NATIVE_TILE_PX;
    /** Width spans inner window plus side strips (~31 tiles). */
    private static final int OUTER_W = (EmeraldTextConstants.WINDOW_TILES_W + 4) * EmeraldTextConstants.NATIVE_TILE_PX;
    /** From row 14 through bottom of inner + one row (48 px). */
    private static final int OUTER_H = 6 * EmeraldTextConstants.NATIVE_TILE_PX;

    @Override
    public void drawFrame(Graphics2D g) {
        g.setColor(OUTER);
        g.fillRect(OUTER_LEFT, OUTER_TOP, OUTER_W, OUTER_H);
        g.setColor(ACCENT);
        g.drawRect(OUTER_LEFT, OUTER_TOP, OUTER_W - 1, OUTER_H - 1);
        g.setColor(INNER_FILL);
        g.fillRect(EmeraldTextConstants.INNER_TOPLEFT_SCREEN_X, EmeraldTextConstants.INNER_TOPLEFT_SCREEN_Y,
                EmeraldTextConstants.INNER_WIDTH_PX, EmeraldTextConstants.INNER_HEIGHT_PX);
        g.setColor(ACCENT);
        g.drawRect(EmeraldTextConstants.INNER_TOPLEFT_SCREEN_X, EmeraldTextConstants.INNER_TOPLEFT_SCREEN_Y,
                EmeraldTextConstants.INNER_WIDTH_PX - 1, EmeraldTextConstants.INNER_HEIGHT_PX - 1);
    }
}
