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
import com.github.glutton.entities.UpgradeNPC;
import com.github.glutton.player.PlayerState;
import com.github.glutton.weapons.WeaponProfile;
import com.github.glutton.world.GameplaySession;
import com.github.glutton.world.PhysicsConfig;
import com.github.glutton.weapons.WeaponState;
import com.github.glutton.screens.DifficultySettingsScreen;

import com.github.glutton.SessionManager;
import com.github.glutton.AuthClient;
import com.badlogic.gdx.utils.JsonValue;

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

    private final Map<WeaponProfile, Texture> hudWeaponTextures = new EnumMap<>(WeaponProfile.class);

    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Box2DDebugRenderer box2dDebugRenderer;
    private BitmapFont hudFont;
    private ParallaxBackground background;

    private boolean debugHitbox = false;
    private GameplaySession gameplaySession;

    private static final int MAX_DEATHS = 1;
    private int deathCount = 0;
    private boolean gameOver = false;

    private final Rectangle retryButtonBounds = new Rectangle();
    private final Rectangle menuButtonBounds = new Rectangle();
    private final Vector2 gameOverTouch = new Vector2();

    private static final float ENEMY_WIDTH = 24f;
    private static final float ENEMY_HEIGHT = 36f;
    private static final float ENEMY_SPAWN_X = 200f;
    private static final float ENEMY_SPAWN_Y = 150f;

    private static final int BOSS_MAX_HEALTH = 1500;
    private static final int BOSS_BASE_DAMAGE = 5;

    private static final float BUY_TIME_SECONDS = 15f;
    private static final int SHOTGUN_COST = 200;
    private static final int AK_COST = 180;

    private int currentWave = 0;
    private int totalKills = 0;
    private int currency = 0;

    private boolean bossSpawned = false;
    private UpgradeNPC upgradeNPC;
    private boolean upgradeMenuOpen = false;
    private String upgradeMessage = "";

    private static final int DAMAGE_UPGRADE_BASE_COST = 100;
    private static final int MAGAZINE_UPGRADE_BASE_COST = 80;
    private static final int FIRE_RATE_UPGRADE_BASE_COST = 120;

    private boolean waitingForNextWave = false;
    private boolean shopOpen = false;

    private float waveDelayTimer = 0f;
    private String shopMessage = "";

    public Page1Screen(GluttonGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        StatsTracker.startNewRun();
        Gdx.input.setCursorCatched(false);
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

        camera = new OrthographicCamera();
        camera.zoom = 0.25f;

        hudCamera = new OrthographicCamera();
        hudCamera.setToOrtho(
                false,
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight());

        viewport = new FitViewport(1280f, 720f, camera);
        viewport.update(
                Gdx.graphics.getWidth(),
                Gdx.graphics.getHeight(),
                true);

        background = new ParallaxBackground(camera.position.y);

        sceneLoader = new SceneLoader(new SceneConfiguration());
        sceneLoader.loadScene("Page1", viewport);

        if (sceneLoader.getWorld() != null) {
            sceneLoader.getWorld().setGravity(
                    new Vector2(0f, PhysicsConfig.GRAVITY_Y));
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
                        new Texture(Gdx.files.internal(weapon.idleTexturePath)));
            } catch (Exception missingWeaponSprite) {
                try {
                    hudWeaponTextures.put(
                            weapon,
                            new Texture(
                                    Gdx.files.internal("sprites/weapons/Glock - P80.png")));
                } catch (Exception missingFallback) {
                    Gdx.app.error(
                            "HUD",
                            "Could not load HUD weapon sprite for "
                                    + weapon.displayName,
                            missingFallback);
                }
            }
        }

        gameplaySession = new GameplaySession(
                camera,
                "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
                "sprites/weapons/01 - Individual sprites/Bullets & Ammo/Glock - P80/Bullet.png",
                sceneLoader);

        startNextWave();
    }

    private void startNextWave() {
        currentWave++;

        waitingForNextWave = false;
        shopOpen = false;
        upgradeMenuOpen = false;
        waveDelayTimer = 0f;
        shopMessage = "";

        if (upgradeNPC != null) {
            upgradeNPC.remove();
            upgradeNPC = null;
        }

        if (currentWave % 5 == 0) {
            spawnBosses(currentWave / 5);
            return;
        }

        int total;
        int zombies;
        int ghouls;
        int hounds;

        switch (currentWave) {
            case 1:
            case 2:
                total = 10;
                zombies = 6;
                ghouls = 3;
                hounds = 1;
                break;

            case 3:
                total = 10;
                zombies = 5;
                ghouls = 3;
                hounds = 2;
                break;

            case 4:
                total = 15;
                zombies = 8;
                ghouls = 4;
                hounds = 3;
                break;

            default:
                total = 10 + currentWave * 3;
                zombies = Math.round(total * 0.60f);
                ghouls = Math.round(total * 0.25f);
                hounds = total - zombies - ghouls;
                break;
        }

        float multiplier = game.getDifficulty().getMobMultiplier();

        int adjustedTotal = Math.max(
                1,
                Math.round(total * multiplier));

        zombies = Math.round(
                adjustedTotal * (zombies / (float) total));

        ghouls = Math.round(
                adjustedTotal * (ghouls / (float) total));

        hounds = adjustedTotal - zombies - ghouls;

        if (hounds < 0) {
            ghouls = Math.max(0, ghouls + hounds);
            hounds = 0;
        }

        Gdx.app.log(
                "Difficulty",
                game.getDifficulty().name()
                        + " | Wave " + currentWave
                        + " | Monsters: " + adjustedTotal);

        spawnMixedWave(
                adjustedTotal,
                zombies,
                ghouls,
                hounds);
    }

    private Enemy createEnemy(String enemyType, float x, float y) {
        switch (enemyType.toLowerCase()) {
            case "zombie":
                return new Zombie(sceneLoader.getWorld(), x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
            case "ghoul":
                return new Ghoul(sceneLoader.getWorld(), x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
            case "hound":
                return new Hound(sceneLoader.getWorld(), x, y, ENEMY_WIDTH, ENEMY_HEIGHT);
            default:
                Gdx.app.error("Waves", "Unknown enemy type: " + enemyType);
                return null;
        }
    }

    private void spawnBosses(int bossCount) {
        if (sceneLoader == null || sceneLoader.getWorld() == null) {
            return;
        }

        bossSpawned = true;
        waitingForNextWave = false;
        shopOpen = false;
        upgradeMenuOpen = false;

        float bossWidth = ENEMY_WIDTH * 1.5f;
        float bossHeight = ENEMY_HEIGHT * 1.5f;

        for (int i = 0; i < bossCount; i++) {
            float spawnX = ENEMY_SPAWN_X + i * 90f;
            float spawnY = ENEMY_SPAWN_Y;

            int bossHealth = BOSS_MAX_HEALTH + ((currentWave / 5) - 1) * 250;

            TargetDummy boss = new TargetDummy(
                    sceneLoader.getWorld(),
                    spawnX,
                    spawnY,
                    bossWidth,
                    bossHeight,
                    bossHealth,
                    BOSS_BASE_DAMAGE,
                    250f,
                    40f,
                    1f);

            enemies.add(boss);
        }

        Gdx.app.log("Waves", "Wave " + currentWave + ": spawned " + bossCount + " boss(es).");
    }

    private void updateWaveProgress(float delta) {
        if (!enemies.isEmpty()) {
            return;
        }

        bossSpawned = false;

        if (!waitingForNextWave) {
            StatsTracker.waveSurvived();

            waitingForNextWave = true;
            shopOpen = true;
            waveDelayTimer = BUY_TIME_SECONDS;

            shopMessage = "Wave cleared! Spend your currency before the next wave.";

            if (currentWave % 3 == 0 && gameplaySession != null) {
                PlayerState player = gameplaySession.getPlayerState();

                if (player != null) {
                    upgradeNPC = new UpgradeNPC(
                            player.x + player.width + 15f,
                            player.y,
                            player.width,
                            player.height);
                }
            }

            Gdx.app.log("Waves", "Wave " + currentWave + " cleared.");
        }

        if (shopOpen) {
            PlayerState player = gameplaySession != null
                    ? gameplaySession.getPlayerState()
                    : null;

            boolean nearNPC = player != null
                    && upgradeNPC != null
                    && upgradeNPC.isActive()
                    && upgradeNPC.isNear(player.x, player.y);

            if (nearNPC && Gdx.input.isKeyJustPressed(Input.Keys.B)) {
                upgradeMenuOpen = !upgradeMenuOpen;
            }

            if (!nearNPC) {
                upgradeMenuOpen = false;
            }

            if (upgradeMenuOpen) {
                handleUpgradeInput();
            } else {
                handleShopInput();
            }

            waveDelayTimer -= delta;

            if (waveDelayTimer <= 0f) {
                startNextWave();
            }
        }
    }

    private void spawnMixedWave(int total, int zombies, int ghouls, int hounds) {
        if (sceneLoader == null || sceneLoader.getWorld() == null) {
            return;
        }

        int spawned = 0;

        for (int i = 0; i < zombies && spawned < total; i++) {
            float x = ENEMY_SPAWN_X + (spawned % 5) * 60f;
            float y = ENEMY_SPAWN_Y + (spawned / 5) * 60f;
            Enemy enemy = createEnemy("zombie", x, y);
            if (enemy != null) {
                enemies.add(enemy);
            }
            spawned++;
        }

        for (int i = 0; i < ghouls && spawned < total; i++) {
            float x = ENEMY_SPAWN_X + (spawned % 5) * 60f;
            float y = ENEMY_SPAWN_Y + (spawned / 5) * 60f;
            Enemy enemy = createEnemy("ghoul", x, y);
            if (enemy != null) {
                enemies.add(enemy);
            }
            spawned++;
        }

        for (int i = 0; i < hounds && spawned < total; i++) {
            float x = ENEMY_SPAWN_X + (spawned % 5) * 60f;
            float y = ENEMY_SPAWN_Y + (spawned / 5) * 60f;
            Enemy enemy = createEnemy("hound", x, y);
            if (enemy != null) {
                enemies.add(enemy);
            }
            spawned++;
        }

        Gdx.app.log("Waves", "Wave " + currentWave + ": spawned " + spawned + " enemies.");
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
            if (player.weaponState.ownedWeapons.contains(WeaponProfile.SHOTGUN)) {
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
            if (player.weaponState.ownedWeapons.contains(WeaponProfile.ASSAULT_RIFLE)) {
                equipWeapon(player, WeaponProfile.ASSAULT_RIFLE);
                shopMessage = "AK-47 equipped!";
            } else if (currency >= AK_COST) {
                currency -= AK_COST;
                player.weaponState.ownedWeapons.add(WeaponProfile.ASSAULT_RIFLE);
                equipWeapon(player, WeaponProfile.ASSAULT_RIFLE);
                shopMessage = "AK-47 purchased and equipped!";
            } else {
                shopMessage = "Not enough currency for the AK-47.";
            }
        }
    }

    private void handleUpgradeInput() {
        if (gameplaySession == null) {
            return;
        }

        PlayerState player = gameplaySession.getPlayerState();

        if (player == null || player.weaponState == null) {
            return;
        }

        WeaponState ws = player.weaponState;
        WeaponProfile weapon = ws.activeWeapon;

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
            int level = ws.getDamageLevel(weapon);
            int cost = DAMAGE_UPGRADE_BASE_COST * (level + 1);

            if (level >= WeaponState.MAX_UPGRADE_LEVEL) {
                upgradeMessage = "Damage is already at maximum level.";
            } else if (currency < cost) {
                upgradeMessage = "Not enough currency! Need $" + cost;
            } else if (ws.upgradeDamage(weapon)) {
                currency -= cost;
                upgradeMessage = weapon.displayName + " damage upgraded to level " + ws.getDamageLevel(weapon) + "!";
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
            int level = ws.getMagazineLevel(weapon);
            int cost = MAGAZINE_UPGRADE_BASE_COST * (level + 1);

            if (level >= WeaponState.MAX_UPGRADE_LEVEL) {
                upgradeMessage = "Magazine is already at maximum level.";
            } else if (currency < cost) {
                upgradeMessage = "Not enough currency! Need $" + cost;
            } else if (ws.upgradeMagazine(weapon)) {
                currency -= cost;
                upgradeMessage = weapon.displayName + " magazine upgraded to level " + ws.getMagazineLevel(weapon) + "!";
            }
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
            int level = ws.getFireRateLevel(weapon);
            int cost = FIRE_RATE_UPGRADE_BASE_COST * (level + 1);

            if (level >= WeaponState.MAX_UPGRADE_LEVEL) {
                upgradeMessage = "Fire rate is already at maximum level.";
            } else if (currency < cost) {
                upgradeMessage = "Not enough currency! Need $" + cost;
            } else if (ws.upgradeFireRate(weapon)) {
                currency -= cost;
                upgradeMessage = weapon.displayName + " fire rate upgraded to level " + ws.getFireRateLevel(weapon) + "!";
            }
        }
    }

    private void equipWeapon(PlayerState player, WeaponProfile weapon) {
        WeaponState ws = player.weaponState;
        ws.activeWeapon = weapon;
        ws.currentAmmo = ws.getEffectiveMaxAmmo(weapon);
        ws.isReloading = false;
    }

    private int getEnemyReward(Enemy enemy) {
        if (enemy instanceof TargetDummy) {
            return 100;
        }
        if (enemy instanceof Hound) {
            return 13;
        }
        if (enemy instanceof Zombie || enemy instanceof Ghoul) {
            return 10;
        }
        return 0;
    }

    @Override
    public void render(float delta) {
        game.revealWindowOnce();

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

        // Remove defeated enemies and award rewards.
        if (!gameOver
                && sceneLoader != null
                && sceneLoader.getWorld() != null
                && !sceneLoader.getWorld().isLocked()) {

            Iterator<Enemy> iterator = enemies.iterator();

            while (iterator.hasNext()) {
                Enemy enemy = iterator.next();

                if (enemy.isDestroyed()
                        && (!(enemy instanceof Zombie)
                                || ((Zombie) enemy).isDeathAnimationFinished())) {

                    int reward = getEnemyReward(enemy);
                    currency += reward;
                    totalKills++;

                    StatsTracker.monsterKilled();
                    StatsTracker.earnCash(reward);

                    Gdx.app.log(
                            "Rewards",
                            "Enemy defeated! +" + reward
                                    + " currency. Total: " + currency);

                    enemy.disposeBody();
                    iterator.remove();
                }
            }
        }

        // Update wave progress.
        if (!gameOver) {
            updateWaveProgress(safeDelta);
        }

        // Step the Box2D physics world.
        if (!gameOver
                && sceneLoader != null
                && sceneLoader.getWorld() != null) {

            float subDelta = safeDelta / PhysicsConfig.PHYSICS_SUBSTEPS;

            for (int i = 0; i < PhysicsConfig.PHYSICS_SUBSTEPS; i++) {
                sceneLoader.getWorld().step(
                        subDelta,
                        PhysicsConfig.VELOCITY_ITERATIONS,
                        PhysicsConfig.POSITION_ITERATIONS);
            }
        }

        // Update enemy AI and player damage.
        if (playerState != null && !gameOver) {
            float playerX = playerState.x;
            float playerY = playerState.y;

            for (Enemy enemy : enemies) {
                enemy.updateAI(safeDelta, playerState);

                if (!enemy.isDestroyed()
                        && enemy.canAttack(playerX, playerY)) {

                    if (enemy instanceof Zombie) {
                        ((Zombie) enemy).triggerAttackAnimation();
                    }

                    int damage = enemy.getAttackDamage();
                    playerState.takeDamage(damage);
                }
            }

            handlePlayerDeath(playerState);

            camera.position.set(
                    playerState.x,
                    playerState.y + 32f,
                    0f);
        }

        camera.update();
        viewport.apply();

        float viewWidth = camera.viewportWidth * camera.zoom;
        float viewHeight = camera.viewportHeight * camera.zoom;

        float cameraLeftEdge = camera.position.x - viewWidth / 2f;
        float cameraBottomEdge = camera.position.y - viewHeight / 2f;

        // Render background.
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
                    viewHeight);
        }

        batch.end();

        // Process scene engine.
        if (!gameOver
                && sceneLoader != null
                && sceneLoader.getEngine() != null) {

            sceneLoader.getEngine().setDelta(safeDelta);
            sceneLoader.getEngine().process();
        }

        if (gameplaySession != null) {

            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            gameplaySession.render(batch, shapeRenderer);

            batch.end();

            gameplaySession.renderTrails(
                    shapeRenderer,
                    camera.combined);

            // Render enemy shapes and other non-sprite objects.
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            gameplaySession.renderMuzzleFlash(shapeRenderer);

            for (Enemy enemy : enemies) {
                // Sprite-based enemies render separately below.
                if (!(enemy instanceof Zombie)
                        && !(enemy instanceof Hound)) {
                    enemy.render(shapeRenderer);
                }
            }

            if (shopOpen
                    && upgradeNPC != null
                    && upgradeNPC.isActive()) {
                batch.begin();
                upgradeNPC.render(batch, safeDelta);
                batch.end();
            }

            shapeRenderer.end();

            // Render animated enemy sprites.
            batch.setProjectionMatrix(camera.combined);
            batch.begin();

            for (Enemy enemy : enemies) {

                if (enemy instanceof Zombie) {
                    ((Zombie) enemy).renderSprite(batch, safeDelta);

                } else if (enemy instanceof Hound) {
                    ((Hound) enemy).renderSprite(batch, safeDelta);
                }
            }

            batch.end();

            renderEnemyHealthBars();
        }

        // Render HUD.
        if (playerState != null) {
            renderDoomHud(playerState);
            renderWaveStatus(viewWidth, viewHeight);
            renderBossHealth(playerState, viewWidth, viewHeight);
        }

        // Optional Box2D hitbox debugging.
        if (debugHitbox
                && !gameOver
                && sceneLoader != null
                && sceneLoader.getWorld() != null) {

            box2dDebugRenderer.render(
                    sceneLoader.getWorld(),
                    camera.combined);
        }

        // Game-over overlay.
        if (gameOver) {
            renderGameOverOverlay(viewWidth, viewHeight);
            handleGameOverInput();
        }
    }

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
                        barHeight);
            }
        }

        shapeRenderer.end();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Enemy enemy : enemies) {
            if (enemy.isDestroyed() || enemy.isBodyDisposed()) {
                continue;
            }

            String hpText = enemy.getHealth() + " / " + enemy.getMaxHealth();

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

    private void handlePlayerDeath(PlayerState player) {
        if (player == null || !player.isDead() || gameOver) {
            return;
        }

        deathCount++;

        if (deathCount >= MAX_DEATHS) {
            gameOver = true;
            StatsTracker.endRun();

            // Save stats to database if user is logged in
            if (SessionManager.isLoggedIn()) {
                AuthClient.saveStats(
                        SessionManager.getUserId(),
                        0, // completion (0 = defeat / game over)
                        currency,
                        currentWave,
                        totalKills,
                        new AuthClient.AuthListener() {
                            @Override
                            public void onSuccess(String message, JsonValue data) {
                                Gdx.app.log("Stats", "Run stats saved successfully!");
                            }

                            @Override
                            public void onError(String message) {
                                Gdx.app.error("Stats", "Failed to save stats: " + message);
                            }
                        });
            }

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

    private void renderGameOverOverlay(float viewWidth, float viewHeight) {
        float centerX = camera.position.x;
        float centerY = camera.position.y;
        float left = centerX - viewWidth / 2f;
        float bottom = centerY - viewHeight / 2f;

        retryButtonBounds.set(centerX - 48f, centerY - 12f, 96f, 22f);
        menuButtonBounds.set(centerX - 48f, centerY - 42f, 96f, 22f);

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0f, 0f, 0f, 0.78f);
        shapeRenderer.rect(left, bottom, viewWidth, viewHeight);

        shapeRenderer.setColor(0.65f, 0.08f, 0.08f, 1f);
        shapeRenderer.rect(
                retryButtonBounds.x,
                retryButtonBounds.y,
                retryButtonBounds.width,
                retryButtonBounds.height);

        shapeRenderer.setColor(0.12f, 0.30f, 0.62f, 1f);
        shapeRenderer.rect(
                menuButtonBounds.x,
                menuButtonBounds.y,
                menuButtonBounds.width,
                menuButtonBounds.height);

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        hudFont.setColor(Color.WHITE);
        drawCenteredText("GAME OVER", centerX, centerY + 25f);
        drawCenteredText("You died " + deathCount + " times", centerX, centerY + 10f);
        drawCenteredText("RETRY", centerX, retryButtonBounds.y + 14f);
        drawCenteredText("MENU", centerX, menuButtonBounds.y + 14f);

        batch.end();
    }

    private void drawCenteredText(String text, float centerX, float y) {
        GlyphLayout layout = new GlyphLayout(hudFont, text);
        hudFont.draw(batch, text, centerX - layout.width / 2f, y);
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
            game.showMainMenu();
        }
    }

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

        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        hudFont.setColor(Color.WHITE);
        hudFont.draw(batch, "HP: " + player.health, 30f, hudHeight / 2f + 10f);
        hudFont.draw(batch, "AMMO: " + ammo, screenWidth - 150f, hudHeight / 2f + 10f);
        hudFont.draw(batch, "WAVE: " + currentWave, centerX - 40f, hudHeight - 20f);
        hudFont.draw(batch, "CASH: $" + currency, centerX - 40f, hudHeight - 45f);

        if (shopOpen) {
            hudFont.draw(batch, shopMessage, 30f, screenHeight - 40f);
        }
        if (upgradeMenuOpen) {
            hudFont.draw(batch, upgradeMessage, 30f, screenHeight - 70f);
        }

        batch.end();
    }

    private void renderWaveStatus(float viewWidth, float viewHeight) {
        // Handled within renderDoomHud
    }

    private void renderBossHealth(PlayerState player, float viewWidth, float viewHeight) {
        // Boss health handled via standard enemy health bars
    }

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

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (shapeRenderer != null) shapeRenderer.dispose();
        if (box2dDebugRenderer != null) box2dDebugRenderer.dispose();
        if (hudFont != null) hudFont.dispose();
        for (Texture tex : hudWeaponTextures.values()) {
            if (tex != null) tex.dispose();
        }
        hudWeaponTextures.clear();
        if (sceneLoader != null) {
            sceneLoader = null;
        }
    }
}