package com.pokemon.gui;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Tracks WASD directional input and interact / menu actions (edge-triggered).
 */
public class KeyHandler implements KeyListener {

    private boolean upPressed;
    private boolean downPressed;
    private boolean leftPressed;
    private boolean rightPressed;

    /** Space or E: field interact (A button). Set on keyPressed, cleared by consume. */
    private boolean interactPressPending;

    /** Enter: often used to close message boxes. */
    private boolean confirmPressPending;

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
        } else if (keyCode == KeyEvent.VK_SPACE || keyCode == KeyEvent.VK_E) {
            interactPressPending = true;
        } else if (keyCode == KeyEvent.VK_ENTER) {
            confirmPressPending = true;
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

    /**
     * Clears and returns true once per physical Space/E press (for reading signs).
     */
    public boolean consumeSignInteractPress() {
        if (interactPressPending) {
            interactPressPending = false;
            return true;
        }
        return false;
    }

    /**
     * Clears and returns true if Space, E, or Enter was pressed (for closing UI).
     */
    public boolean consumeMessageDismissPress() {
        if (interactPressPending || confirmPressPending) {
            interactPressPending = false;
            confirmPressPending = false;
            return true;
        }
        return false;
    }

    /** Call when opening a blocking UI so stale key-edge flags do not auto-dismiss. */
    public void clearPendingMenuActions() {
        interactPressPending = false;
        confirmPressPending = false;
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
