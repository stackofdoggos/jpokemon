package com.pokemon.gui.tiles;

import com.pokemon.gui.GamePanel;
import com.pokemon.gui.entities.PlayerRender;

/**
 * Utility constants and helpers for tile behavior and debugging.
 */
public class TileUtils {
        public static final int VIEWPORT_BUFFER_SIZE_IN_TILES = 2;

        // Mapping from texture ID to tile type. Keep centralized and readable.
        // Extend by adding IDs to the appropriate arrays below.
        private static final String[] IDS_GRASS = { "0088", "0585", "0673" };
        private static final String[] IDS_WILD_GRASS = { "0441", "0584", "0672" };
        private static final String[] IDS_PATH = { "0058", "0146", "0234", "0059", "0147", "0235", "0060",
                        "0148", "0236" };

        // PNG transition sequence timing (n frames per stage)
        public static final int GRASS_TRANSITION_FRAMES_PER_IMAGE = 6;
        // How many frames to hold the first stage (entering grass) before advancing
        // == GRASS_TRANSITION_FRAMES_PER_IMAGE * 1.5
        public static final int GRASS_TRANSITION_FRAMES_FIRST_STAGE = 8;

        // Fraction of the player's bounding box that must overlap the new tile along
        // the movement axis to consider the player "in" that tile (e.g., 0.10 = 10%).
        public static final double GRASS_ENTER_LEADING_EDGE_FRACTION = 0.10;

        // Fraction of overlap into the current tile before we allow grass to render
        // over the player when moving into that tile (e.g., 0.90 = 90%).
        public static final double GRASS_EMERGE_LEADING_EDGE_FRACTION = 0.90;

        /** Returns the semantic {@link TileType} for a given texture ID. */
        public static TileType getTypeForId(String textureId) {
                if (textureId == null)
                        return TileType.UNKNOWN;
                if (arrayContains(IDS_WILD_GRASS, textureId))
                        return TileType.WILD_GRASS;
                if (arrayContains(IDS_GRASS, textureId))
                        return TileType.GRASS;
                if (arrayContains(IDS_PATH, textureId))
                        return TileType.PATH;
                return TileType.UNKNOWN;
        }

        private static boolean arrayContains(String[] ids, String target) {
                for (String id : ids) {
                        if (id.equals(target))
                                return true;
                }
                return false;
        }

        /*
         * Not exactly sure why I need a +1 but whatever
         */
        public static void printCurrentTileForDebugging(PlayerRender player, GamePanel gamePanel) {
                System.out.println("Player is on: (x, y) = (" + (player.getBoundingBoxX() / gamePanel.getTileSize() + 1)
                                + ", "
                                + player.getBoundingBoxY() / gamePanel.getTileSize() + ")");
        }
}
