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
 * Sequential field text printer modeled on pokeemerald's {@code RenderText}:
 * variable-width {@code FONT_NORMAL} glyphs, per-glyph frame delays, a two-line
 * window with page waits, smooth one-line scrolling, and the bouncing red
 * "continue" arrow drawn at the text cursor while waiting for input.
 */
public final class EmeraldTextPrinter {

    private enum State {
        PRINTING,
        /** Page is full (or a scroll prompt was hit); arrow bounces until input. */
        WAIT_PAGE,
        /** Window contents slide up one line. */
        SCROLLING,
        /** All text printed; arrow bounces until the box is dismissed. */
        DONE
    }

    public sealed interface TextUnit permits GlyphStep, PageWaitStep {
    }

    /** One printed glyph at a fixed position on an absolute (unscrolled) line. */
    public record GlyphStep(GlyphSlot slot) implements TextUnit {
    }

    /** Wait for player input, then scroll the window up one line. */
    public enum PageWaitStep implements TextUnit {
        INSTANCE
    }

    public record GlyphSlot(int x, int line, byte charCode) {
    }

    /** Bounce pattern of the continue arrow ({@code sDownArrowYCoords}). */
    private static final int[] ARROW_Y_COORDS = { 0, 1, 2, 1 };
    /** Frames between arrow bounce steps ({@code downArrowDelay}). */
    private static final int ARROW_FRAME_DELAY = 8;
    private static final int ARROW_W = 8;
    private static final int ARROW_H = 16;

    private static final int LINE_HEIGHT = EmeraldTextConstants.FONT_NORMAL_MAX_HEIGHT
            + EmeraldTextConstants.FONT_NORMAL_LINE_SPACING;

    private final EmeraldGlyphSource glyphSource;
    private final Color fg;
    private final Color shadow;
    private final BufferedImage arrowImage;

    private List<TextUnit> units;
    private int head;
    private int delayCounter;
    private int textSpeedFrames;
    private State state;
    /** Lines scrolled off the top of the window. */
    private int scrolledLines;
    /** Pixel offset of the in-progress scroll animation (0..LINE_HEIGHT). */
    private int scrollAnimPx;
    private int arrowAnimTick;
    /** Text cursor (window pixels / absolute line) after the last printed glyph. */
    private int cursorX;
    private int cursorLine;

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
        this.scrolledLines = 0;
        this.scrollAnimPx = 0;
        this.arrowAnimTick = 0;
        this.cursorX = EmeraldTextConstants.TEXT_ORIGIN_X;
        this.cursorLine = 0;
        this.state = units.isEmpty() ? State.DONE : State.PRINTING;
    }

    /** Waiting for input: either a full page or the end of the text. */
    public boolean isWaitingForInput() {
        return state == State.WAIT_PAGE || state == State.DONE;
    }

    /** All units processed and final wait reached. */
    public boolean isDonePrinting() {
        return state == State.DONE;
    }

    public void tick() {
        arrowAnimTick++;
        switch (state) {
            case PRINTING -> tickPrinting();
            case SCROLLING -> tickScrolling();
            case WAIT_PAGE, DONE -> {
            }
        }
    }

    private void tickPrinting() {
        if (head >= units.size()) {
            state = State.DONE;
            return;
        }
        if (delayCounter > 0) {
            delayCounter--;
            return;
        }
        TextUnit u = units.get(head);
        if (u instanceof PageWaitStep) {
            state = State.WAIT_PAGE;
            arrowAnimTick = 0;
            return;
        }
        GlyphSlot slot = ((GlyphStep) u).slot();
        head++;
        cursorX = slot.x() + glyphSource.glyphWidth(slot.charCode()) + EmeraldTextConstants.FONT_NORMAL_LETTER_SPACING;
        cursorLine = slot.line();
        delayCounter = textSpeedFrames;
        if (head >= units.size()) {
            state = State.DONE;
            arrowAnimTick = 0;
        }
    }

    private void tickScrolling() {
        scrollAnimPx += EmeraldTextConstants.SCROLL_SPEED_PX;
        if (scrollAnimPx >= LINE_HEIGHT) {
            scrollAnimPx = 0;
            scrolledLines++;
            state = State.PRINTING;
            delayCounter = 0;
        }
    }

    /** Confirm a page wait and start scrolling; no-op unless waiting on a page. */
    public void advancePage() {
        if (state != State.WAIT_PAGE) {
            return;
        }
        if (head < units.size() && units.get(head) == PageWaitStep.INSTANCE) {
            head++;
        }
        state = State.SCROLLING;
    }

    /** Instantly print the remainder of the current page (A pressed mid-print). */
    public void finishCurrentPage() {
        if (state != State.PRINTING) {
            return;
        }
        while (head < units.size() && units.get(head) instanceof GlyphStep gs) {
            GlyphSlot slot = gs.slot();
            cursorX = slot.x() + glyphSource.glyphWidth(slot.charCode())
                    + EmeraldTextConstants.FONT_NORMAL_LETTER_SPACING;
            cursorLine = slot.line();
            head++;
        }
        delayCounter = 0;
        if (head >= units.size()) {
            state = State.DONE;
            arrowAnimTick = 0;
        } else {
            state = State.WAIT_PAGE;
            arrowAnimTick = 0;
        }
    }

    /** Expects {@code g} in window pixel space, clipped to the inner window rect. */
    public void draw(Graphics2D g) {
        int yShift = scrolledLines * LINE_HEIGHT + scrollAnimPx;
        for (int i = 0; i < head; i++) {
            if (units.get(i) instanceof GlyphStep gs) {
                GlyphSlot s = gs.slot();
                int y = EmeraldTextConstants.TEXT_ORIGIN_Y + s.line() * LINE_HEIGHT - yShift;
                if (y + LINE_HEIGHT <= 0 || y >= EmeraldTextConstants.INNER_HEIGHT_PX) {
                    continue;
                }
                glyphSource.drawGlyph(g, s.charCode(), s.x(), y, fg, EmeraldTextConstants.TEXT_BG, shadow);
            }
        }
        if (isWaitingForInput()) {
            drawContinueArrow(g, yShift);
        }
    }

    private void drawContinueArrow(Graphics2D g, int yShift) {
        // Keep the arrow inside the window when text runs to the right edge.
        int ax = Math.min(cursorX, EmeraldTextConstants.INNER_WIDTH_PX - ARROW_W);
        int ay = EmeraldTextConstants.TEXT_ORIGIN_Y + cursorLine * LINE_HEIGHT - yShift;
        int bounce = ARROW_Y_COORDS[(arrowAnimTick / ARROW_FRAME_DELAY) % ARROW_Y_COORDS.length];
        if (arrowImage != null && arrowImage.getHeight() >= bounce + ARROW_H) {
            g.drawImage(arrowImage.getSubimage(0, bounce, ARROW_W, ARROW_H), ax, ay, null);
        } else {
            g.setColor(fg);
            g.fillRect(ax, ay + 4 + bounce, ARROW_W, ARROW_H / 2);
        }
    }

    /**
     * Lays text out into glyph steps with absolute line indices, auto-wrapping at
     * the window edge, and inserts a {@link PageWaitStep} before each line that
     * would not fit in the {@link EmeraldTextConstants#VISIBLE_LINES}-line window.
     */
    static List<TextUnit> compile(byte[] script, EmeraldGlyphSource glyphs) {
        List<TextUnit> out = new ArrayList<>();
        int ls = EmeraldTextConstants.FONT_NORMAL_LETTER_SPACING;
        int innerW = EmeraldTextConstants.INNER_WIDTH_PX;
        int ox = EmeraldTextConstants.TEXT_ORIGIN_X;
        int x = ox;
        int line = 0;

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
                line = newLine(out, line);
                i++;
                continue;
            }
            if (b == EmeraldCharacters.CHAR_PROMPT_SCROLL || b == EmeraldCharacters.CHAR_PROMPT_CLEAR) {
                out.add(PageWaitStep.INSTANCE);
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
                if (c == EmeraldCharacters.EOS || c == EmeraldCharacters.CHAR_NEWLINE
                        || c == EmeraldCharacters.CHAR_SPACE || c == EmeraldCharacters.EXT_CTRL_CODE_BEGIN
                        || c == EmeraldCharacters.CHAR_PROMPT_SCROLL || c == EmeraldCharacters.CHAR_PROMPT_CLEAR) {
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
                line = newLine(out, line);
            }
            for (int k = i; k < j; k++) {
                byte c = script[k];
                out.add(new GlyphStep(new GlyphSlot(x, line, c)));
                x += glyphs.glyphWidth(c) + ls;
            }
            i = j;
        }
        return out;
    }

    /** Advances to the next line, inserting a page wait once the window is full. */
    private static int newLine(List<TextUnit> out, int line) {
        int next = line + 1;
        if (next >= EmeraldTextConstants.VISIBLE_LINES) {
            out.add(PageWaitStep.INSTANCE);
        }
        return next;
    }
}
