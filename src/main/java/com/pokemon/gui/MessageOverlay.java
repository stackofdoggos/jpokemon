package com.pokemon.gui;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;

import com.pokemon.gui.text.emerald.EmeraldDialogueFrameRenderer;
import com.pokemon.gui.text.emerald.EmeraldCharCodec;
import com.pokemon.gui.text.emerald.EmeraldGlyphSource;
import com.pokemon.gui.text.emerald.EmeraldTextConstants;
import com.pokemon.gui.text.emerald.EmeraldTextPrinter;
import com.pokemon.gui.text.emerald.ImageDialogueFrameRenderer;
import com.pokemon.gui.text.emerald.ImageEmeraldGlyphSource;

/**
 * Field message box using pret/pokeemerald layout ({@code FONT_NORMAL}, standard
 * window template) and asset hooks under {@link com.pokemon.gui.text.emerald.EmeraldTextAssetPaths}.
 */
public class MessageOverlay {

    private final EmeraldGlyphSource glyphSource;
    private final EmeraldDialogueFrameRenderer frameRenderer;
    private final EmeraldTextPrinter printer;

    private boolean visible;

    public MessageOverlay() {
        this.glyphSource = new ImageEmeraldGlyphSource();
        this.frameRenderer = new ImageDialogueFrameRenderer();
        this.printer = new EmeraldTextPrinter(EmeraldCharCodec.encode(""), glyphSource,
                EmeraldTextConstants.TextSpeed.MID);
    }

    public void setTextSpeed(EmeraldTextConstants.TextSpeed speed) {
        printer.setTextSpeed(speed);
    }

    public void show(String text) {
        this.visible = true;
        printer.reset(EmeraldCharCodec.encode(text == null ? "" : text));
    }

    public void dismiss() {
        this.visible = false;
    }

    public boolean isBlocking() {
        return visible;
    }

    /**
     * Space, E, or Enter: while printing, skip to end of text; while waiting on a
     * mid-string pause, continue; when finished, close the overlay.
     */
    public void onAdvancePressed() {
        if (!visible) {
            return;
        }
        if (printer.isStuckOnPause()) {
            printer.resolvePause();
        } else if (!printer.isDonePrinting()) {
            printer.skipToEnd();
        } else {
            dismiss();
        }
    }

    public void tick() {
        if (!visible) {
            return;
        }
        printer.tick();
    }

    public void draw(Graphics2D g2, int panelWidth, int panelHeight) {
        if (!visible) {
            return;
        }

        Object oldTextAa = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        Object oldInterp = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        AffineTransform saved = g2.getTransform();
        g2.scale(EmeraldTextConstants.SCREEN_SCALE, EmeraldTextConstants.SCREEN_SCALE);
        frameRenderer.drawFrame(g2);
        // Glyph positions match the window pixel buffer (origin 0,0); map to screen like BlitBitmapToWindow.
        g2.translate(EmeraldTextConstants.INNER_TOPLEFT_SCREEN_X, EmeraldTextConstants.INNER_TOPLEFT_SCREEN_Y);
        printer.draw(g2);
        g2.setTransform(saved);

        if (oldTextAa != null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldTextAa);
        }
        if (oldInterp != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
        }
    }
}
