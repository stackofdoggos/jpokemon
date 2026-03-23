package com.pokemon.engine.levels;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.pokemon.GameConstants;
import com.pokemon.gui.entities.EntityRender.Direction;

/**
 * Static registry of overworld locations. Includes a paired-warp demo on Route
 * 101 using the same tile data twice with different spawns.
 */
public final class LocationRegistry {

    public static final String ROUTE_101_MAIN = "route101_main";
    public static final String ROUTE_101_ALT = "route101_alt";

    private static final Map<String, Location> BY_ID = new HashMap<>();

    static {
        int ts = GameConstants.SCALED_TILE_PIXELS;
        List<SignPost> route101Signs = List.of(SignPost.atAnyPosition("0264",
                "ROUTE 101\nIf you follow the path, you will reach OLDALE TOWN."));

        Location alt = new Location(
                ROUTE_101_ALT,
                "art/tilemaps/route101.txt",
                "art/tilemaps/route101foreground.txt",
                25 * ts,
                10 * ts,
                Direction.DOWN,
                List.of(new Warp(25, 9, ROUTE_101_MAIN)),
                route101Signs);

        Location main = new Location(
                ROUTE_101_MAIN,
                "art/tilemaps/route101.txt",
                "art/tilemaps/route101foreground.txt",
                20 * ts,
                10 * ts,
                Direction.DOWN,
                List.of(new Warp(16, 11, ROUTE_101_ALT)),
                route101Signs);

        BY_ID.put(main.getId(), main);
        BY_ID.put(alt.getId(), alt);
    }

    private LocationRegistry() {
    }

    public static Location get(String id) {
        Location loc = BY_ID.get(id);
        if (loc == null) {
            throw new IllegalArgumentException("Unknown location: " + id);
        }
        return loc;
    }

    public static Location getStartingLocation() {
        return get(ROUTE_101_MAIN);
    }
}
