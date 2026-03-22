package com.pokemon.gui;

import com.pokemon.engine.levels.Location;
import com.pokemon.engine.levels.LocationRegistry;
import com.pokemon.engine.levels.Warp;
import com.pokemon.gui.entities.PlayerRender;
import com.pokemon.gui.mapfiles.MapFile;
import com.pokemon.gui.tiles.TileLayer;
import com.pokemon.gui.tiles.effects.GrassOverlayLayer;

/**
 * Active overworld map: tile layers, grass overlay, and warp handling for the
 * current {@link Location}.
 */
public class OverworldScene {

    private final GamePanel gamePanel;
    private Location currentLocation;
    private TileLayer backgroundTileLayer;
    private TileLayer foregroundTileLayer;
    private GrassOverlayLayer grassOverlayLayer;

    public OverworldScene(GamePanel gamePanel) {
        this.gamePanel = gamePanel;
    }

    public void applyLocation(Location loc, PlayerRender player) {
        this.currentLocation = loc;
        MapFile bgMap = new MapFile(loc.getBackgroundMapResource());
        this.backgroundTileLayer = new TileLayer(gamePanel, bgMap);
        if (loc.getForegroundMapResource() != null) {
            this.foregroundTileLayer = new TileLayer(gamePanel, new MapFile(loc.getForegroundMapResource()));
        } else {
            this.foregroundTileLayer = null;
        }
        this.grassOverlayLayer = new GrassOverlayLayer(gamePanel, backgroundTileLayer);
        player.resetForLocation(loc.getSpawnWorldX(), loc.getSpawnWorldY(), loc.getSpawnFacing());
    }

    public void onStepCompleted(PlayerRender player, int tileColumn, int tileRow) {
        for (Warp w : currentLocation.getWarps()) {
            if (w.tileColumn() == tileColumn && w.tileRow() == tileRow) {
                applyLocation(LocationRegistry.get(w.targetLocationId()), player);
                return;
            }
        }
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public TileLayer getBackgroundTileLayer() {
        return backgroundTileLayer;
    }

    public TileLayer getForegroundTileLayer() {
        return foregroundTileLayer;
    }

    public GrassOverlayLayer getGrassOverlayLayer() {
        return grassOverlayLayer;
    }
}
