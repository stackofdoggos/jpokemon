package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.imageio.ImageIO;

import com.pokemon.resources.GameResources;

/**
 * Loads {@link EmeraldTextAssetPaths#FONT_NORMAL_PNG}: a 16×16 grid of 16×16
 * native-pixel cells, indexed by {@code charCode & 0xFF} (Emerald encoding).
 * The sheet uses marker colors (black = foreground, 50% gray = shadow,
 * white = opaque white, everything else transparent) that are recolored to the
 * requested fg/shadow colors at draw time. Per-glyph advance widths come from
 * {@link EmeraldTextAssetPaths#FONT_NORMAL_WIDTHS_TXT}. If the sheet resource
 * is missing, drawing falls back to {@link PlaceholderEmeraldGlyphSource}.
 */
public final class ImageEmeraldGlyphSource implements EmeraldGlyphSource {

    private static final int CELL_W = 16;
    private static final int CELL_H = 16;
    private static final int COLS = 16;

    private static final int FG_MARKER = 0xFF000000;
    private static final int SHADOW_MARKER = 0xFF808080;
    /** Glyph background pixels (palette index 3 in-game), mapped to the printer's bg color. */
    private static final int BG_MARKER = 0xFFFFFFFF;

    private final BufferedImage sheet;
    private final int[] widths = new int[256];
    private final PlaceholderEmeraldGlyphSource fallback = new PlaceholderEmeraldGlyphSource();

    /** Recolored copy of the sheet, cached for the last requested colors. */
    private BufferedImage tinted;
    private Color tintedFg;
    private Color tintedBg;
    private Color tintedShadow;

    public ImageEmeraldGlyphSource() {
        this(EmeraldTextAssetPaths.FONT_NORMAL_PNG, EmeraldTextAssetPaths.FONT_NORMAL_WIDTHS_TXT);
    }

    public ImageEmeraldGlyphSource(String sheetPath, String widthsPath) {
        BufferedImage loaded = null;
        try (InputStream in = GameResources.openStream(sheetPath)) {
            if (in != null) {
                loaded = ImageIO.read(in);
            }
        } catch (Exception ignored) {
            loaded = null;
        }
        this.sheet = loaded;
        loadWidths(widthsPath);
    }

    private void loadWidths(String widthsPath) {
        java.util.Arrays.fill(widths, EmeraldTextConstants.FONT_NORMAL_MAX_WIDTH);
        try (InputStream in = GameResources.openStream(widthsPath)) {
            if (in == null) {
                return;
            }
            BufferedReader reader = new BufferedReader(new InputStreamReader(in, StandardCharsets.US_ASCII));
            String line;
            int i = 0;
            while (i < widths.length && (line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    widths[i++] = Integer.parseInt(line);
                }
            }
        } catch (Exception ignored) {
        }
    }

    public boolean hasSheet() {
        return sheet != null;
    }

    @Override
    public int glyphWidth(byte charCode) {
        return widths[charCode & 0xff];
    }

    @Override
    public int glyphHeight(byte charCode) {
        return EmeraldTextConstants.FONT_NORMAL_MAX_HEIGHT;
    }

    @Override
    public void drawGlyph(Graphics2D g, byte charCode, int x, int y, Color fg, Color bg, Color shadow) {
        if (sheet == null) {
            fallback.drawGlyph(g, charCode, x, y, fg, bg, shadow);
            return;
        }
        BufferedImage colored = tintedSheet(fg, bg, shadow);
        int idx = charCode & 0xff;
        int sx = (idx % COLS) * CELL_W;
        int sy = (idx / COLS) * CELL_H;
        g.drawImage(colored.getSubimage(sx, sy, CELL_W, CELL_H), x, y, null);
    }

    private BufferedImage tintedSheet(Color fg, Color bg, Color shadow) {
        if (tinted != null && fg.equals(tintedFg) && bg.equals(tintedBg) && shadow.equals(tintedShadow)) {
            return tinted;
        }
        BufferedImage out = new BufferedImage(sheet.getWidth(), sheet.getHeight(), BufferedImage.TYPE_INT_ARGB);
        int fgRgb = fg.getRGB();
        int bgRgb = bg.getRGB();
        int shadowRgb = shadow.getRGB();
        for (int y = 0; y < sheet.getHeight(); y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                int argb = sheet.getRGB(x, y);
                if ((argb >>> 24) == 0) {
                    continue;
                }
                if (argb == FG_MARKER) {
                    out.setRGB(x, y, fgRgb);
                } else if (argb == SHADOW_MARKER) {
                    out.setRGB(x, y, shadowRgb);
                } else if (argb == BG_MARKER) {
                    out.setRGB(x, y, bgRgb);
                } else {
                    out.setRGB(x, y, argb);
                }
            }
        }
        tinted = out;
        tintedFg = fg;
        tintedBg = bg;
        tintedShadow = shadow;
        return out;
    }
}
