package com.pokemon;

import javax.swing.JFrame;

import com.pokemon.gui.GamePanel;

/**
 * Application entry point that creates the main game window and attaches the
 * {@link GamePanel}.
 */
public class Main {
    private Main() {
    }

    public static void main(String[] args) {
        final JFrame window = new JFrame();
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setResizable(false);
        window.setTitle("Pokemon Emerald MA");

        /*
         * GamePanel initialization
         */
        GamePanel gamePanel = new GamePanel();
        gamePanel.startGameThread();
        window.add(gamePanel);
        window.pack();

        window.setLocationRelativeTo(null);
        window.setVisible(true);
    }
}