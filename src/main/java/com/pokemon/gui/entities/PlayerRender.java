package com.pokemon.gui.entities;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.pokemon.gui.GamePanel;
import com.pokemon.gui.KeyHandler;
import com.pokemon.resources.GameResources;
// import com.pokemon.gui.tiles.TileUtils;

/**
 * Renders and updates the controllable player character. Handles input,
 * animation frames, collision and tile-step movement.
 */
public class PlayerRender extends EntityRender {

    @SuppressWarnings("unused")
    private GamePanel gamePanel;
    private KeyHandler keyHandler;

    /*
     * where to draw player on the screen (the center)
     */

    private final int screenX;
    private final int screenY;

    private static final int NUM_WALK_SPRITES = 4; // 4 walking sprites (0, 1, 2, 3)
    private static final int PLAYER_SCALE = 2; // might pose issues for collisions
    private static final int PLAYER_WIDTH_IN_PIXELS = PLAYER_SCALE * 24; // 48
    private static final int PLAYER_HEIGHT_IN_PIXELS = PLAYER_SCALE * 48; // 96
    private static final int OFFSET_Y_IN_PIXELS = 15; // Brendan's sprite is not perfectly on the bottom
    private static final int FRAMES_PER_MOVEMENT_IMAGE = 8; // 8 seems to be good for walking, changes for running
    // gamePanel.getTileSize() = 48, but can't call method, 16x32 to 48x96 (2 tiles
    // tall and one tile wide)
    /*
     * todo: implement some sort of offset so Brendan touches the ground
     */
    /*
     * From watching videos, characters walk 4 tiles / second
     * tile = 48 pixels
     * second = 60 frames
     * need in pixels/frame
     */

    private int framesSpentStandingStill = 0;
    private final int MAX_FRAMES_ALLOWED_TO_STAND_STILL = 10;

    private TileStepListener tileStepListener;

    public PlayerRender(GamePanel gamePanel, KeyHandler keyHandler) {

        /*
         * default being used right now - nov 30, 2024
         * If future me is wondering, the reason you see black on the left at (300, 300)
         * in route1 is because the screen is a 3:2 rectangle lol
         */

        // default speed is 3 tiles per second
        super(960, 480, 3); // worldX, worldY, speed
        this.gamePanel = gamePanel;
        this.keyHandler = keyHandler;
        this.direction = Direction.DOWN;

        /*
         * the reason for dividing player size in pixels by two is to place the CENTER
         * of the player character in the center of the screen, since draw automatically
         * starts drawing from the top left and centers the top left of the player
         * instead of the center
         */

        this.screenX = gamePanel.getCenterOfScreenWidthInPixels() - PLAYER_WIDTH_IN_PIXELS / 2;
        this.screenY = gamePanel.getCenterOfScreenHeightInPixels() - PLAYER_HEIGHT_IN_PIXELS / 2;
        getPlayerImages();

        this.boundingBox = new Rectangle();
        this.boundingBox.x = 1; // one pixel offset because for some reason 48x48 bb wasnt working
        this.boundingBox.y = gamePanel.getTileSize() + 1; // bounding box only on bottom half of player
        this.boundingBox.width = gamePanel.getTileSize() - 2; // bounding box is one tile exactly
        this.boundingBox.height = gamePanel.getTileSize() - 2; // bounding box is one tile exactly
    }

    public PlayerRender(GamePanel gamePanel, KeyHandler keyHandler, int worldX, int worldY, int speed) {
        super(worldX, worldY, speed);
        this.gamePanel = gamePanel;
        this.direction = Direction.DOWN;
        this.screenX = gamePanel.getCenterOfScreenWidthInPixels() - PLAYER_WIDTH_IN_PIXELS / 2;
        this.screenY = gamePanel.getCenterOfScreenHeightInPixels() - PLAYER_HEIGHT_IN_PIXELS / 2;
    }

    public void setTileStepListener(TileStepListener tileStepListener) {
        this.tileStepListener = tileStepListener;
    }

    /** Clears movement state and places the player for a new map. */
    public void resetForLocation(int worldX, int worldY, Direction facing) {
        setWorldX(worldX);
        setWorldY(worldY);
        setDirection(facing);
        this.isMoving = false;
        this.isColliding = false;
        this.pixelsMovedFromPreviousPosition = 0;
        this.framesInCurrentMovementImage = 0;
        this.indexOfCurrentMovementImage = 0;
        this.framesSpentStandingStill = 0;
    }

    public int getScreenX() {
        return this.screenX;
    }

    public int getScreenY() {
        return this.screenY;
    }

    public int getBoundingBoxX() {
        return this.worldX + this.boundingBox.x;
    }

    public int getBoundingBoxY() {
        return this.worldY + this.boundingBox.y;
    }

    public void getPlayerImages() {
        try {
            for (int i = 0; i < NUM_WALK_SPRITES; i++) {
                this.movementDownImages.add(readImage("art/entities/players/Brendan/walking/walkdown/down" + i + ".png"));
            }
            for (int i = 0; i < NUM_WALK_SPRITES; i++) {
                this.movementUpImages.add(readImage("art/entities/players/Brendan/walking/walkup/up" + i + ".png"));
            }
            for (int i = 0; i < NUM_WALK_SPRITES; i++) {
                this.movementRightImages
                        .add(readImage("art/entities/players/Brendan/walking/walkright/right" + i + ".png"));
            }
            for (int i = 0; i < NUM_WALK_SPRITES; i++) {
                this.movementLeftImages
                        .add(readImage("art/entities/players/Brendan/walking/walkleft/left" + i + ".png"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static BufferedImage readImage(String classpathRelativePath) throws Exception {
        try (InputStream in = GameResources.openStream(classpathRelativePath)) {
            if (in == null) {
                throw new IllegalStateException("Missing resource: " + classpathRelativePath);
            }
            return ImageIO.read(in);
        }
    }

    @Override
    public void draw(Graphics2D g2) {

        // g2.setColor(Color.WHITE);
        // g2.fillRect(this.getX(), this.getY(), gamePanel.getTileSize(),
        // gamePanel.getTileSize());

        BufferedImage image = null;

        if (this.getDirection().isFacingUp()) {
            image = this.movementUpImages.get(indexOfCurrentMovementImage);
        }

        else if (this.getDirection().isFacingDown()) {
            image = this.movementDownImages.get(indexOfCurrentMovementImage);
        }

        else if (this.getDirection().isFacingLeft()) {
            image = this.movementLeftImages.get(indexOfCurrentMovementImage);
        }

        else if (this.getDirection().isFacingRight()) {
            image = this.movementRightImages.get(indexOfCurrentMovementImage);
        }

        /*
         * screenX and screenY will always correspond to the middle of the screen in
         * pixels
         */
        g2.drawImage(image, screenX, screenY + OFFSET_Y_IN_PIXELS, PLAYER_WIDTH_IN_PIXELS, PLAYER_HEIGHT_IN_PIXELS,
                null);
    }

    @Override
    public void update() {

        // TileUtils.printCurrentTileForDebugging(this, this.gamePanel);

        if (!this.isMoving()) {
            if (this.keyHandler.isAnyDirectionKeyPressed()) {

                this.isMoving = true;

                // Match pokeemerald FieldGetPlayerInput: UP > DOWN > LEFT > RIGHT when
                // multiple directions are held (else-if chain, not independent ifs).
                if (this.keyHandler.isUpKeyPressed()) {
                    this.direction = Direction.UP;
                } else if (this.keyHandler.isDownKeyPressed()) {
                    this.direction = Direction.DOWN;
                } else if (this.keyHandler.isLeftKeyPressed()) {
                    this.direction = Direction.LEFT;
                } else if (this.keyHandler.isRightKeyPressed()) {
                    this.direction = Direction.RIGHT;
                }

                this.isColliding = false; // got collisionOn from EntityRender
                gamePanel.getCollisionChecker().checkTile(this); // check if we are colliding
            }

            else {
                framesSpentStandingStill++;
                if (framesSpentStandingStill == MAX_FRAMES_ALLOWED_TO_STAND_STILL) {
                    indexOfCurrentMovementImage = 0;
                    framesSpentStandingStill = 0;
                }
            }
        }

        if (this.isMoving()) {
            // if we are not colliding, proceed with movement
            if (!isColliding) {
                if (this.getDirection().isFacingUp()) {
                    this.moveUp(this.getSpeed());
                }

                else if (this.getDirection().isFacingDown()) {
                    this.moveDown(this.getSpeed());
                }

                else if (this.getDirection().isFacingRight()) {
                    this.moveRight(this.getSpeed());
                }

                else if (this.getDirection().isFacingLeft()) {
                    this.moveLeft(this.getSpeed());
                }
            }

            // regardless of collision, enter walking animation
            framesInCurrentMovementImage++;

            // every n frames, make the player switch to the next walking frame
            // more constants!
            if (framesInCurrentMovementImage > FRAMES_PER_MOVEMENT_IMAGE) {

                indexOfCurrentMovementImage++;

                // if adding causes Player to be on the fourth walking image (nonexistant), loop
                if (indexOfCurrentMovementImage == NUM_WALK_SPRITES) {
                    indexOfCurrentMovementImage = 0;
                }

                framesInCurrentMovementImage = 0;
            }

            this.pixelsMovedFromPreviousPosition += this.speed;

            if (pixelsMovedFromPreviousPosition >= gamePanel.getTileSize()) {
                if (!isColliding && tileStepListener != null) {
                    int tileSize = gamePanel.getTileSize();
                    int cx = getBoundingBoxX() + boundingBox.width / 2;
                    int cy = getBoundingBoxY() + boundingBox.height / 2;
                    tileStepListener.onStepCompleted(cx / tileSize, cy / tileSize);
                }
                isMoving = false;
                pixelsMovedFromPreviousPosition = 0;
            }
        }
    }

    // SETTERS:

    /*
     * player has a lot of movement so lets do this
     */

    public void moveUp(int amount) {
        this.setWorldY(worldY - amount);
    }

    public void moveDown(int amount) {
        this.setWorldY(worldY + amount);
    }

    public void moveRight(int amount) {
        this.setWorldX(worldX + amount);
    }

    public void moveLeft(int amount) {
        this.setWorldX(worldX - amount);
    }
}
