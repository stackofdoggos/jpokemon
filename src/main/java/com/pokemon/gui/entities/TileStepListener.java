package com.pokemon.gui.entities;

/**
 * Called when the player finishes a successful tile step (movement was not
 * blocked by collision). Coordinates match {@link com.pokemon.gui.tiles.TileLayer#getTile(int, int)} as {@code getTile(tileRow, tileColumn)}.
 */
@FunctionalInterface
public interface TileStepListener {

    void onStepCompleted(int tileColumn, int tileRow);
}
