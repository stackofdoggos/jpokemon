package com.pokemon.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JPanel;

import com.pokemon.GameConstants;
import com.pokemon.engine.levels.LocationRegistry;
import com.pokemon.gui.entities.CollisionChecker;
import com.pokemon.gui.entities.PlayerRender;
import com.pokemon.gui.tiles.TileLayer;
import com.pokemon.gui.tiles.effects.GrassOverlayLayer;

/**
 * Core Swing panel responsible for running the game loop and delegating
 * updates/renders to tile layers, entities, and overlay effects.
 */
public class GamePanel extends JPanel implements Runnable {

    /** GBA 16px tile scaled 3x; see {@link GameConstants}. */
    private final int tileSize = GameConstants.SCALED_TILE_PIXELS;

    /*
     * Gameboy Advance had a 3:2 ratio,
     * displaying 15 tiles wide and 10 tiles high.
     * Ran at 60 FPS
     */
    private final int screenWidthInTiles = 15;
    private final int screenHeightInTiles = 10;
    private final int screenWidthInPixels = tileSize * screenWidthInTiles; // 720
    private final int screenHeightInPixels = tileSize * screenHeightInTiles; // 480
    private final int FPS = 60;

    private final Dimension SCREEN_SIZE = new Dimension(screenWidthInPixels, screenHeightInPixels);
    private final Color BACKGROUND_COLOR = Color.BLACK;

    private final CollisionChecker collisionChecker = new CollisionChecker(this);
    private final KeyHandler keyHandler = new KeyHandler();
    private Thread gameThread; // used for ticking
    private final PlayerRender player;
    private final OverworldScene overworldScene;
    private final MessageOverlay messageOverlay = new MessageOverlay();

    public GamePanel() {
        this.player = new PlayerRender(this, keyHandler);
        this.overworldScene = new OverworldScene(this);

        this.setPreferredSize(SCREEN_SIZE);
        this.setBackground(BACKGROUND_COLOR);
        this.setDoubleBuffered(true); // allows for offscreen rendering for smooth performance

        this.addKeyListener(keyHandler);
        this.setFocusable(true); // focuses frame

        this.overworldScene.applyLocation(LocationRegistry.getStartingLocation(), this.player);
        this.player.setTileStepListener((col, row) -> this.overworldScene.onStepCompleted(this.player, col, row));
    }

    /** Starts the game loop on a dedicated thread. */
    public void startGameThread() {
        this.gameThread = new Thread(this);
        gameThread.start();
    }

    /** Size of one world tile in pixels. */
    public int getTileSize() {
        return this.tileSize;
    }

    public int getScreenWidthInTiles() {
        return this.screenWidthInTiles;
    }

    public int getScreenHeightInTiles() {
        return this.screenHeightInTiles;
    }

    public int getCenterOfScreenWidthInPixels() {
        return this.screenWidthInPixels / 2;
    }

    public int getCenterOfScreenHeightInPixels() {
        return this.screenHeightInPixels / 2 - 24;
    }

    public PlayerRender getPlayerRender() {
        return this.player;
    }

    public CollisionChecker getCollisionChecker() {
        return this.collisionChecker;
    }

    public TileLayer getBackgroundTileLayer() {
        return this.overworldScene.getBackgroundTileLayer();
    }

    public TileLayer getForegroundTileLayer() {
        return this.overworldScene.getForegroundTileLayer();
    }

    public OverworldScene getOverworldScene() {
        return overworldScene;
    }

    @Override
    public void run() {

        double drawInterval = 1000000000 / FPS; // 0.0166s
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime; // initialized within loop
        long timer = 0;

        while (gameThread != null) {

            currentTime = System.nanoTime();

            delta += (currentTime - lastTime) / drawInterval;
            timer += currentTime - lastTime;
            lastTime = currentTime;

            if (delta >= 1) {
                update();
                repaint();
                delta--;
            }

            if (timer >= 1000000000) {
                timer = 0;
            }
        }
    }

    /** Update all game actors and systems for the current tick. */
    public void update() {
        if (messageOverlay.isBlocking()) {
            if (keyHandler.consumeMessageDismissPress()) {
                messageOverlay.dismiss();
            }
            return;
        }

        player.update();
        GrassOverlayLayer grass = overworldScene.getGrassOverlayLayer();
        if (grass != null) {
            grass.update(player);
        }

        if (keyHandler.consumeSignInteractPress() && !player.isMoving()) {
            String msg = SignInteractionHelper.findSignMessage(overworldScene.getCurrentLocation(), player, this,
                    overworldScene.getBackgroundTileLayer());
            if (msg != null) {
                messageOverlay.show(msg);
                keyHandler.clearPendingMenuActions();
            }
        }
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;

        overworldScene.getBackgroundTileLayer().draw(g2);

        GrassOverlayLayer grass = overworldScene.getGrassOverlayLayer();
        if (grass != null) {
            grass.drawUnder(g2);
        }

        player.draw(g2);

        if (grass != null) {
            grass.drawOver(g2);
        }

        TileLayer foreground = overworldScene.getForegroundTileLayer();
        if (foreground != null) {
            foreground.draw(g2);
        }

        messageOverlay.draw(g2, screenWidthInPixels, screenHeightInPixels);

        g2.dispose();
    }
}
