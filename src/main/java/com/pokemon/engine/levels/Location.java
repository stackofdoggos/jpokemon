package com.pokemon.engine.levels;

import java.util.Collections;
import java.util.List;

import com.pokemon.gui.entities.EntityRender.Direction;

/**
 * A place in the overworld: map layers, default spawn, warps, and signs.
 */
public final class Location {

    private final String id;
    private final String backgroundMapResource;
    private final String foregroundMapResource;
    private final int spawnWorldX;
    private final int spawnWorldY;
    private final Direction spawnFacing;
    private final List<Warp> warps;
    private final List<SignPost> signs;

    public Location(String id, String backgroundMapResource, String foregroundMapResource, int spawnWorldX,
            int spawnWorldY, Direction spawnFacing, List<Warp> warps, List<SignPost> signs) {
        this.id = id;
        this.backgroundMapResource = backgroundMapResource;
        this.foregroundMapResource = foregroundMapResource;
        this.spawnWorldX = spawnWorldX;
        this.spawnWorldY = spawnWorldY;
        this.spawnFacing = spawnFacing;
        this.warps = warps == null ? List.of() : List.copyOf(warps);
        this.signs = signs == null ? List.of() : List.copyOf(signs);
    }

    public String getId() {
        return id;
    }

    /** Classpath path, e.g. {@code art/tilemaps/route101.txt}. */
    public String getBackgroundMapResource() {
        return backgroundMapResource;
    }

    /** Nullable; when null, no foreground layer is drawn. */
    public String getForegroundMapResource() {
        return foregroundMapResource;
    }

    public int getSpawnWorldX() {
        return spawnWorldX;
    }

    public int getSpawnWorldY() {
        return spawnWorldY;
    }

    public Direction getSpawnFacing() {
        return spawnFacing;
    }

    public List<Warp> getWarps() {
        return Collections.unmodifiableList(warps);
    }

    public List<SignPost> getSigns() {
        return Collections.unmodifiableList(signs);
    }
}
