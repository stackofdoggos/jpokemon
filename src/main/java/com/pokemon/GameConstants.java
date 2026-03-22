package com.pokemon;

/**
 * Values that must stay aligned across overworld spawn math and rendering.
 */
public final class GameConstants {

    public static final int ORIGINAL_TILE_PX = 16;
    public static final int TILE_SCALE = 3;
    public static final int SCALED_TILE_PIXELS = ORIGINAL_TILE_PX * TILE_SCALE;

    private GameConstants() {
    }
}
