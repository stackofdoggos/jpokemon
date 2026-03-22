package com.pokemon.gui.entities;

import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

/**
 * Base class for drawable, updatable world entities. Tracks world position,
 * animation frames, movement state and collision box. Concrete subclasses
 * implement {@link #update()} and {@link #draw(Graphics2D)}.
 */
public abstract class EntityRender {
    protected int worldX, worldY; // entity's position on the world map
    protected int speed;

    protected final ArrayList<BufferedImage> movementUpImages = new ArrayList<>();
    protected final ArrayList<BufferedImage> movementDownImages = new ArrayList<>();
    protected final ArrayList<BufferedImage> movementRightImages = new ArrayList<>();
    protected final ArrayList<BufferedImage> movementLeftImages = new ArrayList<>();

    protected int framesInCurrentMovementImage = 0;
    protected int indexOfCurrentMovementImage = 0; // starts at 0 because pngs start at 0

    protected Direction direction;

    protected Rectangle boundingBox;
    protected boolean isColliding = false;
    protected boolean isMoving = false;
    protected int pixelsMovedFromPreviousPosition = 0;

    public EntityRender(int x, int y, int speed) {
        this.worldX = x;
        this.worldY = y;
        this.speed = speed;
    }

    // GETTERS:
    public Direction getDirection() {
        return this.direction;
    }

    public int getWorldX() {
        return this.worldX;
    }

    public int getWorldY() {
        return this.worldY;
    }

    public int getSpeed() {
        return this.speed;
    }

    public boolean isMoving() {
        return this.isMoving;
    }

    public int getPixelsMovedFromPreviousPosition() {
        return this.pixelsMovedFromPreviousPosition;
    }

    // SETTERS:
    public void setWorldX(int x) {
        this.worldX = x;
    }

    public void setWorldY(int y) {
        this.worldY = y;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    // METHODS:
    /** Update entity state for current tick. */
    public abstract void update();

    /** Draw the entity using the provided graphics context. */
    public abstract void draw(Graphics2D g2);

    public enum Direction {
        UP {
            @Override
            public boolean isFacingUp() {
                return true;
            }
        },
        DOWN {
            @Override
            public boolean isFacingDown() {
                return true;
            }
        },
        LEFT {
            @Override
            public boolean isFacingLeft() {
                return true;
            }
        },
        RIGHT {
            @Override
            public boolean isFacingRight() {
                return true;
            }
        };

        public boolean isFacingUp() {
            return false;
        }

        public boolean isFacingDown() {
            return false;
        }

        public boolean isFacingLeft() {
            return false;
        }

        public boolean isFacingRight() {
            return false;
        }
    }
}
