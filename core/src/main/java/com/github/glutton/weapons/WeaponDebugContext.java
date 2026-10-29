
package com.github.glutton.weapons;

// Provides the camera used to view the game world
import com.badlogic.gdx.graphics.OrthographicCamera;

// Provides Vector2 for storing 2D positions and coordinates
import com.badlogic.gdx.math.Vector2;

// Provides access to the player's current state and information
import com.github.glutton.player.PlayerState;

public class WeaponDebugContext {

    // Reference to the player's current state
    public final PlayerState player;

    // Manages bullets, including spawning, updating, and handling collisions
    public final BulletManager bulletManager;

    // Stores the weapon's muzzle position, where bullets originate
    public final Vector2 muzzlePos;

    // Camera used to view the game world and its coordinates
    public final OrthographicCamera camera;

    // Creates a debug context containing the player, bullet manager,
    // muzzle position, and camera for weapon-related debugging
    public WeaponDebugContext(
            PlayerState player,
            BulletManager bulletManager,
            Vector2 muzzlePos,
            OrthographicCamera camera) {

        // Store the player's state
        this.player = player;

        // Store the bullet manager
        this.bulletManager = bulletManager;

        // Store the muzzle position
        this.muzzlePos = muzzlePos;

        // Store the camera reference
        this.camera = camera;
    }
}
