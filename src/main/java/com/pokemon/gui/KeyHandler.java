package com.pokemon.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Tracks WASD directional input and exposes pressed state flags to the game.
 */
public class KeyHandler implements KeyListener {

    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    @Override
    public void keyPressed(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_W) {
            upPressed = true;
        } else if (keyCode == KeyEvent.VK_S) {
            downPressed = true;
        } else if (keyCode == KeyEvent.VK_A) {
            leftPressed = true;
        } else if (keyCode == KeyEvent.VK_D) {
            rightPressed = true;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int keyCode = e.getKeyCode();

        if (keyCode == KeyEvent.VK_W) {
            upPressed = false;
        } else if (keyCode == KeyEvent.VK_S) {
            downPressed = false;
        } else if (keyCode == KeyEvent.VK_A) {
            leftPressed = false;
        } else if (keyCode == KeyEvent.VK_D) {
            rightPressed = false;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // we do not care ._.
    }

    public boolean isAnyDirectionKeyPressed() {
        return upPressed || downPressed || leftPressed || rightPressed;
    }

    public boolean isUpKeyPressed() {
        return this.upPressed;
    }

    public boolean isDownKeyPressed() {
        return this.downPressed;
    }

    public boolean isRightKeyPressed() {
        return this.rightPressed;
    }

    public boolean isLeftKeyPressed() {
        return this.leftPressed;
    }
}
