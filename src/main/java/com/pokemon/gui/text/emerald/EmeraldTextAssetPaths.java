package com.pokemon.gui.text.emerald;

/**
 * Classpath locations for Emerald UI art under {@code src/main/resources}. Load
 * with {@link com.pokemon.resources.GameResources#openStream(String)}. If a
 * file is missing, the UI falls back to procedural placeholders.
 */
public final class EmeraldTextAssetPaths {

    private EmeraldTextAssetPaths() {
    }

    /**
     * Optional full dialogue frame (message box + decorative strips) as one PNG.
     * <p>
     * Recommended: export a composite at <b>native GBA width</b> (240 px tall
     * strip or full 240×160 crop) showing the box where the inner text area is
     * exactly {@value EmeraldTextConstants#INNER_WIDTH_PX}×{@value EmeraldTextConstants#INNER_HEIGHT_PX}
     * pixels with its <b>top-left</b> at logical screen coordinates
     * ({@value EmeraldTextConstants#INNER_TOPLEFT_SCREEN_X},
     * {@value EmeraldTextConstants#INNER_TOPLEFT_SCREEN_Y}) relative to the
     * 240×160 field. The renderer scales by {@link EmeraldTextConstants#SCREEN_SCALE}
     * with nearest-neighbor. Alternatively supply an image already scaled
     * (720×480) with the same inner rect scaled accordingly.
     */
    public static final String DIALOGUE_FRAME_PNG = "art/ui/emerald/dialogue_frame.png";

    /**
     * Optional {@code FONT_NORMAL} spritesheet. Expected layout: a uniform grid of
     * <b>8×16</b> px cells (native), <b>16 columns × 16 rows</b> = 256 cells.
     * Glyph index = {@code byteValue & 0xFF} (Emerald encoding). Width for layout
     * uses {@link EmeraldTextConstants#FONT_NORMAL_MAX_WIDTH} unless you later
     * add per-glyph metrics. Missing file → black box glyphs.
     */
    public static final String FONT_NORMAL_PNG = "art/ui/emerald/font_normal.png";

    /**
     * Optional continue arrow (e.g. from {@code graphics/fonts/down_arrow.4bpp}).
     * Drawn at native size scaled by {@link EmeraldTextConstants#SCREEN_SCALE};
     * if absent, a small blinking placeholder rectangle is used.
     */
    public static final String DOWN_ARROW_PNG = "art/ui/emerald/down_arrow.png";
}
