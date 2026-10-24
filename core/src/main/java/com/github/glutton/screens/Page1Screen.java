
package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import com.github.glutton.GluttonGame;
import com.github.glutton.background.ParallaxBackground;
import com.github.glutton.entities.Enemy;
import com.github.glutton.entities.TargetDummy;
import com.github.glutton.player.PlayerState;
import com.github.glutton.world.GameplaySession;
import com.github.glutton.world.PhysicsConfig;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Page1Screen implements Screen {

    private final GluttonGame game;
    private final List<Enemy> enemies = new ArrayList<>();

    private SceneLoader sceneLoader;
    private Viewport viewport;
    private OrthographicCamera camera;

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer box2dDebugRenderer;
    private BitmapFont hudFont;
    private ParallaxBackground background;

    private boolean debugHitbox = false;
    private GameplaySession gameplaySession;

    private static final float ENEMY_WIDTH = 32f;
    private static final float ENEMY_HEIGHT = 48f;

    // Fixed world spawn coordinates: bottom-left of enemy.
    private static final float ENEMY_SPAWN_X = 200f;
    private static final float ENEMY_SPAWN_Y = 100f;

    public Page1Screen(GluttonGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        camera = new OrthographicCamera();
        camera.zoom = 0.25f;

        viewport = new FitViewport(1280f, 720f, camera);
        viewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );

        background = new ParallaxBackground(camera.position.y);

        sceneLoader = new SceneLoader(
            new SceneConfiguration()
        );
        sceneLoader.loadScene("Page1", viewport);

        if (sceneLoader.getWorld() != null) {
            sceneLoader.getWorld().setGravity(
                new Vector2(0f, PhysicsConfig.GRAVITY_Y)
            );
        }

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        box2dDebugRenderer = new Box2DDebugRenderer();

        hudFont = new BitmapFont();
        hudFont.getData().setScale(0.8f);

        gameplaySession = new GameplaySession(
            camera,
            "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
            "sprites/weapons/01 - Individual sprites/Bullets & Ammo/Glock - P80/Bullet.png",
            sceneLoader
        );

        spawnEnemy();
    }

    private void spawnEnemy() {
        if (sceneLoader == null
                || sceneLoader.getWorld() == null) {
            return;
        }

        TargetDummy enemy = new TargetDummy(
            sceneLoader.getWorld(),
            ENEMY_SPAWN_X,
            ENEMY_SPAWN_Y,
            ENEMY_WIDTH,
            ENEMY_HEIGHT,
            150,
            20,
            250f,
            40f,
            1f
        );

        enemies.add(enemy);
    }

    @Override
    public void render(float delta) {
        if (camera == null
                || viewport == null
                || batch == null) {
            return;
        }

        float safeDelta = Math.min(
            Math.max(delta, 0f),
            0.1f
        );

        Gdx.gl.glClearColor(
            0.05f, 0.05f, 0.05f, 1f
        );
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            debugHitbox = !debugHitbox;
        }

        // Remove defeated enemies and their bodies.
        if (sceneLoader != null
                && sceneLoader.getWorld() != null
                && !sceneLoader.getWorld().isLocked()) {

            Iterator<Enemy> iterator = enemies.iterator();

            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();

                if (enemy.isDestroyed()) {
                    enemy.disposeBody();
                    iterator.remove();
                }
            }
        }

        // Respawn at the fixed coordinates after defeat.
        if (enemies.isEmpty()) {
            spawnEnemy();
        }

        // Step the existing physics world.
        if (sceneLoader != null
                && sceneLoader.getWorld() != null) {

            float subDelta =
                safeDelta / PhysicsConfig.PHYSICS_SUBSTEPS;

            for (int i = 0;
                    i < PhysicsConfig.PHYSICS_SUBSTEPS;
                    i++) {

                sceneLoader.getWorld().step(
                    subDelta,
                    PhysicsConfig.VELOCITY_ITERATIONS,
                    PhysicsConfig.POSITION_ITERATIONS
                );
            }
        }

        PlayerState playerState = null;

        if (gameplaySession != null) {
            gameplaySession.update(safeDelta, enemies);

            playerState = gameplaySession.getPlayerState();

            if (playerState != null) {
                float playerX = playerState.x;
                float playerY = playerState.y;

                for (Enemy enemy : enemies) {
                    enemy.updateAI(safeDelta, playerState);

                    if (enemy.canAttack(playerX, playerY)) {
                        playerState.takeDamage(
                            enemy.getAttackDamage()
                        );

                        Gdx.app.log(
                            "Enemy",
                            "Enemy attacked player. HP: "
                                + playerState.health
                        );
                    }
                }

                camera.position.set(
                    playerX,
                    playerY + 32f,
                    0f
                );
            }
        }

        camera.update();
        viewport.apply();

        float viewWidth =
            camera.viewportWidth * camera.zoom;

        float viewHeight =
            camera.viewportHeight * camera.zoom;

        float cameraLeftEdge =
            camera.position.x - viewWidth / 2f;

        float cameraBottomEdge =
            camera.position.y - viewHeight / 2f;

        // Background.
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        if (background != null) {
            background.draw(
                batch,
                camera.position.x,
                camera.position.y,
                cameraLeftEdge,
                cameraBottomEdge,
                viewWidth,
                viewHeight
            );
        }

        batch.end();

        // Level scene.
        if (sceneLoader != null
                && sceneLoader.getEngine() != null) {
            sceneLoader.getEngine().setDelta(safeDelta);
            sceneLoader.getEngine().process();
        }

        // Player, weapons, and enemies.
        if (gameplaySession != null) {
            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            gameplaySession.render(batch, shapeRenderer);

            batch.end();

            gameplaySession.renderTrails(
                shapeRenderer,
                camera.combined
            );

            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(
                ShapeRenderer.ShapeType.Filled
            );

            gameplaySession.renderMuzzleFlash(shapeRenderer);

            for (Enemy enemy : enemies) {
                enemy.render(shapeRenderer);
            }

            shapeRenderer.end();
        }

        // Player HP bar and numeric value.
        if (playerState != null) {
            renderPlayerHealth(
                playerState,
                viewWidth,
                viewHeight
            );
        }

        if (debugHitbox
                && sceneLoader != null
                && sceneLoader.getWorld() != null) {
            box2dDebugRenderer.render(
                sceneLoader.getWorld(),
                camera.combined
            );
        }
    }

    private void renderPlayerHealth(
            PlayerState player,
            float viewWidth,
            float viewHeight
    ) {
        float left =
            camera.position.x - viewWidth / 2f + 12f;

        float top =
            camera.position.y + viewHeight / 2f - 16f;

        float barWidth = 100f;
        float barHeight = 8f;

        float percentage = player.maxHealth > 0
            ? (float) player.health / player.maxHealth
            : 0f;

        percentage = Math.max(
            0f,
            Math.min(1f, percentage)
        );

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(
            ShapeRenderer.ShapeType.Filled
        );

        shapeRenderer.setColor(
            0.12f, 0.12f, 0.12f, 1f
        );
        shapeRenderer.rect(
            left,
            top - barHeight,
            barWidth,
            barHeight
        );

        if (percentage > 0f) {
            if (percentage > 0.5f) {
                shapeRenderer.setColor(
                    0.15f, 0.85f, 0.2f, 1f
                );
            } else if (percentage > 0.25f) {
                shapeRenderer.setColor(
                    1f, 0.65f, 0.05f, 1f
                );
            } else {
                shapeRenderer.setColor(
                    0.9f, 0.12f, 0.12f, 1f
                );
            }

            shapeRenderer.rect(
                left,
                top - barHeight,
                barWidth * percentage,
                barHeight
            );
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(1f, 1f, 1f, 1f);
        hudFont.draw(
            batch,
            "HP: " + player.health + " / " + player.maxHealth,
            left,
            top + 2f
        );

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
    }

    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }

    @Override
    public void dispose() {
        if (sceneLoader != null
                && sceneLoader.getWorld() != null
                && !sceneLoader.getWorld().isLocked()) {
            for (Enemy enemy : enemies) {
                enemy.disposeBody();
            }
        }

        enemies.clear();

        if (gameplaySession != null) {
            gameplaySession.dispose();
            gameplaySession = null;
        }

        if (background != null) {
            background.dispose();
            background = null;
        }

        if (batch != null) {
            batch.dispose();
            batch = null;
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
            shapeRenderer = null;
        }

        if (box2dDebugRenderer != null) {
            box2dDebugRenderer.dispose();
            box2dDebugRenderer = null;
        }

        if (hudFont != null) {
            hudFont.dispose();
            hudFont = null;
        }

        if (sceneLoader != null) {
            sceneLoader.dispose();
            sceneLoader = null;
        }
    }
}
