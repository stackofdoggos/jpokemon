package com.pokemon.gui.text.emerald;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

import com.pokemon.gui.MessageOverlay;

/**
 * Headless dev utility: renders the field message box to PNGs so dialogue
 * changes can be verified without launching the game.
 *
 * <pre>
 * mvn -q compile
 * java -cp target/classes com.pokemon.gui.text.emerald.DialoguePreview /tmp "Some sign text" 999
 * </pre>
 *
 * Args: output dir, message (optional), ticks to simulate (optional). Writes
 * {@code dialogue_preview_<ticks>.png} snapshots at a few interesting moments
 * (mid-print, page wait, final state).
 */
public final class DialoguePreview {

    private DialoguePreview() {
    }

    public static void main(String[] args) throws Exception {
        File outDir = new File(args.length > 0 ? args[0] : "/tmp");
        String message = args.length > 1 ? args[1]
                : "ROUTE 101\nIf you follow the path, you will reach OLDALE TOWN.";
        int maxTicks = args.length > 2 ? Integer.parseInt(args[2]) : 600;

        MessageOverlay overlay = new MessageOverlay();
        overlay.show(message);

        int waitTicks = 0;
        int waitsSeen = 0;
        for (int t = 0; t <= maxTicks && overlay.isBlocking(); t++) {
            overlay.tick();
            if (overlay.isAwaitingInput()) {
                waitTicks++;
                // Hold each wait for a moment (shows the bouncing arrow), then advance.
                if (waitTicks == 20) {
                    waitsSeen++;
                    save(overlay, new File(outDir, "dialogue_preview_wait" + waitsSeen + ".png"));
                    overlay.onAdvancePressed();
                    waitTicks = 0;
                }
            }
            if (t == 30) {
                save(overlay, new File(outDir, "dialogue_preview_30.png"));
            }
        }
        System.out.println("previews written to " + outDir);
    }

    private static void save(MessageOverlay overlay, File file) throws Exception {
        BufferedImage img = new BufferedImage(720, 480, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = img.createGraphics();
        g2.setColor(new Color(120, 168, 96));
        g2.fillRect(0, 0, img.getWidth(), img.getHeight());
        overlay.draw(g2, img.getWidth(), img.getHeight());
        g2.dispose();
        ImageIO.write(img, "png", file);
    }
}
