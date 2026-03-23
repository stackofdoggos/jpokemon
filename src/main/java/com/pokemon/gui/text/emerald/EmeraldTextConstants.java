package com.pokemon.gui.text.emerald;

import java.awt.Color;

import com.pokemon.GameConstants;

/**
 * Layout and font metrics aligned with pret/pokeemerald field message box and
 * {@code FONT_NORMAL} ({@code src/menu.c}, {@code src/text.c}).
 */
public final class EmeraldTextConstants {

    /** GBA BG tile size in native pixels. */
    public static final int NATIVE_TILE_PX = 8;

    /** Standard field message window width/height in tiles ({@code sStandardTextBox_WindowTemplates}). */
    public static final int WINDOW_TILES_W = 27;
    public static final int WINDOW_TILES_H = 4;

    /** Window template tilemap origin (tiles). */
    public static final int WINDOW_TILEMAP_LEFT = 2;
    public static final int WINDOW_TILEMAP_TOP = 15;

    /** Inner drawable area in native pixels (window pixel buffer). */
    public static final int INNER_WIDTH_PX = WINDOW_TILES_W * NATIVE_TILE_PX;
    public static final int INNER_HEIGHT_PX = WINDOW_TILES_H * NATIVE_TILE_PX;

    /** Inner text surface top-left on a 240×160 logical field. */
    public static final int INNER_TOPLEFT_SCREEN_X = WINDOW_TILEMAP_LEFT * NATIVE_TILE_PX;
    public static final int INNER_TOPLEFT_SCREEN_Y = WINDOW_TILEMAP_TOP * NATIVE_TILE_PX;

    /** Match {@link GameConstants#TILE_SCALE} (240→720). */
    public static final int SCREEN_SCALE = GameConstants.TILE_SCALE;

    /** {@code FONT_NORMAL} from {@code sFontInfos}. */
    public static final int FONT_NORMAL_MAX_WIDTH = 6;
    public static final int FONT_NORMAL_MAX_HEIGHT = 16;
    public static final int FONT_NORMAL_LETTER_SPACING = 0;
    public static final int FONT_NORMAL_LINE_SPACING = 0;

    /** Text printer initial cursor ({@code AddTextPrinterParameterized2}). */
    public static final int TEXT_ORIGIN_X = 0;
    public static final int TEXT_ORIGIN_Y = 1;

    /** Field message default colors ({@code AddTextPrinterForMessage}). */
    public static final Color TEXT_FG = new Color(0x30, 0x50, 0x50);
    public static final Color TEXT_BG = new Color(0xe8, 0xf8, 0xe8);
    public static final Color TEXT_SHADOW = new Color(0x70, 0x90, 0x90);

    /** Mirrors {@code OPTIONS_TEXT_SPEED_*} + {@code sTextSpeedFrameDelays} (frames between glyphs). */
    public enum TextSpeed {
        SLOW(8),
        MID(4),
        FAST(1);

        private final int frameDelay;

        TextSpeed(int frameDelay) {
            this.frameDelay = frameDelay;
        }

        public int frameDelay() {
            return frameDelay;
        }
    }

    private EmeraldTextConstants() {
    }
}
