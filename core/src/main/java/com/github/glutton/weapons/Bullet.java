package com.github.glutton.weapons;

// Vector2 stores the bullet's position for each point along its trail
import com.badlogic.gdx.math.Vector2;

// Array is LibGDX's collection class used to store trail points
import com.badlogic.gdx.utils.Array;

public class Bullet {

    // Current position of the bullet in the game world
    public float x, y;

    // Bullet velocity: how fast it moves horizontally and vertically
    public float vx, vy;

    // Amount of damage this bullet deals when it hits a target
    public int damage;

    // Remaining lifetime of the bullet, measured in seconds
    public float lifeRemaining;

    // Weapon profile that created this bullet
    // Can be used to identify the weapon's properties
    public WeaponProfile weapon;

    // Original position where the bullet was created
    // Useful for calculating bullet travel distance or drawing effects
    public final float spawnX, spawnY;

    // Stores recent positions of the bullet to create a visual trail
    public final Array<Vector2> trail = new Array<>();

    // Maximum number of position points stored in the bullet trail
    private static final int MAX_TRAIL_POINTS = 5;

    // Indicates whether the bullet has already collided with something
    public boolean hasCollided = false;

    // How long the bullet's impact effect remains visible after a collision
    // Measured in seconds; the default duration is 0.15 seconds
    public float fadeTimer = 0.15f;

    // Constructor: creates a bullet with its position, velocity, damage,
    // lifetime, and the weapon profile that fired it
    public Bullet(
            float x,
            float y,
            float vx,
            float vy,
            int damage,
            float lifeRemaining,
            WeaponProfile weapon
    ) {

        // Set the bullet's current position
        this.x = x;
        this.y = y;

        // Save the bullet's original spawn position
        this.spawnX = x;
        this.spawnY = y;

        // Set the bullet's horizontal and vertical velocity
        this.vx = vx;
        this.vy = vy;

        // Set the damage dealt by the bullet
        this.damage = damage;

        // Set how long the bullet can remain active
        this.lifeRemaining = lifeRemaining;

        // Remember which weapon fired this bullet
        this.weapon = weapon;

        // Add the initial position so the trail starts where the bullet spawns
        trail.add(new Vector2(x, y));
    }

    // Updates the bullet's visual trail as it moves
    public void updateTrail(float delta) {

        // Only update the trail while the bullet has not collided
        if (!hasCollided) {

            // Add the bullet's latest position to the beginning of the trail
            trail.insert(0, new Vector2(x, y));

            // Keep the trail from growing beyond the maximum number of points
            if (trail.size > MAX_TRAIL_POINTS) {
                trail.truncate(MAX_TRAIL_POINTS);
            }
        }
    }
}

