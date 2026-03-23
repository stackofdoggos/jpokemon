package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.pokemon.resources.GameResources;

/**
 * Sequential field text printer: {@code FONT_NORMAL} layout, frame delays, optional
 * mid-string pauses ({@code CHAR_PROMPT_SCROLL}/{@code CHAR_PROMPT_CLEAR}), and a
 * blinking continue indicator when finished.
 */
public final class EmeraldTextPrinter {

    public sealed interface TextUnit permits GlyphStep, PauseStep {
    }

    public record GlyphStep(GlyphSlot slot) implements TextUnit {
    }

    public enum PauseStep implements TextUnit {
        INSTANCE
    }

    public record GlyphSlot(int x, int y, int w, int h, byte charCode) {
    }

    private final EmeraldGlyphSource glyphSource;
    private List<TextUnit> units;
    private int head;
    private int delayCounter;
    private int textSpeedFrames;
    private boolean stuckOnPause;
    private int arrowAnimTick;
    private final Color fg;
    private final Color shadow;
    private final BufferedImage arrowImage;

    public EmeraldTextPrinter(byte[] script, EmeraldGlyphSource glyphSource, EmeraldTextConstants.TextSpeed speed) {
        this.glyphSource = glyphSource;
        this.textSpeedFrames = speed.frameDelay();
        this.fg = EmeraldTextConstants.TEXT_FG;
        this.shadow = EmeraldTextConstants.TEXT_SHADOW;
        this.arrowImage = loadArrow(EmeraldTextAssetPaths.DOWN_ARROW_PNG);
        reset(script);
    }

    private static BufferedImage loadArrow(String path) {
        try (InputStream in = GameResources.openStream(path)) {
            if (in != null) {
                return ImageIO.read(in);
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    public void setTextSpeed(EmeraldTextConstants.TextSpeed speed) {
        this.textSpeedFrames = speed.frameDelay();
    }

    public void reset(byte[] script) {
        this.units = compile(script, glyphSource);
        this.head = 0;
        this.delayCounter = 0;
        this.stuckOnPause = false;
        this.arrowAnimTick = 0;
    }

    public boolean isStuckOnPause() {
        return stuckOnPause;
    }

    /** All units processed; continue arrow may show. */
    public boolean isDonePrinting() {
        return head >= units.size() && !stuckOnPause;
    }

    public void tick() {
        arrowAnimTick++;
        if (stuckOnPause) {
            return;
        }
        if (head >= units.size()) {
            return;
        }
        TextUnit u = units.get(head);
        if (u instanceof PauseStep) {
            stuckOnPause = true;
            return;
        }
        if (delayCounter > 0) {
            delayCounter--;
            return;
        }
        head++;
        if (head >= units.size()) {
            return;
        }
        TextUnit next = units.get(head);
        if (next instanceof GlyphStep) {
            delayCounter = textSpeedFrames;
        } else {
            delayCounter = 0;
        }
    }

    /** Skip pauses and print all glyphs immediately. */
    public void skipToEnd() {
        stuckOnPause = false;
        head = units.size();
        delayCounter = 0;
    }

    /** Advance past a mid-string {@link PauseStep} after the player confirms. */
    public void resolvePause() {
        if (!stuckOnPause) {
            return;
        }
        stuckOnPause = false;
        if (head < units.size() && units.get(head) == PauseStep.INSTANCE) {
            head++;
        }
        delayCounter = 0;
    }

    /** Expects {@code g} in window pixel space (0…{@link EmeraldTextConstants#INNER_WIDTH_PX} etc.). */
    public void draw(Graphics2D g) {
        for (int i = 0; i < head; i++) {
            TextUnit u = units.get(i);
            if (u instanceof GlyphStep gs) {
                GlyphSlot s = gs.slot();
                glyphSource.drawGlyph(g, s.charCode(), s.x, s.y, fg, EmeraldTextConstants.TEXT_BG, shadow);
            }
        }
        if (isDonePrinting()) {
            drawContinueIndicator(g);
        }
    }

    private void drawContinueIndicator(Graphics2D g) {
        // Window-local coords; MessageOverlay translates to INNER_TOPLEFT_SCREEN_* before printer.draw.
        int ax = EmeraldTextConstants.INNER_WIDTH_PX - 14;
        int ay = EmeraldTextConstants.INNER_HEIGHT_PX - 14;
        int blink = (arrowAnimTick / 16) % 2;
        if (blink == 0) {
            return;
        }
        if (arrowImage != null) {
            g.drawImage(arrowImage, ax, ay, null);
        } else {
            g.setColor(fg);
            g.fillRect(ax, ay, 8, 8);
        }
    }

    static List<TextUnit> compile(byte[] script, EmeraldGlyphSource glyphs) {
        List<TextUnit> out = new ArrayList<>();
        int x = EmeraldTextConstants.TEXT_ORIGIN_X;
        int y = EmeraldTextConstants.TEXT_ORIGIN_Y;
        int lineH = EmeraldTextConstants.FONT_NORMAL_MAX_HEIGHT + EmeraldTextConstants.FONT_NORMAL_LINE_SPACING;
        int ls = EmeraldTextConstants.FONT_NORMAL_LETTER_SPACING;
        int innerW = EmeraldTextConstants.INNER_WIDTH_PX;
        int ox = EmeraldTextConstants.TEXT_ORIGIN_X;

        int i = 0;
        while (i < script.length) {
            byte b = script[i];
            if (b == EmeraldCharacters.EOS) {
                break;
            }
            if (b == EmeraldCharacters.EXT_CTRL_CODE_BEGIN) {
                i = EmeraldCharCodec.indexAfterExtCtrl(script, i + 1);
                continue;
            }
            if (b == EmeraldCharacters.CHAR_NEWLINE) {
                x = ox;
                y += lineH;
                i++;
                continue;
            }
            if (b == EmeraldCharacters.CHAR_PROMPT_SCROLL || b == EmeraldCharacters.CHAR_PROMPT_CLEAR) {
                out.add(PauseStep.INSTANCE);
                i++;
                continue;
            }
            if (b == EmeraldCharacters.CHAR_SPACE) {
                x += glyphs.glyphWidth(b) + ls;
                i++;
                continue;
            }
            int j = i;
            while (j < script.length) {
                byte c = script[j];
                if (c == EmeraldCharacters.EOS || c == EmeraldCharacters.CHAR_NEWLINE || c == EmeraldCharacters.CHAR_SPACE
                        || c == EmeraldCharacters.EXT_CTRL_CODE_BEGIN || c == EmeraldCharacters.CHAR_PROMPT_SCROLL
                        || c == EmeraldCharacters.CHAR_PROMPT_CLEAR) {
                    break;
                }
                j++;
            }
            int wordW = 0;
            for (int k = i; k < j; k++) {
                wordW += glyphs.glyphWidth(script[k]) + ls;
            }
            if (x > ox && x + wordW > innerW) {
                x = ox;
                y += lineH;
            }
            for (int k = i; k < j; k++) {
                byte c = script[k];
                int w = glyphs.glyphWidth(c);
                int h = glyphs.glyphHeight(c);
                out.add(new GlyphStep(new GlyphSlot(x, y, w, h, c)));
                x += w + ls;
            }
            i = j;
        }
        return out;
    }
}
