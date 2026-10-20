package com.github.glutton.screens;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
import com.github.glutton.background.ParallaxBackground;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;
import com.github.glutton.entities.Enemy;
import com.github.glutton.entities.TargetDummy;
import com.github.glutton.world.GameplaySession;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import java.util.ArrayList;
import java.util.List;

public class Page1Screen implements Screen {

    private final GluttonGame game;
    private final List<Enemy> enemies = new ArrayList<>();
    private SceneLoader sceneLoader;
    private Viewport viewport;
    private OrthographicCamera camera;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private ParallaxBackground background;

    private boolean debugHitbox = false;
    private GameplaySession gameplaySession;

    public Page1Screen(GluttonGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.zoom = 0.25f;
        viewport = new FitViewport(1280f, 720f, camera);

        background = new ParallaxBackground();

        SceneConfiguration config = new SceneConfiguration();
        sceneLoader = new SceneLoader(config);
        sceneLoader.loadScene("Page1", viewport);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        gameplaySession = new GameplaySession(
                camera,
                "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
                "sprites/weapons/01 - Individual sprites/Bullets & Ammo/Glock - P80/Bullet.png"
        );

        // Spawn initial test dummy
        enemies.add(new TargetDummy(250f, 64f));
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            debugHitbox = !debugHitbox;
        }

        // Auto-respawn destroyed enemies
        enemies.removeIf(Enemy::isDestroyed);
        if (enemies.isEmpty()) {
            enemies.add(new TargetDummy(250f, 64f));
        }

        // Update gameplay and pass enemies list for ghost-ray collision handling
        gameplaySession.update(delta, enemies);
        // Note: Update your GameplaySession to pass `enemies` into bulletManager.update(delta, enemies)
        
        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;
        float cameraLeftEdge = camera.position.x - viewWidth / 2f;
        float cameraBottomEdge = camera.position.y - viewHeight / 2f;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        background.draw(batch, camera.position.x, cameraLeftEdge, cameraBottomEdge, viewWidth, viewHeight);
        batch.end();

        sceneLoader.getEngine().setDelta(delta);
        sceneLoader.getEngine().process();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        gameplaySession.render(batch, shapeRenderer);
        batch.end();

        gameplaySession.renderTrails(shapeRenderer, camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        gameplaySession.renderMuzzleFlash(shapeRenderer);

        // Render all active enemies
        for (Enemy enemy : enemies) {
            enemy.render(shapeRenderer);
        }

        shapeRenderer.end();

        if (debugHitbox) {
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

            shapeRenderer.setColor(0, 1, 1, 1);
            for (Enemy enemy : enemies) {
                shapeRenderer.rect(enemy.bounds.x, enemy.bounds.y, enemy.bounds.width, enemy.bounds.height);
            }

            shapeRenderer.end();
        }

        camera.position.set(gameplaySession.getPlayerState().x, gameplaySession.getPlayerState().y + 32f, 0);
        camera.update();
    }

    @Override
    public void resize(int width, int height) { viewport.update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (gameplaySession != null) gameplaySession.dispose();
    }
}