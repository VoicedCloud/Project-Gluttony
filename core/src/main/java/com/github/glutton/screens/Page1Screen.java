package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;
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
import com.github.glutton.weapons.WeaponProfile;
import com.github.glutton.world.GameplaySession;
import com.github.glutton.world.PhysicsConfig;

import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import com.github.glutton.player.PlayerController;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
import java.util.Iterator;
import java.util.List;

public class Page1Screen implements Screen {

    private final GluttonGame game;
    private final List<Enemy> enemies = new ArrayList<>();

    private SceneLoader sceneLoader;
    private Viewport viewport;
    private OrthographicCamera camera;
    private OrthographicCamera hudCamera;

    private final Map<WeaponProfile, Texture> hudWeaponTextures =
        new EnumMap<>(WeaponProfile.class);

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer box2dDebugRenderer;
    private BitmapFont hudFont;
    private ParallaxBackground background;

    private boolean debugHitbox = false;
    private GameplaySession gameplaySession;

    private static final int MAX_DEATHS = 3;
    private int deathCount = 0;
    private boolean gameOver = false;

    private final Rectangle retryButtonBounds = new Rectangle();
    private final Rectangle menuButtonBounds = new Rectangle();
    private final Vector2 gameOverTouch = new Vector2();

    private static final float ENEMY_WIDTH = 9.6f;
    private static final float ENEMY_HEIGHT = 19.2f;
    private static final float ENEMY_SPAWN_X = 200f;
    private static final float ENEMY_SPAWN_Y = 150f;

    private static final int BOSS_MAX_HEALTH = 1500;
    private static final int BOSS_BASE_DAMAGE = 5;

    private static final int TOTAL_WAVES = 6;
    private static final float BUY_TIME_SECONDS = 15f;
    private static final int SHOTGUN_COST = 200;
    private static final int AK_COST = 180;

    private int currentWave = 0;
    private int totalKills = 0;
    private int currency = 0;

    private boolean bossSpawned = false;
    private boolean waitingForNextWave = false;
    private boolean shopOpen = false;

    private float waveDelayTimer = 0f;
    private String shopMessage = "";

    public Page1Screen(GluttonGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.input.setCursorCatched(false);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

        camera = new OrthographicCamera();
        camera.zoom = 0.25f;

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(
            false,
            Gdx.graphics.getWidth(),
            Gdx.graphics.getHeight()
        );

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
        hudFont.getData().setScale(0.85f);

        for (WeaponProfile weapon : WeaponProfile.values()) {
            try {
                hudWeaponTextures.put(
                    weapon,
                    new Texture(Gdx.files.internal(weapon.idleTexturePath))
                );
            } catch (Exception missingWeaponSprite) {
                try {
                    hudWeaponTextures.put(
                        weapon,
                        new Texture(
                            Gdx.files.internal("sprites/weapons/Glock - P80.png")
                        )
                    );
                } catch (Exception missingFallback) {
                    Gdx.app.error(
                        "HUD",
                        "Could not load HUD weapon sprite for "
                            + weapon.displayName,
                        missingFallback
                    );
                }
            }
        }

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

    private void startNextWave() {
        if (bossSpawned || currentWave >= TOTAL_WAVES) {
            return;
        }

        currentWave++;
        waitingForNextWave = false;
        shopOpen = false;
        waveDelayTimer = 0f;
        shopMessage = "";

        Gdx.app.log("Waves", "Starting Wave " + currentWave);

        switch (currentWave) {
                    case 1:
             spawnMixedWave(10, 6, 3, 1);
             break;
            case 2:
                spawnMixedWave(10, 6, 3, 1);
                break;

            case 3:
                spawnMixedWave(10, 5, 3, 2);
                break;

            case 4:
                spawnMixedWave(15, 8, 4, 3);
                break;

            case 5:
                spawnMixedWave(20, 10, 5, 5);
                break;

            case 6:
                spawnBoss();
                break;

            default:
                break;
        }
    }

    private void spawnMixedWave(
            int total,
            int zombies,
            int ghouls,
            int hounds
    ) {
        spawnMobGroup(zombies, "zombie");
        spawnMobGroup(ghouls, "ghoul");
        spawnMobGroup(hounds, "hound");

        int remaining = total - zombies - ghouls - hounds;

        if (remaining > 0) {
            spawnMobGroup(remaining, "zombie");
        }
    }

    private void spawnMobGroup(int count, String enemyType) {
        if (sceneLoader == null || sceneLoader.getWorld() == null) {
            return;
        }

        for (int i = 0; i < count; i++) {
            float spawnX = ENEMY_SPAWN_X + i * 70f;
            float spawnY = ENEMY_SPAWN_Y;

            Enemy enemy = createEnemy(enemyType, spawnX, spawnY);

            if (enemy != null) {
                enemies.add(enemy);
            }
        }

        Gdx.app.log(
            "Waves",
            "Spawned " + count + " " + enemyType
                + "(s). Total kills: " + totalKills
        );
    }

    private Enemy createEnemy(String enemyType, float x, float y) {
        switch (enemyType.toLowerCase()) {
            case "zombie":
                return new Zombie(
                    sceneLoader.getWorld(),
                    x,
                    y,
                    ENEMY_WIDTH,
                    ENEMY_HEIGHT
                );

            case "ghoul":
                return new Ghoul(
                    sceneLoader.getWorld(),
                    x,
                    y,
                    ENEMY_WIDTH,
                    ENEMY_HEIGHT
                );

            case "hound":
                return new Hound(
                    sceneLoader.getWorld(),
                    x,
                    y,
                    ENEMY_WIDTH,
                    ENEMY_HEIGHT
                );

            default:
                Gdx.app.error("Waves", "Unknown enemy type: " + enemyType);
                return null;
        }
    }

    private void spawnBoss() {
        if (bossSpawned
                || sceneLoader == null
                || sceneLoader.getWorld() == null) {
            return;
        }

        bossSpawned = true;
        waitingForNextWave = false;
        shopOpen = false;

        float bossWidth = ENEMY_WIDTH * 1.5f;
        float bossHeight = ENEMY_HEIGHT * 1.5f;

        TargetDummy boss = new TargetDummy(
            sceneLoader.getWorld(),
            ENEMY_SPAWN_X,
            ENEMY_SPAWN_Y,
            bossWidth,
            bossHeight,
            BOSS_MAX_HEALTH,
            BOSS_BASE_DAMAGE,
            250f,
            40f,
            1f
        );

        enemies.add(boss);

        Gdx.app.log("Waves", "BOSS SPAWNED! Prepare for the fight!");
    }

    private void updateWaveProgress(float delta) {
        if (bossSpawned || !enemies.isEmpty()) {
            return;
        }

        if (!waitingForNextWave) {
            if (currentWave >= TOTAL_WAVES) {
                return;
            }

            waitingForNextWave = true;
            shopOpen = true;
            waveDelayTimer = BUY_TIME_SECONDS;

            shopMessage =
                "Wave cleared! [1] Shotgun $200  [2] AK-47 $180";

            Gdx.app.log(
                "Waves",
                "Wave " + currentWave + " cleared. Shop open."
            );
        }

        if (shopOpen) {
            handleShopInput();

            waveDelayTimer -= delta;

            if (waveDelayTimer <= 0f) {
                startNextWave();
            }
        }
    }

    private void handleShopInput() {
        if (gameplaySession == null) {
            return;
        }

        PlayerState player = gameplaySession.getPlayerState();

        if (player == null || player.weaponState == null) {
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            if (player.weaponState.ownedWeapons.contains(
                    WeaponProfile.SHOTGUN)) {
                equipWeapon(player, WeaponProfile.SHOTGUN);
                shopMessage = "Shotgun equipped!";
            } else if (currency >= SHOTGUN_COST) {
                currency -= SHOTGUN_COST;
                player.weaponState.ownedWeapons.add(WeaponProfile.SHOTGUN);

                equipWeapon(player, WeaponProfile.SHOTGUN);
                shopMessage = "Shotgun purchased and equipped!";
            } else {
                shopMessage = "Not enough currency for the Shotgun.";
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            if (player.weaponState.ownedWeapons.contains(
                    WeaponProfile.ASSAULT_RIFLE)) {
                equipWeapon(player, WeaponProfile.ASSAULT_RIFLE);
                shopMessage = "AK-47 equipped!";
            } else if (currency >= AK_COST) {
                currency -= AK_COST;
                player.weaponState.ownedWeapons.add(
                    WeaponProfile.ASSAULT_RIFLE
                );

                equipWeapon(player, WeaponProfile.ASSAULT_RIFLE);
                shopMessage = "AK-47 purchased and equipped!";
            } else {
                shopMessage = "Not enough currency for the AK-47.";
            }
        }
    }

    private void equipWeapon(PlayerState player, WeaponProfile weapon) {
        player.weaponState.activeWeapon = weapon;
        player.weaponState.currentAmmo = weapon.maxAmmo;
        player.weaponState.isReloading = false;
    }

    private int getEnemyReward(Enemy enemy) {
        if (enemy instanceof Hound) {
            return 13;
        }

        if (enemy instanceof Zombie || enemy instanceof Ghoul) {
            return 10;
        }

        return 0;
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

        if (!gameOver && Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
            debugHitbox = !debugHitbox;
        }

        PlayerState playerState = null;

        if (gameplaySession != null) {
            playerState = gameplaySession.getPlayerState();

            if (!gameOver) {
                gameplaySession.update(safeDelta, enemies);
                playerState = gameplaySession.getPlayerState();
            }
        }

        // Remove defeated enemies and award currency.
        if (!gameOver
                && sceneLoader != null
                && sceneLoader.getWorld() != null
                && !sceneLoader.getWorld().isLocked()) {

            Iterator<Enemy> iterator = enemies.iterator();

            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();

                if (enemy.isDestroyed()) {
                    int reward = getEnemyReward(enemy);
                    currency += reward;
                    totalKills++;

                    Gdx.app.log(
                        "Rewards",
                        "Enemy defeated! +" + reward
                            + " currency. Total: " + currency
                    );

                    enemy.disposeBody();
                    iterator.remove();
                }
            }
        }

        if (!gameOver) {
            updateWaveProgress(safeDelta);
        }

        // Freeze physics after Game Over.
        if (!gameOver
                && sceneLoader != null
                && sceneLoader.getWorld() != null) {

            float subDelta =
                safeDelta / PhysicsConfig.PHYSICS_SUBSTEPS;

            for (int i = 0; i < PhysicsConfig.PHYSICS_SUBSTEPS; i++) {
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
        if (playerState != null && !gameOver) {
            float playerX = playerState.x;
            float playerY = playerState.y;

            for (Enemy enemy : enemies) {
                enemy.updateAI(safeDelta, playerState);

                if (enemy.canAttack(playerX, playerY)) {
                    int damage = enemy.getAttackDamage();
                    playerState.takeDamage(damage);
                }
            }

            handlePlayerDeath(playerState);

            camera.position.set(
                playerState.x,
                playerState.y + 32f,
                0f
            );
        }

        camera.update();
        viewport.apply();

        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;

        float cameraLeftEdge = camera.position.x - viewWidth / 2f;
        float cameraBottomEdge = camera.position.y - viewHeight / 2f;

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
        if (!gameOver
                && sceneLoader != null
                && sceneLoader.getEngine() != null) {

            sceneLoader.getEngine().setDelta(safeDelta);
            sceneLoader.getEngine().process();
        }

        // Player, weapons, bullets, and enemies.
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

            gameplaySession.renderMuzzleFlash(shapeRenderer);

            for (Enemy enemy : enemies) {
                enemy.render(shapeRenderer);
            }

            shapeRenderer.end();

            renderEnemyHealthBars();
        }

        if (playerState != null) {
            renderDoomHud(playerState);
            renderWaveStatus(viewWidth, viewHeight);
            renderBossHealth(playerState, viewWidth, viewHeight);
        }

        if (debugHitbox
                && !gameOver
                && sceneLoader != null
                && sceneLoader.getWorld() != null) {

            box2dDebugRenderer.render(
                sceneLoader.getWorld(),
                camera.combined
            );
        }

        if (gameOver) {
            renderGameOverOverlay(viewWidth, viewHeight);
            handleGameOverInput();
        }
    }

    // --------------------------------------------------
    // ENEMY HEALTH BARS
    // --------------------------------------------------

    private void renderEnemyHealthBars() {
        if (enemies.isEmpty()) {
            return;
        }

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Enemy enemy : enemies) {
            if (enemy.isDestroyed() || enemy.isBodyDisposed()) {
                continue;
            }

            float barWidth = enemy.bounds.width;
            float barHeight = 3f;
            float barX = enemy.bounds.x;
            float barY = enemy.bounds.y + enemy.bounds.height + 5f;

            float hpPercent = enemy.getMaxHealth() > 0
                ? (float) enemy.getHealth() / enemy.getMaxHealth()
                : 0f;

            hpPercent = Math.max(0f, Math.min(1f, hpPercent));

            shapeRenderer.setColor(0.12f, 0.12f, 0.12f, 1f);
            shapeRenderer.rect(barX, barY, barWidth, barHeight);

            if (hpPercent > 0f) {
                if (hpPercent > 0.5f) {
                    shapeRenderer.setColor(0.12f, 0.85f, 0.2f, 1f);
                } else if (hpPercent > 0.25f) {
                    shapeRenderer.setColor(1f, 0.65f, 0.05f, 1f);
                } else {
                    shapeRenderer.setColor(0.9f, 0.1f, 0.1f, 1f);
                }

                shapeRenderer.rect(
                    barX,
                    barY,
                    barWidth * hpPercent,
                    barHeight
                );
            }
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Enemy enemy : enemies) {
            if (enemy.isDestroyed() || enemy.isBodyDisposed()) {
                continue;
            }

            String hpText =
                enemy.getHealth() + " / " + enemy.getMaxHealth();

            GlyphLayout layout = new GlyphLayout(hudFont, hpText);

            float textX = enemy.bounds.x
                + enemy.bounds.width / 2f
                - layout.width / 2f;

            float textY = enemy.bounds.y
                + enemy.bounds.height + 14f;

            hudFont.setColor(Color.WHITE);
            hudFont.draw(batch, hpText, textX, textY);
        }

        batch.end();
    }

    // --------------------------------------------------
    // PLAYER DEATH / GAME OVER
    // --------------------------------------------------

    private void handlePlayerDeath(PlayerState player) {
        if (player == null || !player.isDead() || gameOver) {
            return;
        }

        deathCount++;

        if (deathCount >= MAX_DEATHS) {
            gameOver = true;

            Gdx.input.setCursorCatched(false);
            Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
            return;
        }

        player.respawn();
        player.x = PhysicsConfig.PLAYER_SPAWN_X;
        player.y = PhysicsConfig.PLAYER_SPAWN_Y;
        player.velocityY = 0f;
        player.isGrounded = false;
        player.canDoubleJump = true;
        player.movementState = PlayerState.MovementState.IDLE;
        player.jumpStartY = player.y;
        player.peakY = player.y;
    }

    private void renderGameOverOverlay(
            float viewWidth,
            float viewHeight
    ) {
        float centerX = camera.position.x;
        float centerY = camera.position.y;
        float left = centerX - viewWidth / 2f;
        float bottom = centerY - viewHeight / 2f;

        retryButtonBounds.set(
            centerX - 48f,
            centerY - 12f,
            96f,
            22f
        );

        menuButtonBounds.set(
            centerX - 48f,
            centerY - 42f,
            96f,
            22f
        );

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(left, bottom, viewWidth, viewHeight);

        shapeRenderer.setColor(0.65f, 0.08f, 0.08f, 1f);
        shapeRenderer.rect(
            retryButtonBounds.x,
            retryButtonBounds.y,
            retryButtonBounds.width,
            retryButtonBounds.height
        );

        shapeRenderer.setColor(0.12f, 0.30f, 0.62f, 1f);
        shapeRenderer.rect(
            menuButtonBounds.x,
            menuButtonBounds.y,
            menuButtonBounds.width,
            menuButtonBounds.height
        );

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(Color.WHITE);
        drawCenteredText("GAME OVER", centerX, centerY + 25f);

        drawCenteredText(
            "You died " + deathCount + " times",
            centerX,
            centerY + 10f
        );

        drawCenteredText(
            "RETRY",
            centerX,
            retryButtonBounds.y + 14f
        );

        drawCenteredText(
            "MENU",
            centerX,
            menuButtonBounds.y + 14f
        );

        batch.end();
    }

    private void drawCenteredText(
            String text,
            float centerX,
            float y
    ) {
        GlyphLayout layout = new GlyphLayout(hudFont, text);

        hudFont.draw(
            batch,
            text,
            centerX - layout.width / 2f,
            y
        );
    }

    private void handleGameOverInput() {
        Gdx.input.setCursorCatched(false);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

        if (!Gdx.input.justTouched()) {
            return;
        }

        gameOverTouch.set(Gdx.input.getX(), Gdx.input.getY());
        viewport.unproject(gameOverTouch);

        if (retryButtonBounds.contains(gameOverTouch)) {
            dispose();
            game.setScreen(new Page1Screen(game));
        } else if (menuButtonBounds.contains(gameOverTouch)) {
            dispose();
            game.setScreen(new MainMenuScreen(game));
        }
    }

    // --------------------------------------------------
    // DOOM-STYLE PLAYER HUD
    // --------------------------------------------------

    private void renderDoomHud(PlayerState player) {
        if (player == null || batch == null || shapeRenderer == null) {
            return;
        }

        int screenWidth = Gdx.graphics.getWidth();
        int screenHeight = Gdx.graphics.getHeight();

        hudCamera.setToOrtho(false, screenWidth, screenHeight);
        hudCamera.update();

        float hudHeight = Math.max(105f, screenHeight * 0.19f);
        float centerX = screenWidth / 2f;

        float healthPercent = player.maxHealth > 0
            ? (float) player.health / player.maxHealth
            : 0f;

        healthPercent = Math.max(0f, Math.min(1f, healthPercent));

        int ammo = Math.max(0, player.weaponState.currentAmmo);
        int maxAmmo = player.weaponState.activeWeapon.maxAmmo;

        String weaponName = player.weaponState.activeWeapon.displayName;
        
       PlayerController controller = gameplaySession != null
        ? gameplaySession.getPlayerController()
        : null;

float dashCooldown = controller != null
        ? controller.getDashCooldownRemaining()
        : 0f;

float pushCooldown = controller != null
        ? controller.getPushCooldownRemaining()
        : 0f;

String dashText = dashCooldown <= 0f
        ? "READY"
        : String.format(java.util.Locale.US, "%.1fs", dashCooldown);

String pushText = pushCooldown <= 0f
        ? "READY"
        : String.format(java.util.Locale.US, "%.1fs", pushCooldown);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        );

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.025f, 0.025f, 0.035f, 0.98f);
        shapeRenderer.rect(0f, 0f, screenWidth, hudHeight);

        shapeRenderer.setColor(0.35f, 0.30f, 0.22f, 1f);
        shapeRenderer.rect(0f, hudHeight - 4f, screenWidth, 4f);

        float leftPanelX = screenWidth * 0.04f;
        float healthBarWidth = screenWidth * 0.18f;
        float healthBarHeight = 13f;
        float healthBarY = hudHeight * 0.55f;

        shapeRenderer.setColor(0.13f, 0.10f, 0.10f, 1f);
        shapeRenderer.rect(
            leftPanelX,
            healthBarY,
            healthBarWidth,
            healthBarHeight
        );

        if (healthPercent > 0f) {
            if (healthPercent > 0.5f) {
                shapeRenderer.setColor(0.1f, 0.75f, 0.18f, 1f);
            } else if (healthPercent > 0.25f) {
                shapeRenderer.setColor(0.95f, 0.55f, 0.05f, 1f);
            } else {
                shapeRenderer.setColor(0.9f, 0.08f, 0.08f, 1f);
            }

            shapeRenderer.rect(
                leftPanelX,
                healthBarY,
                healthBarWidth * healthPercent,
                healthBarHeight
            );
        }

        float portraitSize = Math.min(88f, hudHeight * 0.75f);
        float portraitX = centerX - portraitSize / 2f;
        float portraitY = hudHeight * 0.08f;

        shapeRenderer.setColor(0.20f, 0.18f, 0.15f, 1f);
        shapeRenderer.rect(
            portraitX - 5f,
            portraitY - 5f,
            portraitSize + 10f,
            portraitSize + 10f
        );

        shapeRenderer.setColor(0.08f, 0.08f, 0.08f, 1f);
        shapeRenderer.rect(
            portraitX,
            portraitY,
            portraitSize,
            portraitSize
        );

        shapeRenderer.setColor(0.68f, 0.48f, 0.34f, 1f);
        shapeRenderer.circle(
            centerX,
            portraitY + portraitSize * 0.57f,
            portraitSize * 0.27f
        );

        shapeRenderer.setColor(0.12f, 0.10f, 0.10f, 1f);
        shapeRenderer.rect(
            centerX - portraitSize * 0.25f,
            portraitY + portraitSize * 0.29f,
            portraitSize * 0.50f,
            portraitSize * 0.12f
        );

        float rightPanelX = screenWidth * 0.76f;
        float ammoBarWidth = screenWidth * 0.19f;

        float ammoPercent = maxAmmo > 0
            ? (float) ammo / maxAmmo
            : 0f;

        ammoPercent = Math.max(0f, Math.min(1f, ammoPercent));

        shapeRenderer.setColor(0.13f, 0.10f, 0.10f, 1f);
        shapeRenderer.rect(
            rightPanelX,
            healthBarY,
            ammoBarWidth,
            healthBarHeight
        );

        if (ammoPercent > 0f) {
            shapeRenderer.setColor(0.88f, 0.62f, 0.12f, 1f);
            shapeRenderer.rect(
                rightPanelX,
                healthBarY,
                ammoBarWidth * ammoPercent,
                healthBarHeight
            );
        }

        
        // ==================================================
// DASH AND PUSH HUD PANELS
// ==================================================

float skillPanelWidth = Math.min(145f, screenWidth * 0.13f);
float skillPanelHeight = 45f;
float skillPanelY = 12f;

float dashPanelX = screenWidth * 0.27f;
float pushPanelX = screenWidth * 0.61f;

float dashProgress = Math.max(
        0f,
        Math.min(1f, 1f - dashCooldown / 1.5f)
);

float pushProgress = Math.max(
        0f,
        Math.min(1f, 1f - pushCooldown / 4f)
);

// Dash panel background.
shapeRenderer.setColor(0.08f, 0.08f, 0.10f, 1f);
shapeRenderer.rect(
        dashPanelX,
        skillPanelY,
        skillPanelWidth,
        skillPanelHeight
);

// Dash accent.
shapeRenderer.setColor(0.95f, 0.65f, 0.12f, 1f);
shapeRenderer.rect(
        dashPanelX,
        skillPanelY + skillPanelHeight - 3f,
        skillPanelWidth,
        3f
);

// Dash cooldown bar background.
shapeRenderer.setColor(0.18f, 0.18f, 0.20f, 1f);
shapeRenderer.rect(
        dashPanelX + 8f,
        skillPanelY + 5f,
        skillPanelWidth - 16f,
        4f
);

// Dash cooldown bar.
if (dashProgress > 0f) {
    shapeRenderer.setColor(0.95f, 0.65f, 0.12f, 1f);
    shapeRenderer.rect(
            dashPanelX + 8f,
            skillPanelY + 5f,
            (skillPanelWidth - 16f) * dashProgress,
            4f
    );
}

// Push panel background.
shapeRenderer.setColor(0.08f, 0.08f, 0.10f, 1f);
shapeRenderer.rect(
        pushPanelX,
        skillPanelY,
        skillPanelWidth,
        skillPanelHeight
);

// Push accent.
shapeRenderer.setColor(0.85f, 0.18f, 0.12f, 1f);
shapeRenderer.rect(
        pushPanelX,
        skillPanelY + skillPanelHeight - 3f,
        skillPanelWidth,
        3f
);

// Push cooldown bar.
if (pushProgress > 0f) {
    shapeRenderer.setColor(0.85f, 0.18f, 0.12f, 1f);
    shapeRenderer.rect(
        pushPanelX + 8f,
        skillPanelY + 5f,
        (skillPanelWidth - 16f) * pushProgress,
        4f
    );
}

shapeRenderer.end();

Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        hudFont.setColor(1f, 0.9f, 0.65f, 1f);
        hudFont.draw(batch, "HEALTH", leftPanelX, healthBarY + 33f);

        hudFont.draw(
            batch,
            player.health + " / " + player.maxHealth,
            leftPanelX,
            healthBarY - 9f
        );

        hudFont.draw(
            batch,
            "DEATHS " + deathCount + " / " + MAX_DEATHS,
            leftPanelX,
            18f
        );

        hudFont.draw(batch, "AMMO", rightPanelX, healthBarY + 33f);

        hudFont.draw(
            batch,
            ammo + " / " + maxAmmo,
            rightPanelX,
            healthBarY - 9f
        );

        GlyphLayout weaponLayout = new GlyphLayout(hudFont, weaponName);

        hudFont.draw(
            batch,
            weaponName,
            centerX - weaponLayout.width / 2f,
            hudHeight - 6f
        );

        Texture weaponTexture =
            hudWeaponTextures.get(player.weaponState.activeWeapon);

        if (weaponTexture != null) {
            float weaponWidth = Math.min(112f, screenWidth * 0.13f);
            float weaponHeight = weaponWidth * 0.75f;

            batch.draw(
                weaponTexture,
                screenWidth - weaponWidth - 24f,
                15f,
                weaponWidth,
                weaponHeight
            );
        }

        if (player.weaponState.isReloading) {
            GlyphLayout reloadLayout =
                new GlyphLayout(hudFont, "RELOADING...");

            hudFont.setColor(1f, 0.25f, 0.15f, 1f);

            hudFont.draw(
                batch,
                "RELOADING...",
                centerX - reloadLayout.width / 2f,
                hudHeight * 0.45f
            );
        }

        // Currency display.
        hudFont.setColor(1f, 0.85f, 0.2f, 1f);
        hudFont.draw(
            batch,
            "CASH: $" + currency,
            centerX - 45f,
            hudHeight + 22f
        );
// ==================================================
// DASH AND PUSH HUD TEXT
// ==================================================

hudFont.setColor(
        dashCooldown <= 0f ? Color.GREEN : Color.WHITE
);

hudFont.draw(
        batch,
        "Q  DASH",
        dashPanelX + 8f,
        skillPanelY + 34f
);

hudFont.draw(
        batch,
        dashText,
        dashPanelX + 8f,
        skillPanelY + 19f
);

hudFont.setColor(
        pushCooldown <= 0f ? Color.GREEN : Color.WHITE
);

hudFont.draw(
        batch,
        "F  PUSH",
        pushPanelX + 8f,
        skillPanelY + 34f
);

hudFont.draw(
        batch,
        pushText,
        pushPanelX + 8f,
        skillPanelY + 19f
);

hudFont.setColor(Color.WHITE);
        batch.end();

        batch.setProjectionMatrix(camera.combined);
        shapeRenderer.setProjectionMatrix(camera.combined);
    }
        // --------------------------------------------------
    // WAVE HUD
    // --------------------------------------------------

    private void renderWaveStatus(
            float viewWidth,
            float viewHeight
    ) {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(Color.WHITE);

        String waveText = bossSpawned
            ? "BOSS FIGHT"
            : "WAVE " + currentWave + " / " + TOTAL_WAVES;

        GlyphLayout layout = new GlyphLayout(hudFont, waveText);

        hudFont.draw(
            batch,
            waveText,
            camera.position.x - layout.width / 2f,
            camera.position.y + viewHeight / 2f - 20f
        );

        String currencyText = "CURRENCY: $" + currency;

        hudFont.draw(
            batch,
            currencyText,
            camera.position.x - viewWidth / 2f + 12f,
            camera.position.y + viewHeight / 2f - 20f
        );

        if (shopOpen) {
            float secondsLeft = Math.max(0f, waveDelayTimer);

            String timerText =
                "SHOP BREAK: " + (int) Math.ceil(secondsLeft) + "s";

            GlyphLayout timerLayout = new GlyphLayout(hudFont, timerText);

            hudFont.setColor(1f, 0.85f, 0.2f, 1f);

            hudFont.draw(
                batch,
                timerText,
                camera.position.x - timerLayout.width / 2f,
                camera.position.y + viewHeight / 2f - 42f
            );

            String shopControls =
                "[1] Shotgun $200    [2] AK-47 $180    [3] Pistol";

            GlyphLayout controlsLayout =
                new GlyphLayout(hudFont, shopControls);

            hudFont.draw(
                batch,
                shopControls,
                camera.position.x - controlsLayout.width / 2f,
                camera.position.y + viewHeight / 2f - 60f
            );

            if (!shopMessage.isEmpty()) {
                GlyphLayout messageLayout =
                    new GlyphLayout(hudFont, shopMessage);

                hudFont.setColor(Color.WHITE);

                hudFont.draw(
                    batch,
                    shopMessage,
                    camera.position.x - messageLayout.width / 2f,
                    camera.position.y + viewHeight / 2f - 78f
                );
            }
        }

        batch.end();
    }

    // --------------------------------------------------
    // BOSS HEALTH
    // --------------------------------------------------

    private void renderBossHealth(
            PlayerState player,
            float viewWidth,
            float viewHeight
    ) {
        if (!bossSpawned) {
            return;
        }

        Enemy boss = null;

        for (Enemy enemy : enemies) {
            if (enemy instanceof TargetDummy) {
                boss = enemy;
                break;
            }
        }

        if (boss == null) {
            return;
        }

        float barWidth = Math.min(400f, viewWidth * 0.6f);
        float barHeight = 14f;

        float barX = camera.position.x - barWidth / 2f;
        float barY = camera.position.y + viewHeight / 2f - 75f;

        float healthPercent = boss.getMaxHealth() > 0
            ? (float) boss.getHealth() / boss.getMaxHealth()
            : 0f;

        healthPercent = Math.max(0f, Math.min(1f, healthPercent));

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0.12f, 0.02f, 0.02f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        shapeRenderer.setColor(0.85f, 0.05f, 0.05f, 1f);
        shapeRenderer.rect(
            barX,
            barY,
            barWidth * healthPercent,
            barHeight
        );

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        String bossText =
            "BOSS: " + boss.getHealth() + " / " + boss.getMaxHealth();

        GlyphLayout layout = new GlyphLayout(hudFont, bossText);

        hudFont.setColor(Color.WHITE);

        hudFont.draw(
            batch,
            bossText,
            camera.position.x - layout.width / 2f,
            barY + barHeight + 18f
        );

        batch.end();
    }

    // --------------------------------------------------
    // RESIZE
    // --------------------------------------------------

    @Override
    public void resize(int width, int height) {
        if (viewport != null) {
            viewport.update(width, height, true);
        }

        if (hudCamera != null) {
            hudCamera.setToOrtho(false, width, height);
            hudCamera.update();
        }
    }

    // --------------------------------------------------
    // PAUSE / RESUME
    // --------------------------------------------------

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void hide() {
    }

    // --------------------------------------------------
    // DISPOSE
    // --------------------------------------------------

    @Override
    public void dispose() {
        if (gameplaySession != null) {
            gameplaySession.dispose();
            gameplaySession = null;
        }

        if (sceneLoader != null) {
            sceneLoader.dispose();
            sceneLoader = null;
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

        for (Texture texture : hudWeaponTextures.values()) {
            if (texture != null) {
                texture.dispose();
            }
        }

        hudWeaponTextures.clear();
    }
}   