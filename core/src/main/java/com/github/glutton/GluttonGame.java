package com.github.glutton;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.github.glutton.screens.MainMenuScreen;

public class GluttonGame extends Game {

	private Music menuBgm;

	@Override
	public void create() {
		playMenuBgm();
		setScreen(new MainMenuScreen(this));
	}

	public void playMenuBgm() {
		if (menuBgm == null && Gdx.files.internal("UI/Menu/BGM.wav").exists()) {
			menuBgm = Gdx.audio.newMusic(Gdx.files.internal("UI/Menu/BGM.wav"));
			menuBgm.setLooping(true);
		}
		if (menuBgm != null && !menuBgm.isPlaying()) {
			menuBgm.play();
		}
	}

	public void stopMenuBgm() {
		if (menuBgm != null && menuBgm.isPlaying()) {
			menuBgm.stop();
		}
	}

	@Override
	public void dispose() {
		super.dispose();
		if (menuBgm != null) {
			menuBgm.dispose();
		}
	}

	public void playLevelBgm(String musicPath) {
		if (musicPath != null && Gdx.files.internal(musicPath).exists()) {
			// Stop menu music if playing
			if (menuBgm != null && menuBgm.isPlaying()) {
				menuBgm.stop();
			}
			// Load and play new track
			Music levelBgm = Gdx.audio.newMusic(Gdx.files.internal(musicPath));
			levelBgm.setLooping(true);
			levelBgm.play();
		} else {
			// Silence: stop existing music if track doesn't exist
			if (menuBgm != null && menuBgm.isPlaying()) {
				menuBgm.stop();
			}
		}
	}
}
