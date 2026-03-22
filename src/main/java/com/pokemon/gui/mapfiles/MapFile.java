package com.pokemon.gui.mapfiles;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.pokemon.gui.tiles.Texture;
import com.pokemon.resources.GameResources;

/**
 * Descriptor for a tile map stored as a classpath resource. Parses dimensions
 * from the text grid and preloads textures used by the map.
 */
public class MapFile {

    private static final String EXTERIOR_TILE_CLASSPATH_PREFIX = "art/tiles/exterior/";

    private final String resourcePath;
    private final List<String> mapLines;
    private final int mapFileWidthInTiles;
    private final int mapFileHeightInTiles;
    private final HashMap<String, Texture> preloadedTextures;

    public MapFile(String classpathRelativeMapPath) {
        this.resourcePath = classpathRelativeMapPath;
        try {
            this.mapLines = readMapLines(classpathRelativeMapPath);
            if (mapLines.isEmpty()) {
                throw new IOException("Map has no rows: " + classpathRelativeMapPath);
            }
            this.mapFileWidthInTiles = mapLines.get(0).split(" ").length;
            this.mapFileHeightInTiles = mapLines.size();
            this.preloadedTextures = allocateTextures();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load map: " + classpathRelativeMapPath, e);
        }
    }

    public String getResourcePath() {
        return resourcePath;
    }

    public int getMapFileHeightInTiles() {
        return this.mapFileHeightInTiles;
    }

    public int getMapFileWidthInTiles() {
        return this.mapFileWidthInTiles;
    }

    public HashMap<String, Texture> getTextures() {
        return this.preloadedTextures;
    }

    /** Row text: space-separated tile ids (single line, no trailing newline). */
    public String getMapLine(int rowIndex) {
        return mapLines.get(rowIndex);
    }

    public List<String> getMapLines() {
        return Collections.unmodifiableList(mapLines);
    }

    public Texture getTexture(String id, String classpathResourcePath) throws IOException {
        if (!preloadedTextures.containsKey(id)) {
            preloadedTextures.put(id, Texture.load(id, classpathResourcePath));
        }
        return preloadedTextures.get(id);
    }

    public Texture getTexture(String id) {
        return preloadedTextures.get(id);
    }

    private static List<String> readMapLines(String classpathRelativeMapPath) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                GameResources.requireStream(classpathRelativeMapPath), StandardCharsets.UTF_8))) {
            String first = br.readLine();
            if (first == null || first.isBlank()) {
                throw new IOException("Empty map file: " + classpathRelativeMapPath);
            }
            String firstTrim = first.trim();
            int width = firstTrim.split(" ").length;
            List<String> lines = new ArrayList<>();
            String line = firstTrim;
            while (line != null) {
                if (line.isEmpty()) {
                    break;
                }
                String[] parts = line.split(" ");
                if (parts.length != width) {
                    break;
                }
                lines.add(line);
                String next = br.readLine();
                line = next == null ? null : next.trim();
            }
            return lines;
        }
    }

    private HashMap<String, Texture> allocateTextures() throws IOException {
        HashMap<String, Texture> textures = new HashMap<>();
        for (String row : mapLines) {
            for (String token : row.split(" ")) {
                if (!textures.containsKey(token)) {
                    String path = EXTERIOR_TILE_CLASSPATH_PREFIX + token + ".png";
                    textures.put(token, Texture.load(token, path));
                }
            }
        }
        return textures;
    }
}
