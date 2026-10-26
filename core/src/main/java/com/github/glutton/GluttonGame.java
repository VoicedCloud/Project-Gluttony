package com.github.glutton;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.github.glutton.screens.MainMenuScreen;
import com.github.glutton.screens.LevelSelectScreen;
import com.github.glutton.screens.Page1Screen;
import com.github.glutton.screens.LoadingScreen;

public class GluttonGame extends Game {
    private MainMenuScreen mainMenuScreen;
    private LevelSelectScreen levelSelectScreen;
    private Page1Screen page1Screen;
    private Music menuMusic;

    private boolean mainMenuLoaded = false;
    private boolean levelSelectLoaded = false;
    private boolean musicLoaded = false;

    private Runnable windowRevealCallback;
    private boolean windowRevealed = false;

    @Override
    public void create() {
        setScreen(new LoadingScreen(this));
    }

    public void setWindowRevealCallback(Runnable callback) {
        this.windowRevealCallback = callback;
    }

    public void revealWindowOnce() {
        if (!windowRevealed && windowRevealCallback != null) {
            windowRevealCallback.run();
            windowRevealed = true;
        }
    }

    public boolean preloadNextStep() {
        if (!mainMenuLoaded) {
            if (mainMenuScreen == null) {
                mainMenuScreen = new MainMenuScreen(this);
            }
            mainMenuLoaded = true;
            return false;
        }
        if (!levelSelectLoaded) {
            if (levelSelectScreen == null) {
                levelSelectScreen = new LevelSelectScreen(this);
            }
            levelSelectLoaded = true;
            return false;
        }
        if (!musicLoaded) {
            loadMenuMusic();
            musicLoaded = true;
            return true;
        }
        return true;
    }

    public void showMainMenu() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }
        setScreen(mainMenuScreen);
        playMenuBgm();
    }

    public void showLevelSelect() {
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }
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
        loadMenuMusic();
        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    private void loadMenuMusic() {
        if (menuMusic == null) {
            try {
                menuMusic = Gdx.audio.newMusic(Gdx.files.internal("ui/Menu/BGM.wav"));
                menuMusic.setLooping(true);
            } catch (Exception e) {
                Gdx.app.error("Audio", "Failed to load menu BGM from ui/Menu/BGM.wav", e);
            }
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