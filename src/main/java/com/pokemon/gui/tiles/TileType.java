package com.pokemon.gui.tiles;

/**
 * Semantic classification for tiles used by the map. Each type encodes
 * gameplay-relevant flags to avoid scattering ID lists across the codebase.
 */
public enum TileType {
    GRASS(true, false, "Grass"),
    WILD_GRASS(true, true, "Wild Grass"),
    PATH(true, false, "Path"),
    UNKNOWN(false, false, "Unknown");

    private final boolean walkable;
    private final boolean interactive;
    private final String displayName;

    TileType(boolean walkable, boolean interactive, String displayName) {
        this.walkable = walkable;
        this.interactive = interactive;
        this.displayName = displayName;
    }

    public boolean isWalkable() {
        return walkable;
    }

    public boolean isInteractive() {
        return interactive;
    }

    public String getDisplayName() {
        return displayName;
    }
}
