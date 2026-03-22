package com.pokemon.gui.tiles;

import java.awt.Graphics2D;

import com.pokemon.gui.GamePanel;

/**
 * Base tile class for background/world tiles. Responsible for drawing a
 * texture-sized quad scaled to the current {@link GamePanel#getTileSize()}.
 */
public abstract class BackgroundTile {

    /*
     * Until it becomes obvious it's necessary, I'm not gonna keep track of a tile's
     * position coordinates in here
     */

    private Texture texture;
    private boolean walkable;
    private GamePanel gamePanel;

    public BackgroundTile(GamePanel gamePanel, Texture texture, boolean walkable) {
        this.texture = texture;
        this.walkable = walkable;
        this.gamePanel = gamePanel;
    }

    // default behavior
    public boolean isWalkable() {
        return this.walkable;
    }

    public Texture getTexture() {
        return this.texture;
    }

    // default behavior
    public void draw(Graphics2D g2, int screenXInPixels, int screenYInPixels) {
        g2.drawImage(texture.getImage(), screenXInPixels, screenYInPixels, gamePanel.getTileSize(),
                gamePanel.getTileSize(), null);
    }

    // TILES THAT WILL NEVER DO ANYTHING
    public static class StaticTile extends BackgroundTile {

        public StaticTile(GamePanel gamePanel, Texture texture, boolean walkable) {
            super(gamePanel, texture, walkable);
        }

    }

    // Unifying tile types for readability: interactive/animated behavior will be
    // driven by systems rather than subclasses for now.

}
