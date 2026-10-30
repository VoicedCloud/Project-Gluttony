package com.github.glutton;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.github.glutton.screens.MainMenuScreen;
import com.github.glutton.screens.LevelSelectScreen;
import com.github.glutton.screens.Page1Screen;
import com.github.glutton.screens.LoadingScreen;
import com.github.glutton.screens.DifficultySettingsScreen;

/**
 * Main application coordinator for Project Gluttony.
 * Extends LibGDX's Game class to manage screen transitions, audio lifecycle, 
 * game difficulty state, and initial asset preloading steps.
 */
public class GluttonGame extends Game {

    // Global difficulty configuration (defaults to NORMAL)
    private Difficulty difficulty = Difficulty.NORMAL;
    private DifficultySettingsScreen difficultySettingsScreen;

    // Cached game screen instances
    private MainMenuScreen mainMenuScreen;
    private LevelSelectScreen levelSelectScreen;
    private Page1Screen page1Screen;

    // Global background music handler for menus
    private Music menuMusic;

    // Incremental loading flags for smooth startup preloading
    private boolean mainMenuLoaded = false;
    private boolean levelSelectLoaded = false;
    private boolean musicLoaded = false;

    // LWJGL3 desktop window reveal callback handler
    private Runnable windowRevealCallback;
    private boolean windowRevealed = false;

    /**
     * Called upon application initialization.
     * Launches the splash/loading screen first to manage asynchronous asset preloading.
     */
    @Override
    public void create() {
        setScreen(new LoadingScreen(this));
    }

    /**
     * Registers a callback to handle desktop window visibility toggles once assets are ready.
     */
    public void setWindowRevealCallback(Runnable callback) {
        this.windowRevealCallback = callback;
    }

    /**
     * Triggers the window reveal callback once during execution to prevent premature screen flickering.
     */
    public void revealWindowOnce() {
        if (!windowRevealed && windowRevealCallback != null) {
            windowRevealCallback.run();
            windowRevealed = true;
        }
    }

    /**
     * Incrementally preloads screens and audio tracks across multiple frame steps.
     *
     * @return true when all initial preloading steps are finished, false if steps remain.
     */
    public boolean preloadNextStep() {

        // Step 1: Pre-instantiate the main menu screen
        if (!mainMenuLoaded) {
            if (mainMenuScreen == null) {
                mainMenuScreen = new MainMenuScreen(this);
            }

            mainMenuLoaded = true;
            return false;
        }

        // Step 2: Pre-instantiate the level selection screen
        if (!levelSelectLoaded) {
            if (levelSelectScreen == null) {
                levelSelectScreen = new LevelSelectScreen(this);
            }

            levelSelectLoaded = true;
            return false;
        }

        // Step 3: Load menu audio assets
        if (!musicLoaded) {
            loadMenuMusic();
            musicLoaded = true;
            return true;
        }

        return true;
    }

    /**
     * Switches the active display to the Main Menu Screen and initiates menu BGM.
     */
    public void showMainMenu() {
        if (mainMenuScreen == null) {
            mainMenuScreen = new MainMenuScreen(this);
        }

        setScreen(mainMenuScreen);
        playMenuBgm();
    }

    /**
     * Switches the active display to the Level Selection Screen.
     */
    public void showLevelSelect() {
        if (levelSelectScreen == null) {
            levelSelectScreen = new LevelSelectScreen(this);
        }

        setScreen(levelSelectScreen);
    }

    /**
     * Re-creates and transitions to a fresh gameplay session (Page1Screen).
     * Disposes previous gameplay instances to ensure a clean state reset.
     */
    public void showPage1() {
        // Clear previous gameplay state to avoid memory leaks or stale entities
        if (page1Screen != null) {
            page1Screen.dispose();
        }

        page1Screen = new Page1Screen(this);

        playLevelBgm("audio/bgm/page1_theme.mp3");
        setScreen(page1Screen);
    }

    /**
     * Retrieves the current global difficulty setting.
     */
    public Difficulty getDifficulty() {
        return difficulty;
    }

    /**
     * Updates the global difficulty setting across gameplay sessions.
     */
    public void setDifficulty(Difficulty difficulty) {
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
    }

    /**
     * Switches the active display to the Difficulty Settings Screen.
     */
    public void showDifficultySettings() {
        if (difficultySettingsScreen == null) {
            difficultySettingsScreen = new DifficultySettingsScreen(this);
        }

        setScreen(difficultySettingsScreen);
    }

    /**
     * Plays the main menu background music loop if not already playing.
     */
    public void playMenuBgm() {
        loadMenuMusic();

        if (menuMusic != null && !menuMusic.isPlaying()) {
            menuMusic.play();
        }
    }

    /**
     * Internal helper to load and configure the menu BGM file safely with exception handling.
     */
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

    /**
     * Handles background audio transitions when entering gameplay levels.
     */
    public void playLevelBgm(String path) {
        if (menuMusic != null && menuMusic.isPlaying()) {
            menuMusic.stop();
        }
    }

    /**
     * Cleans up native audio resources and screen instances when the application closes.
     */
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