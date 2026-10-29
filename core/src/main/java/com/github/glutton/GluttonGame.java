package com.github.glutton;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.github.glutton.screens.MainMenuScreen;
import com.github.glutton.screens.LevelSelectScreen;
import com.github.glutton.screens.Page1Screen;
import com.github.glutton.screens.LoadingScreen;
import com.github.glutton.screens.DifficultySettingsScreen;

public class GluttonGame extends Game {

    // Difficulty settings
    private Difficulty difficulty = Difficulty.NORMAL;
    private DifficultySettingsScreen difficultySettingsScreen;

    // Game screens
    private MainMenuScreen mainMenuScreen;
    private LevelSelectScreen levelSelectScreen;
    private Page1Screen page1Screen;

    // Audio
    private Music menuMusic;

    // Loading flags
    private boolean mainMenuLoaded = false;
    private boolean levelSelectLoaded = false;
    private boolean musicLoaded = false;

    // Window reveal
    private Runnable windowRevealCallback;
    private boolean windowRevealed = false;

    @Override
    public void create() {
        setScreen(new LoadingScreen(this));
    }

    // Window reveal methods
    public void setWindowRevealCallback(Runnable callback) {
        this.windowRevealCallback = callback;
    }

    public void revealWindowOnce() {
        if (!windowRevealed && windowRevealCallback != null) {
            windowRevealCallback.run();
            windowRevealed = true;
        }
    }

    // Preload screens and music
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

    // Show main menu
    public void showMainMenu() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }

        setScreen(mainMenuScreen);
        playMenuBgm();
    }

    // Show level selection
    public void showLevelSelect() {
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }

        setScreen(levelSelectScreen);
    }

    // Show gameplay
    public void showPage1() {
        // Create a fresh gameplay screen when starting the level
        if (page1Screen != null) {
            page1Screen.dispose();
        }

        page1Screen = new Page1Screen(this);

        playLevelBgm("audio/bgm/page1_theme.mp3");
        setScreen(page1Screen);
    }

    // Difficulty settings
    public Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    // Show difficulty settings screen
    public void showDifficultySettings() {
        if (difficultySettingsScreen == null) {
            difficultySettingsScreen = new DifficultySettingsScreen(this);
        }

        setScreen(difficultySettingsScreen);
    }

    // Menu music
    public void playMenuBgm() {
        loadMenuMusic();

        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    private void loadMenuMusic() {
        if (menuMusic == null) {
            try {
                menuMusic = Gdx.audio.newMusic(
                    Gdx.files.internal("ui/Menu/BGM.wav")
                );

                menuMusic.setLooping(true);

            } catch (Exception e) {
                Gdx.app.error(
                    "Audio",
                    "Failed to load menu BGM from ui/Menu/BGM.wav",
                    e
                );
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

        if (difficultySettingsScreen != null) {
            difficultySettingsScreen.dispose();
            difficultySettingsScreen = null;
        }
    }
}