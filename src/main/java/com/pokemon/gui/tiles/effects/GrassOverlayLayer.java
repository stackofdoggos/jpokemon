package com.pokemon.gui.tiles.effects;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.imageio.ImageIO;

import com.pokemon.gui.GamePanel;
import com.pokemon.gui.tiles.BackgroundTile;
import com.pokemon.gui.tiles.TileLayer;
import com.pokemon.gui.tiles.TileUtils;
import com.pokemon.resources.GameResources;

/**
 * Grass overlay that plays a PNG transition per tile. Sequence per tile:
 * 0001 (under), 0002–0004 (over). If the player remains on that tile after
 * stage 4 completes, 0000 persists. If the player leaves earlier, the tile
 * continues ruffling (finishes 0002–0004) and then stops — no 0000 persists.
 */
public class GrassOverlayLayer {

    private final GamePanel gamePanel;
    private final TileLayer backgroundTileLayer;

    // per-tile overlay instances keyed by "row,col"
    private final Map<String, OverlayInstance> overlays = new HashMap<>();
    private String currentTileKey = null; // key of tile currently under player (or null)

    // preloaded overlay images by stage index (0..4)
    private BufferedImage[] stageImages;

    private static final String GRASS_TRANSITION_CLASSPATH_PREFIX = "art/animations/wildgrass/tall_grass_transition_0000";

    // Stage 1 hold duration is configured via TileUtils constant

    private static class OverlayInstance {
        int row;
        int col;
        int stage; // 1..4 are transition frames, 0 is persistent (only while current tile)
        int framesSpentInStage;

        OverlayInstance(int row, int col) {
            this.row = row;
            this.col = col;
            this.stage = 1;
            this.framesSpentInStage = 0;
        }
    }

    public GrassOverlayLayer(GamePanel gamePanel, TileLayer backgroundTileLayer) {
        this.gamePanel = gamePanel;
        this.backgroundTileLayer = backgroundTileLayer;
        loadOverlayImages();
    }

    public void update(com.pokemon.gui.entities.PlayerRender player) {
        int tileSize = gamePanel.getTileSize();

        // Compute bounding box edges and center
        int leftX = player.getBoundingBoxX();
        int topY = player.getBoundingBoxY();
        int bbWidth = tileSize - 2; // matches PlayerRender bounding box setup
        int bbHeight = tileSize - 2; // matches PlayerRender bounding box setup
        int rightX = leftX + bbWidth - 1;
        int bottomY = topY + bbHeight - 1;
        int centerX = (leftX + rightX) / 2;
        int centerY = (topY + bottomY) / 2;

        // Choose the leading-edge tile based on facing direction, but require a
        // minimum overlap along the movement axis before triggering.
        final int triggerCol;
        final int triggerRow;
        double fraction = TileUtils.GRASS_ENTER_LEADING_EDGE_FRACTION;
        int minWidthOverlap = Math.max(1, (int) Math.ceil(bbWidth * fraction));
        int minHeightOverlap = Math.max(1, (int) Math.ceil(bbHeight * fraction));
        if (player.getDirection().isFacingRight()) {
            int colCandidate = rightX / tileSize;
            int rowCandidate = centerY / tileSize;
            int tileLeftX = colCandidate * tileSize;
            int overlap = rightX - tileLeftX + 1; // pixels inside this tile
            triggerCol = (overlap >= minWidthOverlap) ? colCandidate : (leftX / tileSize);
            triggerRow = rowCandidate;
        } else if (player.getDirection().isFacingLeft()) {
            int colCandidate = leftX / tileSize;
            int rowCandidate = centerY / tileSize;
            int tileRightX = (colCandidate + 1) * tileSize - 1;
            int overlap = tileRightX - leftX + 1; // pixels inside this tile
            triggerCol = (overlap >= minWidthOverlap) ? colCandidate : (rightX / tileSize);
            triggerRow = rowCandidate;
        } else if (player.getDirection().isFacingUp()) {
            int colCandidate = centerX / tileSize;
            int rowCandidate = topY / tileSize;
            int tileBottomY = (rowCandidate + 1) * tileSize - 1;
            int overlap = tileBottomY - topY + 1;
            triggerCol = colCandidate;
            triggerRow = (overlap >= minHeightOverlap) ? rowCandidate : (bottomY / tileSize);
        } else { // facing down (or default)
            int colCandidate = centerX / tileSize;
            int rowCandidate = bottomY / tileSize;
            int tileTopY = rowCandidate * tileSize;
            int overlap = bottomY - tileTopY + 1;
            triggerCol = colCandidate;
            triggerRow = (overlap >= minHeightOverlap) ? rowCandidate : (topY / tileSize);
        }

        BackgroundTile tile = safeGetTile(triggerRow, triggerCol);
        String keyAtPlayer = null;
        if (tile != null && isInteractive(tile)) {
            keyAtPlayer = key(triggerRow, triggerCol);
            // if entered a new grass tile, start or restart its animation
            if (!keyAtPlayer.equals(currentTileKey)) {
                overlays.compute(keyAtPlayer, (k, inst) -> {
                    if (inst == null) {
                        return new OverlayInstance(triggerRow, triggerCol);
                    } else {
                        inst.stage = 1;
                        inst.framesSpentInStage = 0;
                        return inst;
                    }
                });
                currentTileKey = keyAtPlayer;
            }
        } else {
            currentTileKey = null;
        }

        // advance all overlay instances; remove those that finish off-screen (not
        // current)
        Iterator<Map.Entry<String, OverlayInstance>> it = overlays.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, OverlayInstance> entry = it.next();
            OverlayInstance inst = entry.getValue();
            boolean isCurrent = entry.getKey().equals(currentTileKey);

            if (inst.stage == 0) {
                // persistent stage only while tile is current; otherwise cancel immediately
                if (!isCurrent) {
                    it.remove();
                }
                continue;
            }

            inst.framesSpentInStage++;
            int framesRequiredForStage = (inst.stage == 1
                    ? TileUtils.GRASS_TRANSITION_FRAMES_FIRST_STAGE
                    : TileUtils.GRASS_TRANSITION_FRAMES_PER_IMAGE);
            if (inst.framesSpentInStage >= framesRequiredForStage) {
                inst.framesSpentInStage = 0;
                if (inst.stage < 4) {
                    inst.stage++;
                } else { // stage == 4
                    if (isCurrent) {
                        // player stayed on tile: enter persistent stage
                        inst.stage = 0;
                    } else {
                        // player already left: finish ruffle and disappear
                        it.remove();
                    }
                }
            }
        }
    }

    /** Draws overlays that belong under the player (but above background). */
    public void drawUnder(Graphics2D g2) {
        if (overlays.isEmpty())
            return;
        final var player = gamePanel.getPlayerRender();
        final boolean movingDown = player.getDirection().isFacingDown() && player.isMoving();
        final boolean movingUp = player.getDirection().isFacingUp() && player.isMoving();
        final int tileSize = gamePanel.getTileSize();
        // derive player tile row for depth comparisons independent of interactive tile
        int topYpx = player.getBoundingBoxY();
        int bottomYpx = topYpx + (tileSize - 2) - 1;
        int centerYpx = (topYpx + bottomYpx) / 2;
        final int playerRowForDepth = movingDown ? (bottomYpx / tileSize)
                : movingUp ? (topYpx / tileSize)
                        : (centerYpx / tileSize);
        // Compute up movement overlap into current tile
        int upOverlap = 0;
        if (movingUp) {
            int rowTopY = playerRowForDepth * tileSize;
            upOverlap = (rowTopY + tileSize - 1) - topYpx + 1;
        }
        int minUpOverlap = (int) Math.ceil((tileSize - 2) * TileUtils.GRASS_EMERGE_LEADING_EDGE_FRACTION);
        for (Map.Entry<String, OverlayInstance> entry : overlays.entrySet()) {
            OverlayInstance inst = entry.getValue();
            boolean isCurrent = entry.getKey().equals(currentTileKey);
            boolean behindPlayer = movingDown && inst.row < playerRowForDepth;
            boolean underWhileEmergingUp = movingUp && (inst.row == playerRowForDepth)
                    && (upOverlap < Math.max(1, minUpOverlap));
            if (inst.stage == 1) {
                drawStageAt(g2, inst.row, inst.col, 1);
            } else if ((behindPlayer || underWhileEmergingUp)
                    && ((inst.stage >= 2 && inst.stage <= 4) || (inst.stage == 0 && isCurrent))) {
                // While walking down on the current tile, render over-stages under the player
                drawStageAt(g2, inst.row, inst.col, inst.stage);
            }
        }
    }

    /** Draws overlays that belong over the player. */
    public void drawOver(Graphics2D g2) {
        if (overlays.isEmpty())
            return;
        final var player = gamePanel.getPlayerRender();
        final boolean movingDown = player.getDirection().isFacingDown() && player.isMoving();
        final boolean movingUp = player.getDirection().isFacingUp() && player.isMoving();
        final int tileSize = gamePanel.getTileSize();
        // derive player tile row for depth comparisons independent of interactive tile
        int topYpx = player.getBoundingBoxY();
        int bottomYpx = topYpx + (tileSize - 2) - 1;
        int centerYpx = (topYpx + bottomYpx) / 2;
        final int playerRowForDepth = movingDown ? (bottomYpx / tileSize)
                : movingUp ? (topYpx / tileSize)
                        : (centerYpx / tileSize);
        int upOverlap = 0;
        if (movingUp) {
            int rowTopY = playerRowForDepth * tileSize;
            upOverlap = (rowTopY + tileSize - 1) - topYpx + 1;
        }
        int minUpOverlap = (int) Math.ceil((tileSize - 2) * TileUtils.GRASS_EMERGE_LEADING_EDGE_FRACTION);
        for (Map.Entry<String, OverlayInstance> entry : overlays.entrySet()) {
            OverlayInstance inst = entry.getValue();
            boolean isCurrent = entry.getKey().equals(currentTileKey);
            boolean behindPlayer = movingDown && inst.row < playerRowForDepth;
            boolean underWhileEmergingUp = movingUp && (inst.row == playerRowForDepth)
                    && (upOverlap < Math.max(1, minUpOverlap));
            if (!behindPlayer && !underWhileEmergingUp
                    && ((inst.stage >= 2 && inst.stage <= 4) || (inst.stage == 0 && isCurrent))) {
                drawStageAt(g2, inst.row, inst.col, inst.stage);
            }
        }
    }

    // rowFromKey helper removed; row comparisons now use player's bounding box row

    private void drawStageAt(Graphics2D g2, int row, int col, int stageToDraw) {
        BufferedImage img = getImageForStage(stageToDraw);
        if (img == null)
            return;

        int tileSize = gamePanel.getTileSize();

        int worldX = col * tileSize;
        int worldY = row * tileSize;

        int screenX = worldX - gamePanel.getPlayerRender().getWorldX() + gamePanel.getPlayerRender().getScreenX();
        int screenY = worldY - gamePanel.getPlayerRender().getWorldY() + gamePanel.getPlayerRender().getScreenY();

        Object previousInterpolation = g2.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

        g2.drawImage(img, screenX, screenY, screenX + tileSize, screenY + tileSize, 0, 0, img.getWidth(),
                img.getHeight(), null);

        if (previousInterpolation != null) {
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, previousInterpolation);
        }
    }

    private void loadOverlayImages() {
        stageImages = new BufferedImage[5]; // 0..4
        for (int i = 0; i <= 4; i++) {
            String path = GRASS_TRANSITION_CLASSPATH_PREFIX.substring(0, GRASS_TRANSITION_CLASSPATH_PREFIX.length() - 1)
                    + i + ".png";
            try (InputStream in = GameResources.openStream(path)) {
                stageImages[i] = in == null ? null : ImageIO.read(in);
            } catch (IOException e) {
                stageImages[i] = null;
            }
        }
    }

    private BufferedImage getImageForStage(int s) {
        if (stageImages == null || s < 0 || s >= stageImages.length)
            return null;
        return stageImages[s];
    }

    private BackgroundTile safeGetTile(int row, int col) {
        try {
            return backgroundTileLayer.getTile(row, col);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean isInteractive(BackgroundTile tile) {
        String id = tile.getTexture().getID();
        return TileUtils.getTypeForId(id).isInteractive();
    }

    private static String key(int row, int col) {
        return row + "," + col;
    }
}
