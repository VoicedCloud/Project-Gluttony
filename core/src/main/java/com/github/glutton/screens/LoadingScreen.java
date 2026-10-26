package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;

public class LoadingScreen implements Screen {

    private final GluttonGame game;
    private final Viewport viewport;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private float timer = 0f;
    private static final float MIN_DISPLAY_DURATION = 2.0f;
    private boolean loadingComplete = false;

    public LoadingScreen(GluttonGame game) {
        this.game = game;
        viewport = new FitViewport(1280f, 720f);
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();
        font.getData().setScale(1.5f);
    }

    @Override
    public void show() {
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
    }

@Override
    public void render(float delta) {
        // Reveal the hidden window instantly on the very first frame
        game.revealWindowOnce();

        timer += delta;

        Gdx.gl.glClearColor(0.02f, 0.02f, 0.03f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        viewport.apply();
        batch.setProjectionMatrix(viewport.getCamera().combined);
        shapeRenderer.setProjectionMatrix(viewport.getCamera().combined);

        // Render loading visuals
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.12f, 0.12f, 0.15f, 1f);
        shapeRenderer.rect(440f, 320f, 400f, 24f);

        float progress = Math.min(1f, timer / MIN_DISPLAY_DURATION);
        shapeRenderer.setColor(0.85f, 0.18f, 0.12f, 1f);
        shapeRenderer.rect(440f, 320f, 400f * progress, 24f);
        shapeRenderer.end();

        batch.begin();
        font.setColor(Color.WHITE);
        String title = "GLUTTONY";
        GlyphLayout titleLayout = new GlyphLayout(font, title);
        font.draw(batch, title, 640f - titleLayout.width / 2f, 400f);

        String loadingText = "LOADING...";
        GlyphLayout textLayout = new GlyphLayout(font, loadingText);
        font.draw(batch, loadingText, 640f - textLayout.width / 2f, 280f);
        batch.end();

        // Incrementally preload heavy assets in the background
        if (!loadingComplete) {
            loadingComplete = game.preloadNextStep();
        }

        // Transition when minimum time has passed and background assets are ready
        if (timer >= MIN_DISPLAY_DURATION && loadingComplete) {
            game.showMainMenu();
        } else if ((Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY) || Gdx.input.justTouched()) && loadingComplete) {
            game.showMainMenu();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (font != null) font.dispose();
    }
}