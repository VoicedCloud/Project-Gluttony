
package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
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
import com.github.glutton.entities.Zombie;
import com.github.glutton.entities.Ghoul;
import com.github.glutton.entities.Hound;
import com.github.glutton.player.PlayerState;
import com.github.glutton.world.GameplaySession;
import com.github.glutton.world.PhysicsConfig;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

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

    private static final float ENEMY_SPAWN_X = 200f;
    private static final float ENEMY_SPAWN_Y = 150f;
    private static final float SPAWN_SPACING = 70f;

    private static final int BOSS_MAX_HEALTH = 1500;
    private static final float BOSS_BAR_SHOW_DISTANCE = 250f;

    private static final int TOTAL_WAVES = 3;
    private static final int KILLS_REQUIRED_FOR_BOSS = 10;
    private static final float WAVE_DELAY = 2f;

    private static final float MOB_BAR_HEIGHT = 4f;
    private static final float MOB_BAR_MIN_WIDTH = 24f;
    private static final float MOB_BAR_WIDTH_FACTOR = 1.2f;

    private int currentWave = 0;
    private int totalKills = 0;

    private boolean bossSpawned = false;
    private boolean waitingForNextWave = false;
    private boolean bossDefeated = false;

    private float waveDelayTimer = 0f;

    private Enemy bossEnemy;

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

        sceneLoader = new SceneLoader(new SceneConfiguration());
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
        hudFont.getData().setScale(0.45f);

        gameplaySession = new GameplaySession(
            camera,
            "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
            "sprites/weapons/01 - Individual sprites/Bullets & Ammo/Glock - P80/Bullet.png",
            sceneLoader
        );

        startNextWave();
    }

    // --------------------------------------------------
    // WAVE MANAGEMENT
    // --------------------------------------------------

    private boolean canSpawnEnemies() {
        return sceneLoader != null
            && sceneLoader.getWorld() != null
            && !sceneLoader.getWorld().isLocked();
    }

    private float nextSpawnX() {
        float rightmostX = ENEMY_SPAWN_X;

        for (Enemy enemy : enemies) {
            rightmostX = Math.max(
                rightmostX,
                enemy.bounds.x + enemy.bounds.width
            );
        }

        return rightmostX + SPAWN_SPACING;
    }

    private void startNextWave() {
        if (bossSpawned || currentWave >= TOTAL_WAVES) {
            return;
        }

        currentWave++;
        waitingForNextWave = false;
        waveDelayTimer = 0f;

        Gdx.app.log("Waves", "Starting Wave " + currentWave);

        switch (currentWave) {
            case 1:
                // 3 Zombies.
                spawnZombieGroup(3);
                break;

            case 2:
                // 2 Zombies + 1 Ghoul.
                spawnZombieGroup(2);
                spawnGhoulGroup(1);
                break;

            case 3:
                // 2 Zombies + 1 Ghoul + 1 Hound.
                spawnZombieGroup(2);
                spawnGhoulGroup(1);
                spawnHoundGroup(1);
                break;

            default:
                break;
        }
    }

    private void spawnZombieGroup(int count) {
        if (!canSpawnEnemies()) {
            return;
        }

        for (int i = 0; i < count; i++) {
            enemies.add(new Zombie(
                sceneLoader.getWorld(),
                nextSpawnX(),
                ENEMY_SPAWN_Y,
                ENEMY_WIDTH,
                ENEMY_HEIGHT
            ));
        }

        Gdx.app.log("Waves", "Spawned " + count + " Zombie(s).");
    }

    private void spawnGhoulGroup(int count) {
        if (!canSpawnEnemies()) {
            return;
        }

        for (int i = 0; i < count; i++) {
            enemies.add(new Ghoul(
                sceneLoader.getWorld(),
                nextSpawnX(),
                ENEMY_SPAWN_Y,
                ENEMY_WIDTH,
                ENEMY_HEIGHT
            ));
        }

        Gdx.app.log("Waves", "Spawned " + count + " Ghoul(s).");
    }

    private void spawnHoundGroup(int count) {
        if (!canSpawnEnemies()) {
            return;
        }

        for (int i = 0; i < count; i++) {
            enemies.add(new Hound(
                sceneLoader.getWorld(),
                nextSpawnX(),
                ENEMY_SPAWN_Y,
                ENEMY_WIDTH,
                ENEMY_HEIGHT
            ));
        }

        Gdx.app.log("Waves", "Spawned " + count + " Hound(s).");
    }

    private void spawnBoss() {
        if (bossSpawned || !canSpawnEnemies()) {
            return;
        }

        bossSpawned = true;
        waitingForNextWave = false;
        bossDefeated = false;

        bossEnemy = new TargetDummy(
            sceneLoader.getWorld(),
            ENEMY_SPAWN_X,
            ENEMY_SPAWN_Y,
            ENEMY_WIDTH,
            ENEMY_HEIGHT,
            BOSS_MAX_HEALTH,
            5,
            250f,
            40f,
            1f
        );

        enemies.add(bossEnemy);

        Gdx.app.log(
            "Waves",
            "BOSS SPAWNED! HP: " + BOSS_MAX_HEALTH
        );
    }

    private void updateWaveProgress(float delta) {
        if (bossSpawned || bossDefeated) {
            return;
        }

        if (!enemies.isEmpty()) {
            return;
        }

        if (totalKills >= KILLS_REQUIRED_FOR_BOSS) {
            spawnBoss();
            return;
        }

        if (!waitingForNextWave) {
            waitingForNextWave = true;
            waveDelayTimer = WAVE_DELAY;

            Gdx.app.log(
                "Waves",
                "Wave " + currentWave
                    + " cleared. Next wave incoming."
            );
        }

        waveDelayTimer -= delta;

        if (waveDelayTimer <= 0f) {
            if (currentWave < TOTAL_WAVES) {
                startNextWave();
            } else if (totalKills >= KILLS_REQUIRED_FOR_BOSS) {
                spawnBoss();
            } else {
                Gdx.app.error(
                    "Waves",
                    "All tutorial waves ended with "
                        + totalKills + " kills."
                );
            }
        }
    }

    // --------------------------------------------------
    // MAIN GAME LOOP
    // --------------------------------------------------

    @Override
    public void render(float delta) {
        if (camera == null || viewport == null || batch == null) {
            return;
        }

        float safeDelta = Math.min(Math.max(delta, 0f), 0.1f);

        Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            debugHitbox = !debugHitbox;
        }

        PlayerState playerState = null;

        if (gameplaySession != null) {
            gameplaySession.update(safeDelta, enemies);
            playerState = gameplaySession.getPlayerState();
        }

        // Remove defeated enemies while Box2D is unlocked.
        if (sceneLoader != null
                && sceneLoader.getWorld() != null
                && !sceneLoader.getWorld().isLocked()) {

            Iterator<Enemy> iterator = enemies.iterator();

            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();

                if (enemy.isDestroyed()) {
                    boolean wasBoss = enemy == bossEnemy;

                    enemy.disposeBody();
                    iterator.remove();

                    if (wasBoss) {
                        bossDefeated = true;
                        Gdx.app.log("Waves", "BOSS DEFEATED!");
                    } else {
                        totalKills++;

                        Gdx.app.log(
                            "Waves",
                            "Enemy defeated! Kills: "
                                + totalKills + "/"
                                + KILLS_REQUIRED_FOR_BOSS
                        );
                    }
                }
            }
        }

        updateWaveProgress(safeDelta);

        // Physics update.
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

            for (Enemy enemy : enemies) {
                enemy.syncBounds();
            }
        }

        // Enemy AI and attacks.
        if (playerState != null) {
            float playerX = playerState.x;
            float playerY = playerState.y;

            for (Enemy enemy : enemies) {
                enemy.updateAI(safeDelta, playerState);

                if (enemy.canAttack(playerX, playerY)) {
                    int damage;

                    if (enemy == bossEnemy) {
                        damage = ThreadLocalRandom.current()
                            .nextInt(5, 11);
                    } else {
                        damage = enemy.getAttackDamage();
                    }

                    playerState.takeDamage(damage);

                    Gdx.app.log(
                        "Enemy",
                        "Damage: " + damage
                            + " | Player HP: "
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

        camera.update();
        viewport.apply();

        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;

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

        // Player, weapons, bullets, and enemy bodies.
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
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            for (Enemy enemy : enemies) {
                enemy.render(shapeRenderer);
            }

            // Draw health bars after the enemy bodies.
            renderMobHealthBars();

            gameplaySession.renderMuzzleFlash(shapeRenderer);

            shapeRenderer.end();

            // Draw numeric HP labels after the shapes.
            renderMobHealthLabels();
        }

        // HUD.
        if (playerState != null) {
            renderPlayerHealth(playerState, viewWidth, viewHeight);
            renderWaveStatus(viewWidth, viewHeight);
            renderBossHealth(playerState, viewWidth, viewHeight);
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

    // --------------------------------------------------
    // MOB HEALTH BARS
    // --------------------------------------------------

    private void renderMobHealthBars() {
        for (Enemy enemy : enemies) {
            // The boss has its own dedicated health bar.
            if (enemy == bossEnemy
                    || enemy.isDestroyed()
                    || enemy.isBodyDisposed()) {
                continue;
            }

            float barWidth = Math.max(
                MOB_BAR_MIN_WIDTH,
                boundsWidthForHealthBar(enemy)
            );

            float barHeight = MOB_BAR_HEIGHT;

            float barX = enemy.bounds.x
                + (enemy.bounds.width - barWidth) / 2f;

            float barY = enemy.bounds.y
                + enemy.bounds.height + 12f;

            float hpPercent = enemy.getMaxHealth() > 0
                ? (float) enemy.getHealth() / enemy.getMaxHealth()
                : 0f;

            hpPercent = Math.max(0f, Math.min(1f, hpPercent));

            // Background.
            shapeRenderer.setColor(0.10f, 0.10f, 0.10f, 1f);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            // HP color changes with remaining health.
            if (hpPercent > 0f) {
                if (hpPercent > 0.5f) {
                    shapeRenderer.setColor(
                        0.15f, 0.85f, 0.2f, 1f
                    );
                } else if (hpPercent > 0.25f) {
                    shapeRenderer.setColor(
                        1f, 0.65f, 0.05f, 1f
                    );
                } else {
                    shapeRenderer.setColor(
                        0.9f, 0.12f, 0.12f, 1f
                    );
                }

                shapeRenderer.rect(
                    barX,
                    barY,
                    barWidth * hpPercent,
                    barHeight
                );
            }
        }
    }

    private float boundsWidthForHealthBar(Enemy enemy) {
        return enemy.bounds.width * 1.2f;
    }

    private void renderMobHealthLabels() {
        if (hudFont == null || batch == null) {
            return;
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(1f, 1f, 1f, 1f);

        for (Enemy enemy : enemies) {
            // Keep the boss health display separate.
            if (enemy == bossEnemy
                    || enemy.isDestroyed()
                    || enemy.isBodyDisposed()) {
                continue;
            }

            String hpText = enemy.getHealth() + " HP";

            GlyphLayout layout = new GlyphLayout();
            layout.setText(hudFont, hpText);

            float x = enemy.bounds.x
                + enemy.bounds.width / 2f
                - layout.width / 2f;

            float y = enemy.bounds.y
                + enemy.bounds.height + 25f;

            hudFont.draw(batch, hpText, x, y);
        }

        batch.end();
    }

    // --------------------------------------------------
    // PLAYER HUD
    // --------------------------------------------------

    private void renderPlayerHealth(
            PlayerState player,
            float viewWidth,
            float viewHeight
    ) {
        float left =
            camera.position.x - viewWidth / 2f + 12f;

        float top =
            camera.position.y + viewHeight / 2f - 16f;

        float barWidth = 65f;
        float barHeight = 4f;

        float hpPercentage = player.maxHealth > 0
            ? (float) player.health / player.maxHealth
            : 0f;

        hpPercentage = Math.max(0f, Math.min(1f, hpPercentage));

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 1f);
        shapeRenderer.rect(
            left, top - barHeight, barWidth, barHeight
        );

        if (hpPercentage > 0f) {
            if (hpPercentage > 0.5f) {
                shapeRenderer.setColor(0.15f, 0.85f, 0.2f, 1f);
            } else if (hpPercentage > 0.25f) {
                shapeRenderer.setColor(1f, 0.65f, 0.05f, 1f);
            } else {
                shapeRenderer.setColor(0.9f, 0.12f, 0.12f, 1f);
            }

            shapeRenderer.rect(
                left,
                top - barHeight,
                barWidth * hpPercentage,
                barHeight
            );
        }

        shapeRenderer.end();

        int ammo = Math.max(0, player.weaponState.currentAmmo);
        int maxAmmo = player.weaponState.activeWeapon.maxAmmo;

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(1f, 1f, 1f, 1f);

        hudFont.draw(
            batch,
            "HP: " + player.health + " / " + player.maxHealth,
            left,
            top + 1f
        );

        hudFont.draw(
            batch,
            "AMMO: " + ammo + " / " + maxAmmo,
            left,
            top - 15f
        );

        if (player.weaponState.isReloading) {
            hudFont.draw(
                batch,
                "RELOADING...",
                left,
                top - 27f
            );
        }

        batch.end();
    }

    // --------------------------------------------------
    // WAVE HUD
    // --------------------------------------------------

    private void renderWaveStatus(
            float viewWidth,
            float viewHeight
    ) {
        String status;

        if (bossDefeated) {
            status = "BOSS DEFEATED!";
        } else if (bossSpawned) {
            status = "BOSS FIGHT!";
        } else if (waitingForNextWave) {
            status = "WAVE CLEARED - NEXT WAVE SOON";
        } else {
            status = "WAVE " + currentWave
                + " / " + TOTAL_WAVES
                + "   KILLS: " + totalKills
                + " / " + KILLS_REQUIRED_FOR_BOSS;
        }

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        GlyphLayout layout = new GlyphLayout();
        layout.setText(hudFont, status);

        float x = camera.position.x - layout.width / 2f;
        float y = camera.position.y + viewHeight / 2f - 16f;

        hudFont.setColor(1f, 1f, 1f, 1f);
        hudFont.draw(batch, status, x, y);

        batch.end();
    }

    // --------------------------------------------------
    // BOSS HEALTH BAR
    // --------------------------------------------------

    private void renderBossHealth(
            PlayerState player,
            float viewWidth,
            float viewHeight
    ) {
        if (!bossSpawned
                || bossEnemy == null
                || bossEnemy.isDestroyed()) {
            return;
        }

        float enemyCenterX =
            bossEnemy.bounds.x + bossEnemy.bounds.width / 2f;

        float enemyCenterY =
            bossEnemy.bounds.y + bossEnemy.bounds.height / 2f;

        float dx = player.x - enemyCenterX;
        float dy = player.y - enemyCenterY;

        if (dx * dx + dy * dy
                > BOSS_BAR_SHOW_DISTANCE * BOSS_BAR_SHOW_DISTANCE) {
            return;
        }

        float barWidth = Math.min(180f, viewWidth * 0.55f);
        float barHeight = 5f;

        float barX = camera.position.x - barWidth / 2f;
        float barY = camera.position.y + viewHeight / 2f - 35f;

        float hpPercent = bossEnemy.getMaxHealth() > 0
            ? (float) bossEnemy.getHealth()
                / bossEnemy.getMaxHealth()
            : 0f;

        hpPercent = Math.max(0f, Math.min(1f, hpPercent));

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.05f, 0.02f, 0.08f, 1f);
        shapeRenderer.rect(
            barX - 2f, barY - 2f,
            barWidth + 4f, barHeight + 4f
        );

        shapeRenderer.setColor(0.18f, 0.08f, 0.18f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        if (hpPercent > 0f) {
            shapeRenderer.setColor(0.65f, 0.08f, 0.35f, 1f);
            shapeRenderer.rect(
                barX, barY, barWidth * hpPercent, barHeight
            );
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(1f, 1f, 1f, 1f);

        String bossName = "BOSS";
        String healthText =
            bossEnemy.getHealth() + " / " + bossEnemy.getMaxHealth();

        GlyphLayout layout = new GlyphLayout();
        layout.setText(hudFont, bossName);

        hudFont.draw(
            batch,
            bossName,
            camera.position.x - layout.width / 2f,
            barY + barHeight + 10f
        );

        layout.setText(hudFont, healthText);

        hudFont.draw(
            batch,
            healthText,
            camera.position.x - layout.width / 2f,
            barY - 5f
        );

        batch.end();
    }

    // --------------------------------------------------
    // SCREEN LIFECYCLE
    // --------------------------------------------------

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }
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
        if (sceneLoader != null
                && sceneLoader.getWorld() != null
                && !sceneLoader.getWorld().isLocked()) {
            for (Enemy enemy : enemies) {
                enemy.disposeBody();
            }
        }

        enemies.clear();
        bossEnemy = null;

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
