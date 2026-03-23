package com.pokemon.engine.levels;

/**
 * A sign readable when the player faces a tile with the given texture id.
 * Optional grid coordinates disambiguate multiple tiles sharing the same id.
 */
public record SignPost(String textureId, String message, Integer signTileColumn, Integer signTileRow) {

    public static SignPost atAnyPosition(String textureId, String message) {
        return new SignPost(textureId, message, null, null);
    }

    public boolean matchesTargetTile(int frontTileColumn, int frontTileRow, String frontTextureId) {
        if (frontTextureId == null || !textureId.equals(frontTextureId)) {
            return false;
        }
        if (signTileColumn != null && signTileRow != null) {
            return signTileColumn == frontTileColumn && signTileRow == frontTileRow;
        }
        return true;
    }
}
