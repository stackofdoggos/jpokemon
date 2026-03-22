package com.pokemon.engine.levels;

/**
 * When the player completes a step on {@code (tileColumn, tileRow)}, load the
 * target location (spawn comes from that {@link Location}).
 */
public record Warp(int tileColumn, int tileRow, String targetLocationId) {
}
