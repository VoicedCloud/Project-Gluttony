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
		
		// Borderless Fullscreen: matches monitor resolution with no window borders
		Graphics.DisplayMode displayMode = Lwjgl3ApplicationConfiguration.getDisplayMode();
		configuration.setWindowedMode(displayMode.width, displayMode.height);
		configuration.setDecorated(false); // Removes the window title bar and borders
		
		configuration.setResizable(false);
		configuration.setHdpiMode(HdpiMode.Pixels);
		configuration.useVsync(true);

		new Lwjgl3Application(new GluttonGame(), configuration);
	}
}