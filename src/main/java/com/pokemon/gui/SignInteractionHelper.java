package com.pokemon.gui;

import com.pokemon.engine.levels.Location;
import com.pokemon.engine.levels.SignPost;
import com.pokemon.gui.entities.EntityRender.Direction;
import com.pokemon.gui.entities.PlayerRender;
import com.pokemon.gui.tiles.BackgroundTile;
import com.pokemon.gui.tiles.TileLayer;

/**
 * Resolves sign messages when the player faces a registered sign tile.
 */
public final class SignInteractionHelper {

    private SignInteractionHelper() {
    }

    /**
     * @return message to show, or null if no sign matches the tile in front of the player
     */
    public static String findSignMessage(Location location, PlayerRender player, GamePanel gamePanel,
            TileLayer background) {
        if (location == null || location.getSigns().isEmpty() || background == null) {
            return null;
        }

        int[] front = tileInFrontOfPlayer(player, gamePanel);
        int frontCol = front[0];
        int frontRow = front[1];

        int rows = background.getMapFileHeightInTiles();
        int cols = background.getMapFileWidthInTiles();
        if (frontRow < 0 || frontRow >= rows || frontCol < 0 || frontCol >= cols) {
            return null;
        }

        BackgroundTile tile = background.getTile(frontRow, frontCol);
        if (tile == null) {
            return null;
        }
        String textureId = tile.getTexture().getID();

        for (SignPost sign : location.getSigns()) {
            if (sign.matchesTargetTile(frontCol, frontRow, textureId)) {
                return sign.message();
            }
        }
        return null;
    }

    /**
     * @return [column, row] of the tile adjacent to the player in the facing direction,
     *         using bounding-box center for the player's tile anchor (same as step/warp logic).
     */
    public static int[] tileInFrontOfPlayer(PlayerRender player, GamePanel gamePanel) {
        int ts = gamePanel.getTileSize();
        int cx = player.getBoundingBoxX() + player.getBoundingBoxWidth() / 2;
        int cy = player.getBoundingBoxY() + player.getBoundingBoxHeight() / 2;
        int playerCol = cx / ts;
        int playerRow = cy / ts;

        Direction d = player.getDirection();
        int frontCol = playerCol;
        int frontRow = playerRow;
        if (d.isFacingUp()) {
            frontRow--;
        } else if (d.isFacingDown()) {
            frontRow++;
        } else if (d.isFacingLeft()) {
            frontCol--;
        } else if (d.isFacingRight()) {
            frontCol++;
        }
        return new int[] { frontCol, frontRow };
    }
}
