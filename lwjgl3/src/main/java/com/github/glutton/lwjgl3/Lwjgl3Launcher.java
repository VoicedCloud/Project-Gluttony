package com.github.glutton.lwjgl3;

import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.glutils.HdpiMode;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.github.glutton.GluttonGame;

public class Lwjgl3Launcher {
    public static void main(String[] args) {
        Box2D.init();

        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Gluttony");
        
        Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
        configuration.setWindowedMode(displayMode.width, displayMode.height);
        configuration.setDecorated(false);
        configuration.setResizable(false);
        configuration.useVsync(true);

        configuration.setInitialVisible(false);

        GluttonGame game = new GluttonGame();

        configuration.setWindowListener(new com.badlogic.gdx.backends.lwjgl3.Lwjgl3WindowListener() {
            @Override
            public void created(com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window window) {
                game.setWindowRevealCallback(() -> window.setVisible(true));
            }
            @Override public void iconified(boolean isIconified) {}
            @Override public void maximized(boolean isMaximized) {}
            @Override public void focusLost() {}
            @Override public void focusGained() {}
            @Override public boolean closeRequested() { return true; }
            @Override public void filesDropped(String[] files) {}
            @Override public void refreshRequested() {}
        });

        new Lwjgl3Application(game, configuration);
    }
}