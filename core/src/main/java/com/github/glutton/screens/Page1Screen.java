package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
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
import com.github.glutton.world.GameplaySession;
import com.github.glutton.world.PhysicsConfig;
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
    private Box2DDebugRenderer box2dDebugRenderer;
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

        if (sceneLoader.getWorld() != null) {
            sceneLoader.getWorld().setGravity(
                new Vector2(0f, PhysicsConfig.GRAVITY_Y)
            );
        }

        viewport.update(
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight(),
            true
        );

        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        box2dDebugRenderer = new Box2DDebugRenderer();

        gameplaySession = new GameplaySession(
            camera,
            "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
            "sprites/weapons/01 - Individual sprites/Bullets & Ammo/Glock - P80/Bullet.png",
            sceneLoader
        );

        spawnEnemy();
    }

    private void spawnEnemy() {
        TargetDummy enemy = new TargetDummy(
            200f, 150f,  // Spawn X, Y
            32f, 48f,    // Width, height
            150,         // Health
            20,          // Attack damage
            250f,        // Detection range
            40f,         // Attack range
            1f           // Attack cooldown in seconds
        );

        enemies.add(enemy);
    }

    @Override
    public void render(float delta) {
        viewport.apply();

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            debugHitbox = !debugHitbox;
        }

        // Remove defeated enemies and respawn them.
        enemies.removeIf(Enemy::isDestroyed);

        if (enemies.isEmpty()) {
            spawnEnemy();
        }

        // 1. Step physics.
        if (sceneLoader.getWorld() != null) {
            float subDelta = delta / PhysicsConfig.PHYSICS_SUBSTEPS;

            for (int i = 0; i < PhysicsConfig.PHYSICS_SUBSTEPS; i++) {
                sceneLoader.getWorld().step(
                    subDelta,
                    PhysicsConfig.VELOCITY_ITERATIONS,
                    PhysicsConfig.POSITION_ITERATIONS
                );
            }
        }

        // 2. Update gameplay.
        gameplaySession.update(delta, enemies);

        // 3. Update NPC aggro, movement, and attack cooldown.
        float playerX = gameplaySession.getPlayerState().x;
        float playerY = gameplaySession.getPlayerState().y;

        for (Enemy currentEnemy : enemies) {
            currentEnemy.updateAggro(delta, playerX, playerY);

            if (currentEnemy.canAttack(playerX, playerY)) {
                Gdx.app.log(
                    "Enemy",
                    "Enemy attacked! Damage: " + currentEnemy.getAttackDamage()
                );
            }
        }

        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;

        float cameraLeftEdge = camera.position.x - viewWidth / 2f;
        float cameraBottomEdge = camera.position.y - viewHeight / 2f;

        // 4. Draw background.
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        background.draw(
            batch,
            camera.position.x,
            cameraLeftEdge,
            cameraBottomEdge,
            viewWidth,
            viewHeight
        );

        batch.end();

        // 5. Render the level.
        sceneLoader.getEngine().setDelta(delta);
        sceneLoader.getEngine().process();

        // 6. Render gameplay.
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        gameplaySession.render(batch, shapeRenderer);
        batch.end();

        gameplaySession.renderTrails(shapeRenderer, camera.combined);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        gameplaySession.renderMuzzleFlash(shapeRenderer);

        for (Enemy currentEnemy : enemies) {
            currentEnemy.render(shapeRenderer);
        }

        shapeRenderer.end();

        if (debugHitbox && sceneLoader.getWorld() != null) {
            box2dDebugRenderer.render(
                sceneLoader.getWorld(),
                camera.combined
            );
        }

        camera.position.set(
            gameplaySession.getPlayerState().x,
            gameplaySession.getPlayerState().y + 32f,
            0f
        );

        camera.update();
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

        if (box2dDebugRenderer != null) {
            box2dDebugRenderer.dispose();
        }

        if (gameplaySession != null) {
            gameplaySession.dispose();
        }
    }
}