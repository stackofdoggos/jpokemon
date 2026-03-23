package com.pokemon.gui.text.emerald;

import java.awt.Graphics2D;

/**
 * Draws the field message box frame in native (240×160) logical coordinates before
 * {@link EmeraldTextConstants#SCREEN_SCALE} is applied.
 */
public interface EmeraldDialogueFrameRenderer {

    void drawFrame(Graphics2D g);
}
