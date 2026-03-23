package com.pokemon.gui;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import com.pokemon.resources.GameResources;

/**
 * Emerald-style field message box drawn in screen space. Optional frame image
 * from classpath; falls back to filled rectangle + border.
 */
public class MessageOverlay {

    private static final Color PANEL_FILL = new Color(0x18, 0x38, 0x18, 230);
    private static final Color PANEL_BORDER = new Color(0x50, 0x78, 0x50);
    private static final Color TEXT_COLOR = new Color(0xf8, 0xf8, 0xf0);

    private boolean visible;
    private String currentText = "";

    /** Nullable; e.g. {@code art/ui/messagebox_frame.png} */
    private final String optionalFrameClasspath;

    private BufferedImage cachedFrame;

    public MessageOverlay() {
        this(null);
    }

    public MessageOverlay(String optionalFrameClasspath) {
        this.optionalFrameClasspath = optionalFrameClasspath;
    }

    public void show(String text) {
        this.currentText = text == null ? "" : text;
        this.visible = true;
    }

    public void dismiss() {
        this.visible = false;
        this.currentText = "";
    }

    public boolean isBlocking() {
        return visible;
    }

    public void draw(Graphics2D g2, int panelWidth, int panelHeight) {
        if (!visible) {
            return;
        }

        int margin = 24;
        int boxHeight = Math.min(panelHeight / 3, 160);
        int boxY = panelHeight - boxHeight - margin;
        int boxX = margin;
        int boxW = panelWidth - 2 * margin;

        Object oldInterp = g2.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        BufferedImage frame = getFrameImage();
        if (frame != null) {
            g2.drawImage(frame, boxX, boxY, boxX + boxW, boxY + boxHeight, 0, 0, frame.getWidth(), frame.getHeight(),
                    null);
        } else {
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
            g2.setColor(PANEL_FILL);
            g2.fillRoundRect(boxX, boxY, boxW, boxHeight, 8, 8);
            g2.setColor(PANEL_BORDER);
            g2.drawRoundRect(boxX, boxY, boxW, boxHeight, 8, 8);
        }

        int padX = 20;
        int padY = 16;
        Font font = new Font(Font.MONOSPACED, Font.BOLD, 18);
        g2.setFont(font);
        g2.setColor(TEXT_COLOR);

        FontMetrics fm = g2.getFontMetrics();
        int textX = boxX + padX;
        int lineHeight = fm.getHeight();
        int maxTextWidth = boxW - 2 * padX;

        List<String> lines = wrapText(currentText, fm, maxTextWidth);
        int textY = boxY + padY + fm.getAscent();
        for (String line : lines) {
            if (textY + fm.getDescent() > boxY + boxHeight - padY) {
                break;
            }
            g2.drawString(line, textX, textY);
            textY += lineHeight;
        }

        if (oldInterp != null) {
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, oldInterp);
        }
    }

    private BufferedImage getFrameImage() {
        if (optionalFrameClasspath == null) {
            return null;
        }
        if (cachedFrame != null) {
            return cachedFrame;
        }
        try (InputStream in = GameResources.openStream(optionalFrameClasspath)) {
            if (in != null) {
                cachedFrame = ImageIO.read(in);
            }
        } catch (Exception ignored) {
            cachedFrame = null;
        }
        return cachedFrame;
    }

    private static List<String> wrapText(String text, FontMetrics fm, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            result.add("");
            return result;
        }
        for (String paragraph : text.split("\n")) {
            if (paragraph.isEmpty()) {
                result.add("");
                continue;
            }
            String[] words = paragraph.split(" ");
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String trial = line.isEmpty() ? word : line + " " + word;
                if (fm.stringWidth(trial) <= maxWidth) {
                    line = new StringBuilder(trial);
                } else {
                    if (!line.isEmpty()) {
                        result.add(line.toString());
                    }
                    line = new StringBuilder(word);
                }
            }
            if (!line.isEmpty()) {
                result.add(line.toString());
            }
        }
        return result;
    }
}
