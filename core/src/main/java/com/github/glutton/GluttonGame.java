package com.github.glutton;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.github.glutton.screens.MainMenuScreen;
import com.github.glutton.screens.LevelSelectScreen;
import com.github.glutton.screens.Page1Screen;

public class GluttonGame extends Game {
    private MainMenuScreen mainMenuScreen;
    private LevelSelectScreen levelSelectScreen;
    private Page1Screen page1Screen;
    private Music menuMusic;

    @Override
    public void create() {
        mainMenuScreen = new MainMenuScreen(this);
        levelSelectScreen = new LevelSelectScreen(this);
        setScreen(mainMenuScreen);
        playMenuBgm();
    }

    public void showMainMenu() {
        setScreen(mainMenuScreen);
        playMenuBgm();
    }

    public void showLevelSelect() {
        setScreen(levelSelectScreen);
    }

    public void showPage1() {
        if (page1Screen == null) {
            page1Screen = new Page1Screen(this);
        }
        playLevelBgm("audio/bgm/page1_theme.mp3");
        setScreen(page1Screen);
    }

    public void playMenuBgm() {
        if (menuMusic == null) {
            try {
                menuMusic = Gdx.audio.newMusic(Gdx.files.internal("ui/Menu/BGM.wav"));
                menuMusic.setLooping(true);
            } catch (Exception e) {
                Gdx.app.error("Audio", "Failed to load menu BGM from ui/Menu/BGM.wav", e);
                return;
            }
        }
        if (!menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    public void playLevelBgm(String path) {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (menuMusic != null) {
            menuMusic.dispose();
            menuMusic = null;
        }
    }
}