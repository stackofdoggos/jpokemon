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

    /** True while the bouncing arrow is shown (page full or all text printed). */
    public boolean isAwaitingInput() {
        return visible && printer.isWaitingForInput();
    }

    /**
     * Space, E, or Enter: while printing, finish the current page instantly;
     * while waiting on a full page, scroll to the next line; when all text has
     * been shown, close the overlay.
     */
    public void onAdvancePressed() {
        if (!visible) {
            return;
        }
        if (printer.isDonePrinting()) {
            dismiss();
        } else if (printer.isWaitingForInput()) {
            printer.advancePage();
        } else {
            printer.finishCurrentPage();
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
        java.awt.Shape savedClip = g2.getClip();
        g2.scale(EmeraldTextConstants.SCREEN_SCALE, EmeraldTextConstants.SCREEN_SCALE);
        frameRenderer.drawFrame(g2);
        // Glyph positions match the window pixel buffer (origin 0,0); map to screen like BlitBitmapToWindow.
        g2.translate(EmeraldTextConstants.INNER_TOPLEFT_SCREEN_X, EmeraldTextConstants.INNER_TOPLEFT_SCREEN_Y);
        // Clip like the GBA window so scrolled lines disappear at the window edge.
        g2.clipRect(0, 0, EmeraldTextConstants.INNER_WIDTH_PX, EmeraldTextConstants.INNER_HEIGHT_PX);
        printer.draw(g2);
        g2.setClip(savedClip);
        g2.setTransform(saved);

        if (oldTextAa != null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldTextAa);
        }
        if (oldInterp != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, oldInterp);
        }
    }
}
