package com.github.glutton.world;

// LibGDX classes used for the camera, sprite rendering, shapes, and matrices
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;

// Game entity and player classes
import com.github.glutton.entities.Enemy;
import com.github.glutton.player.PlayerAnimator;
import com.github.glutton.player.PlayerController;
import com.github.glutton.player.PlayerGunRenderer;
import com.github.glutton.player.PlayerState;

// Weapon and bullet management classes
import com.github.glutton.weapons.BulletManager;
import com.github.glutton.weapons.ImpactManager;

// Scene2D-related scene loader
import games.rednblack.editor.renderer.SceneLoader;

// Java collection for the list of enemies
import java.util.List;

public class GameplaySession {

    // Stores the player's position and other gameplay information
    private final PlayerState playerState;

    // Handles player movement, input, and other player controls
    private final PlayerController playerController;

    // Handles player sprite animations
    private final PlayerAnimator playerAnimator;

    // Draws the player's currently equipped gun
    private final PlayerGunRenderer gunRenderer;

    // Manages bullets, including their movement and collision behavior
    private final BulletManager bulletManager;

    // Manages visual effects created by bullet impacts
    private final ImpactManager impactManager;

    // Main constructor used to initialize the gameplay systems
    public GameplaySession(
            OrthographicCamera camera,
            String gunTexturePath,
            String bulletTexturePath,
            SceneLoader sceneLoader
    ) {

        // Create the player's initial state
        playerState = new PlayerState();

        // Set the player's starting position using the physics configuration
        playerState.x = PhysicsConfig.PLAYER_SPAWN_X;
        playerState.y = PhysicsConfig.PLAYER_SPAWN_Y;

        // Create the system responsible for bullet impact effects
        this.impactManager = new ImpactManager();

        // Create the bullet manager and connect it to the scene and impact system
        bulletManager = new BulletManager(
                sceneLoader,
                bulletTexturePath,
                impactManager
        );

        // Create the renderer for the player's gun
        gunRenderer = new PlayerGunRenderer(gunTexturePath);

        // Create the controller that handles player input and movement
        playerController = new PlayerController(
                camera,
                bulletManager,
                gunRenderer,
                sceneLoader
        );

        // Create the player's animation controller
        playerAnimator = new PlayerAnimator();
    }

    // Convenience constructor that uses the default bullet texture
    // when a custom bullet texture path is not provided
    public GameplaySession(
            OrthographicCamera camera,
            String gunTexturePath,
            SceneLoader sceneLoader
    ) {
        this(
                camera,
                gunTexturePath,
                "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[CASING] Glock - P80.png",
                sceneLoader
        );
    }

    // Updates the gameplay systems every frame
    public void update(float delta, List<Enemy> enemies) {

        // Process player input and update the player's state
        playerController.update(playerState, delta);

        // Update the player's animation
        playerAnimator.update(delta);

        // Update bullets and check them against the provided enemies
        bulletManager.update(delta, enemies);
    }

    // Convenience update method for situations where no enemy list is provided
    public void update(float delta) {
        update(delta, null);
    }

    // Draws the player's character, gun, and bullets
    public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {

        // Draw the player character and its current animation
        playerAnimator.draw(batch, playerState);

        // Draw the player's gun
        gunRenderer.draw(batch, playerState);

        // Draw active bullets
        bulletManager.draw(batch);
    }

    // Draws the gun's muzzle flash effect
    public void renderMuzzleFlash(ShapeRenderer shapeRenderer) {
        gunRenderer.drawMuzzleFlash(shapeRenderer, playerState);
    }

    // Draws bullet trails using the specified projection matrix
    public void renderTrails(
            ShapeRenderer shapeRenderer,
            Matrix4 projectionMatrix
    ) {
        bulletManager.drawTrails(shapeRenderer, projectionMatrix);
    }

    // Returns the player's current state
    public PlayerState getPlayerState() {
        return playerState;
    }

    // Returns the player controller so other classes can access its methods
    public PlayerController getPlayerController() {
        return playerController;
    }

    // Returns the gun renderer
    public PlayerGunRenderer getGunRenderer() {
        return gunRenderer;
    }

    // Returns the bullet manager
    public BulletManager getBulletManager() {
        return bulletManager;
    }

    // Returns the impact manager
    public ImpactManager getImpactManager() {
        return impactManager;
    }

    // Releases resources used by the gameplay systems
    // Call this when the gameplay session is no longer needed
    public void dispose() {

        // Release the player's animation resources
        if (playerAnimator != null) {
            playerAnimator.dispose();
        }

        // Release the gun renderer's resources
        if (gunRenderer != null) {
            gunRenderer.dispose();
        }

        // Release the bullet manager's resources
        if (bulletManager != null) {
            bulletManager.dispose();
        }
    }
}