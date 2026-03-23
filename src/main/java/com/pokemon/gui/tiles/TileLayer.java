package com.pokemon.gui.tiles;

import java.awt.Graphics2D;

import com.pokemon.gui.GamePanel;
import com.pokemon.gui.entities.PlayerRender;
import com.pokemon.gui.mapfiles.MapFile;
import com.pokemon.gui.tiles.BackgroundTile.StaticTile;

/**
 * Represents a 2D matrix of background tiles backed by a {@link MapFile}.
 * Provides view-dependent drawing based on the player's position.
 */
public class TileLayer {

    private final GamePanel gamePanel;
    private final BackgroundTile[][] tiles;
    private final MapFile mapFile;

    public TileLayer(GamePanel gamePanel, MapFile mapFile) {
        this.gamePanel = gamePanel;
        this.mapFile = mapFile;
        this.tiles = loadMap(mapFile);
    }

    public void setTile(int row, int column, BackgroundTile tile) {
        tiles[row][column] = tile;
    }

    public BackgroundTile getTile(int row, int column) {
        return tiles[row][column];
    }

    public int getMapFileHeightInTiles() {
        return mapFile.getMapFileHeightInTiles();
    }

    public int getMapFileWidthInTiles() {
        return mapFile.getMapFileWidthInTiles();
    }

    public int[] getCoordinatesOfTile(BackgroundTile tile) {

        // in the format row, column
        // gonna need to standardize between row col and col row at some point
        int[] coordinates = new int[2];

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[0].length; col++) {
                if (tiles[row][col] == tile) {
                    coordinates[0] = row;
                    coordinates[1] = col;
                    return coordinates;
                }
            }
        }

        return null;
    }

    public BackgroundTile getTileAtPixel(int worldX, int worldY) {
        int col = worldX / gamePanel.getTileSize();
        int row = worldY / gamePanel.getTileSize();
        return getTile(row, col);
    }

    public void draw(Graphics2D g2) {

        for (int currentWorldRow = 0; currentWorldRow < mapFile.getMapFileHeightInTiles(); currentWorldRow++) {

            for (int currentWorldColumn = 0; currentWorldColumn < mapFile
                    .getMapFileWidthInTiles(); currentWorldColumn++) {

                BackgroundTile tile = tiles[currentWorldRow][currentWorldColumn];

                /*
                 * The worldX and worldY variables for tiles are where the tiles are in the
                 * world.
                 * The top left tile will always be (0, 0)
                 */

                int worldXInPixels = currentWorldColumn * gamePanel.getTileSize();
                int worldYInPixels = currentWorldRow * gamePanel.getTileSize();

                int screenXInPixels = worldXInPixels - gamePanel.getPlayerRender().getWorldX()
                        + gamePanel.getPlayerRender().getScreenX();

                int screenYInPixels = worldYInPixels - gamePanel.getPlayerRender().getWorldY()
                        + gamePanel.getPlayerRender().getScreenY();

                if (tile != null
                        && tileIsViewableByPlayer(gamePanel.getPlayerRender(), worldXInPixels, worldYInPixels)) {

                    tile.draw(g2, screenXInPixels, screenYInPixels);

                }
            }

        }

    }

    private BackgroundTile[][] loadMap(MapFile mapFile) {
        BackgroundTile[][] tiles = new BackgroundTile[mapFile.getMapFileHeightInTiles()][mapFile
                .getMapFileWidthInTiles()];

        try {
            int currentColumnBeingRead = 0;
            int currentRowBeingRead = 0;

            while (currentRowBeingRead < mapFile.getMapFileHeightInTiles()) {
                String line = mapFile.getMapLine(currentRowBeingRead);
                String numbers[] = line.split(" ");

                while (currentColumnBeingRead < mapFile.getMapFileWidthInTiles()) {

                    String textureID = numbers[currentColumnBeingRead];

                    Texture texture = mapFile.getTexture(textureID);

                    BackgroundTile texturedTile = new StaticTile(gamePanel, texture, isTileWalkable(texture));

                    tiles[currentRowBeingRead][currentColumnBeingRead] = texturedTile;

                    currentColumnBeingRead++;

                }

                currentRowBeingRead++;
                currentColumnBeingRead = 0;

            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return tiles;

    }

    private boolean tileIsViewableByPlayer(PlayerRender player, int worldXInPixels, int worldYInPixels) {

        return (worldXInPixels + 3 * gamePanel.getTileSize() > player.getWorldX() - player.getScreenX()
                && worldXInPixels - 3 * gamePanel.getTileSize() < player.getWorldX() + player.getScreenX()
                && worldYInPixels + 3 * gamePanel.getTileSize() > player.getWorldY() - player.getScreenY()
                && worldYInPixels - 3 * gamePanel.getTileSize() < player.getWorldY() + player.getScreenY());
    }

    private boolean isTileWalkable(Texture texture) {
        return TileUtils.getTypeForId(texture.getID()).isWalkable();
    }
}
