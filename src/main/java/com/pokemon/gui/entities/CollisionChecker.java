package com.pokemon.gui.entities;

import com.pokemon.gui.GamePanel;
import com.pokemon.gui.tiles.BackgroundTile;

/**
 * Computes axis-aligned tile collisions for entities based on their bounding
 * box and intended movement direction.
 */
public class CollisionChecker {

    private final GamePanel gamePanel;

    public CollisionChecker(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void checkTile(EntityRender entity) {

        /*
         * major refactoring could prob be done once Brendan is restricted to grid
         * movement
         */

        int entityLeftBoundingBoxWorldX = entity.worldX + entity.boundingBox.x;
        int entityRightBoundingBoxWorldX = entity.worldX + entity.boundingBox.x + entity.boundingBox.width;

        int entityTopBoundingBoxWorldY = entity.worldY + entity.boundingBox.y;
        int entityBottomBoundingBoxWorldY = entity.worldY + entity.boundingBox.y + entity.boundingBox.height;

        int entityLeftColumn = entityLeftBoundingBoxWorldX / gamePanel.getTileSize();
        int entityRightColumn = entityRightBoundingBoxWorldX / gamePanel.getTileSize();
        int entityTopRow = entityTopBoundingBoxWorldY / gamePanel.getTileSize();
        int entityBottomRow = entityBottomBoundingBoxWorldY / gamePanel.getTileSize();

        /*
         * Don't think about the "right" and "left" too hard. What this means is that
         * there are two possible tile collisions for any given direction with my 16x16
         * (48x48) bounding box
         */

        BackgroundTile possibleRightCollision;
        BackgroundTile possibleLeftCollision;

        /*
         * the first line of each if, else if statement predicts where the character
         * will be and accesses the tile at that location to see if it is traversable.
         * If it is not, then the isColliding boolean is switched on and the character
         * won't move
         */

        if (entity.getDirection().isFacingUp()) {
            entityTopRow = (entityTopBoundingBoxWorldY - entity.speed) / gamePanel.getTileSize();

            // this is where im initializing the tiles
            possibleRightCollision = gamePanel.getBackgroundTileLayer().getTile(entityTopRow, entityRightColumn);

            // this is where im initializing the tiles
            possibleLeftCollision = gamePanel.getBackgroundTileLayer().getTile(entityTopRow, entityLeftColumn);

            if (!possibleRightCollision.isWalkable() || !possibleLeftCollision.isWalkable()) {
                entity.isColliding = true;
            }
        }

        else if (entity.getDirection().isFacingDown()) {
            entityBottomRow = (entityBottomBoundingBoxWorldY + entity.speed) / gamePanel.getTileSize();

            possibleRightCollision = gamePanel.getBackgroundTileLayer().getTile(entityBottomRow, entityRightColumn);

            possibleLeftCollision = gamePanel.getBackgroundTileLayer().getTile(entityBottomRow, entityLeftColumn);

            if (!possibleRightCollision.isWalkable() || !possibleLeftCollision.isWalkable()) {
                entity.isColliding = true;
            }
        }

        else if (entity.getDirection().isFacingRight()) {
            entityRightColumn = (entityRightBoundingBoxWorldX + entity.speed) / gamePanel.getTileSize();

            possibleRightCollision = gamePanel.getBackgroundTileLayer().getTile(entityTopRow, entityRightColumn);

            possibleLeftCollision = gamePanel.getBackgroundTileLayer().getTile(entityBottomRow, entityRightColumn);

            if (!possibleRightCollision.isWalkable() || !possibleLeftCollision.isWalkable()) {
                entity.isColliding = true;
            }
        }

        else if (entity.getDirection().isFacingLeft()) {
            entityLeftColumn = (entityLeftBoundingBoxWorldX - entity.speed) / gamePanel.getTileSize();

            possibleRightCollision = gamePanel.getBackgroundTileLayer().getTile(entityTopRow, entityLeftColumn);

            possibleLeftCollision = gamePanel.getBackgroundTileLayer().getTile(entityBottomRow, entityLeftColumn);

            // DEBUG:
            // System.out.println(Arrays.toString(gamePanel.getTileLayer().getCoordinatesOfTile(possibleRightCollision)));
            // System.out.println(Arrays.toString(gamePanel.getTileLayer().getCoordinatesOfTile(possibleLeftCollision)));

            if (!possibleRightCollision.isWalkable() || !possibleLeftCollision.isWalkable()) {
                entity.isColliding = true;
            }
        }
    }
}
