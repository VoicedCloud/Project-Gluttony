package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.github.glutton.Difficulty;
import com.github.glutton.GluttonGame;

public class DifficultySettingsScreen implements Screen {

    private final GluttonGame game;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private BitmapFont font;

    private OrthographicCamera camera;
    private Viewport viewport;

    private final float WORLD_WIDTH = 1280f;
    private final float WORLD_HEIGHT = 720f;

    private final Rectangle easyButton =
            new Rectangle(440, 390, 400, 65);

    private final Rectangle normalButton =
            new Rectangle(440, 300, 400, 65);

    private final Rectangle hardButton =
            new Rectangle(440, 210, 400, 65);

    private final Rectangle backButton =
            new Rectangle(440, 100, 400, 55);

    private final Vector3 touchPoint = new Vector3();

    private boolean wasTouched = false;

    public DifficultySettingsScreen(GluttonGame game) {
        this.game = game;

        camera = new OrthographicCamera();

        viewport = new FitViewport(
                WORLD_WIDTH,
                WORLD_HEIGHT,
                camera
        );

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        font = new BitmapFont();

        font.getData().setScale(2f);

        camera.position.set(
                WORLD_WIDTH / 2f,
                WORLD_HEIGHT / 2f,
                0
        );

        camera.update();
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
    }

    @Override
    public void render(float delta) {

        Gdx.gl.glClearColor(0.04f, 0.04f, 0.07f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        camera.update();

        shapeRenderer.setProjectionMatrix(
                camera.combined
        );

        batch.setProjectionMatrix(
                camera.combined
        );

        // Draw buttons
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        drawButton(
                easyButton,
                Difficulty.EASY,
                new Color(0.15f, 0.55f, 0.20f, 1f)
        );

        drawButton(
                normalButton,
                Difficulty.NORMAL,
                new Color(0.15f, 0.30f, 0.65f, 1f)
        );

        drawButton(
                hardButton,
                Difficulty.HARD,
                new Color(0.70f, 0.15f, 0.15f, 1f)
        );

        shapeRenderer.setColor(
                0.30f, 0.30f, 0.35f, 1f
        );

        shapeRenderer.rect(
                backButton.x,
                backButton.y,
                backButton.width,
                backButton.height
        );

        shapeRenderer.end();

        // Draw text
        batch.begin();

        font.setColor(Color.WHITE);

        font.getData().setScale(3f);

        font.draw(
                batch,
                "DIFFICULTY SETTINGS",
                390,
                540
        );

        font.getData().setScale(2f);

        drawCenteredText(
                "EASY",
                easyButton
        );

        drawCenteredText(
                "NORMAL",
                normalButton
        );

        drawCenteredText(
                "HARD",
                hardButton
        );

        drawCenteredText(
                "BACK",
                backButton
        );

        font.setColor(Color.LIGHT_GRAY);
        font.getData().setScale(1.3f);

        font.draw(
                batch,
                "Easy: 50% fewer monsters",
                470,
                370
        );

        font.draw(
                batch,
                "Normal: Current monster count",
                450,
                280
        );

        font.draw(
                batch,
                "Hard: 50% more monsters",
                465,
                190
        );

        batch.end();

        handleInput();
    }

    private void drawButton(
            Rectangle button,
            Difficulty buttonDifficulty,
            Color buttonColor) {

        if (game.getDifficulty() == buttonDifficulty) {
            shapeRenderer.setColor(Color.GOLD);
        } else {
            shapeRenderer.setColor(buttonColor);
        }

        shapeRenderer.rect(
                button.x,
                button.y,
                button.width,
                button.height
        );
    }

    private void drawCenteredText(
            String text,
            Rectangle button) {

        float textWidth = font.getRegion().getRegionWidth();

        // Approximate centering for the button label
        float x = button.x + button.width / 2f
                - text.length() * 10f;

        float y = button.y + button.height / 2f + 10f;

        font.setColor(Color.WHITE);
        font.draw(batch, text, x, y);
    }

    private void handleInput() {

        boolean touched =
                Gdx.input.isButtonPressed(
                        com.badlogic.gdx.Input.Buttons.LEFT
                );

        if (touched && !wasTouched) {

            touchPoint.set(
                    Gdx.input.getX(),
                    Gdx.input.getY(),
                    0
            );

            viewport.unproject(touchPoint);

            float x = touchPoint.x;
            float y = touchPoint.y;

            if (easyButton.contains(x, y)) {
                game.setDifficulty(Difficulty.EASY);

            } else if (normalButton.contains(x, y)) {
                game.setDifficulty(Difficulty.NORMAL);

            } else if (hardButton.contains(x, y)) {
                game.setDifficulty(Difficulty.HARD);

            } else if (backButton.contains(x, y)) {
                game.showMainMenu();
            }
        }

        wasTouched = touched;
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    @Override
    public void dispose() {
        if (batch != null) {
            batch.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (font != null) {
            font.dispose();
        }
    }
}