package com.github.glutton.weapons;

// LibGDX classes for logging, textures, and rendering
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Math and collision-related classes
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

// Box2D classes used to detect collisions with level obstacles
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

// Game classes
import com.github.glutton.entities.Enemy;
import games.rednblack.editor.renderer.SceneLoader;

// Java collection classes
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulletManager {

    // Stores information about a bullet impact.
    // This information can be used to display impact effects.
    public static class Impact {

        // World position where the impact occurred
        public final float x;
        public final float y;

        // Damage associated with the impact
        public final int damage;

        // Time elapsed since the impact was created
        public float age = 0f;

        // Creates a new impact record
        public Impact(float x, float y, int damage) {
            this.x = x;
            this.y = y;
            this.damage = damage;
        }
    }

    // Stores all bullets currently managed by this class
    private final List<Bullet> bullets = new ArrayList<>();

    // Manages visual effects created when bullets hit something
    private final ImpactManager impactManager;

    // Provides access to the game's physics world through the scene loader
    private final SceneLoader sceneLoader;

    // Stores the bullet texture for each weapon type
    private final Map<WeaponProfile, Texture> bulletTextures =
        new EnumMap<>(WeaponProfile.class);

    // Stores drawable regions for each weapon's bullet texture
    private final Map<WeaponProfile, TextureRegion> bulletRegions =
        new EnumMap<>(WeaponProfile.class);

    // Default bullet lifetime in seconds
    private static final float DEFAULT_LIFESPAN = 1.5f;

    // Radius assigned to a bullet for other systems that need it
    private static final float BULLET_RADIUS = 1.5f;

    // Extra width and height added to an enemy's hitbox.
    // This makes enemies easier to hit without changing their visible size.
    private static final float ENEMY_HITBOX_EXTRA_WIDTH = 12f;
    private static final float ENEMY_HITBOX_EXTRA_HEIGHT = 10f;

    // Constructor: prepares bullet textures and connects the required systems
    public BulletManager(
            SceneLoader sceneLoader,
            String defaultTexturePath,
            ImpactManager impactManager
    ) {
        this.sceneLoader = sceneLoader;
        this.impactManager = impactManager;

        // Load a bullet texture for every weapon profile
        for (WeaponProfile wp : WeaponProfile.values()) {

            // Start with the texture path configured for this weapon
            String path = wp.bulletTexturePath;

            // If the weapon's texture is missing, use the default texture
            if (!Gdx.files.internal(path).exists()) {
                path = defaultTexturePath;
            }

            // Load the texture from the game's internal assets
            Texture tex = new Texture(Gdx.files.internal(path));

            // Save the texture and create a drawable region for it
            bulletTextures.put(wp, tex);
            bulletRegions.put(wp, new TextureRegion(tex));
        }
    }

    // Creates a bullet and adds it to the active bullet list.
    // angleRad is the firing angle in radians.
    public void spawn(
            float x,
            float y,
            float angleRad,
            float speed,
            int damage,
            WeaponProfile weapon
    ) {

        // Convert the firing angle and speed into horizontal and vertical velocity
        float vx = (float) Math.cos(angleRad) * speed;
        float vy = (float) Math.sin(angleRad) * speed;

        // Create the bullet and add it to the list
        bullets.add(
            new Bullet(
                x,
                y,
                vx,
                vy,
                damage,
                DEFAULT_LIFESPAN,
                weapon
            )
        );
    }

    // Checks whether a line segment intersects a rectangle.
    // The segment runs from (x1, y1) to (x2, y2).
    // If an intersection is found, outPoint receives the first intersection point.
    public static boolean intersectSegmentRectangle(
            float x1,
            float y1,
            float x2,
            float y2,
            Rectangle rect,
            Vector2 outPoint
    ) {

        // Calculate the horizontal and vertical distances travelled by the segment
        float dx = x2 - x1;
        float dy = y2 - y1;

        // tmin and tmax describe the portion of the segment inside the rectangle
        float tmin = 0f;
        float tmax = 1f;

        // Check intersection with the rectangle's left and right boundaries
        if (dx != 0f) {
            float tx1 = (rect.x - x1) / dx;
            float tx2 = (rect.x + rect.width - x1) / dx;

            // Ensure tx1 is the smaller value
            if (tx1 > tx2) {
                float tmp = tx1;
                tx1 = tx2;
                tx2 = tmp;
            }

            // Narrow the valid portion of the segment
            tmin = Math.max(tmin, tx1);
            tmax = Math.min(tmax, tx2);

        } else if (x1 < rect.x || x1 > rect.x + rect.width) {

            // The segment is vertical and outside the rectangle horizontally
            return false;
        }

        // Check intersection with the rectangle's bottom and top boundaries
        if (dy != 0f) {
            float ty1 = (rect.y - y1) / dy;
            float ty2 = (rect.y + rect.height - y1) / dy;

            // Ensure ty1 is the smaller value
            if (ty1 > ty2) {
                float tmp = ty1;
                ty1 = ty2;
                ty2 = tmp;
            }

            // Narrow the valid portion of the segment
            tmin = Math.max(tmin, ty1);
            tmax = Math.min(tmax, ty2);

        } else if (y1 < rect.y || y1 > rect.y + rect.height) {

            // The segment is horizontal and outside the rectangle vertically
            return false;
        }

        // No intersection exists if the valid ranges do not overlap
        if (tmin > tmax) {
            return false;
        }

        // Calculate and store the first intersection point
        outPoint.set(
            x1 + dx * tmin,
            y1 + dy * tmin
        );

        return true;
    }

    // Updates bullet movement, collision detection, damage, and impact effects
    public void update(
            float delta,
            List<Enemy> enemies
    ) {

        // Get the Box2D world if a scene loader is available
        World world = sceneLoader != null
            ? sceneLoader.getWorld()
            : null;

        // Iterator allows bullets to be removed safely while looping
        Iterator<Bullet> it = bullets.iterator();

        while (it.hasNext()) {
            Bullet b = it.next();

            // If the bullet already hit something, let its trail fade out
            if (b.hasCollided) {
                b.fadeTimer -= delta;

                // Remove the bullet after its impact linger time expires
                if (b.fadeTimer <= 0f) {
                    it.remove();
                }

                continue;
            }

            // Calculate where the bullet will be after this frame
            float nextX = b.x + b.vx * delta;
            float nextY = b.y + b.vy * delta;

            // Track whether a collision occurs during this movement step
            boolean collided = false;

            // Default impact position is the bullet's next position
            float hitX = nextX;
            float hitY = nextY;

            // Stores the enemy hit, if the bullet hits an enemy
            Enemy hitEnemy = null;

            // Distance squared to the closest collision.
            // Squared distance avoids needing a square-root calculation.
            float closestDistSq = Float.MAX_VALUE;

            // ------------------------------------------
            // 1. CHECK LEVEL OBSTACLES
            // Use a Box2D raycast to detect walls and other solid obstacles.
            // ------------------------------------------
            if (world != null) {
                final float[] closestFraction = {1f};
                final Vector2 rayHitPoint = new Vector2();
                final boolean[] worldHit = {false};

                world.rayCast(
                    new RayCastCallback() {
                        @Override
                        public float reportRayFixture(
                                Fixture fixture,
                                Vector2 point,
                                Vector2 normal,
                                float fraction
                        ) {

                            // Ignore enemy bodies because enemy hitboxes
                            // are checked separately below
                            if (fixture.getBody().getUserData()
                                    instanceof Enemy) {
                                return -1f;
                            }

                            // Ignore sensor fixtures because they should
                            // not stop bullets in this collision check
                            if (fixture.isSensor()) {
                                return -1f;
                            }

                            // Save the closest obstacle hit so far
                            if (fraction < closestFraction[0]) {
                                closestFraction[0] = fraction;
                                rayHitPoint.set(point);
                                worldHit[0] = true;
                            }

                            // Continue checking for a closer collision
                            return fraction;
                        }
                    },
                    new Vector2(b.x, b.y),
                    new Vector2(nextX, nextY)
                );

                // If the ray hit an obstacle, compare its distance
                // with any enemy collision found later
                if (worldHit[0]) {
                    float dx = rayHitPoint.x - b.x;
                    float dy = rayHitPoint.y - b.y;

                    float distSq = dx * dx + dy * dy;

                    if (distSq < closestDistSq) {
                        closestDistSq = distSq;
                        hitX = rayHitPoint.x;
                        hitY = rayHitPoint.y;
                        collided = true;
                    }
                }
            }

            // ------------------------------------------
            // 2. CHECK ENEMY HITBOXES
            // Test the bullet's path against each enemy's expanded rectangle.
            // ------------------------------------------
            if (enemies != null) {
                Vector2 contact = new Vector2();

                for (Enemy enemy : enemies) {

                    // Skip enemies that have already been destroyed
                    if (enemy.isDestroyed()) {
                        continue;
                    }

                    // Expand the hitbox without changing the enemy's visible size
                    Rectangle bulletHitbox = new Rectangle(
                        enemy.bounds.x
                            - ENEMY_HITBOX_EXTRA_WIDTH / 2f,
                        enemy.bounds.y
                            - ENEMY_HITBOX_EXTRA_HEIGHT / 2f,
                        enemy.bounds.width
                            + ENEMY_HITBOX_EXTRA_WIDTH,
                        enemy.bounds.height
                            + ENEMY_HITBOX_EXTRA_HEIGHT
                    );

                    // Check whether the bullet's movement segment crosses
                    // this enemy's hitbox
                    if (intersectSegmentRectangle(
                            b.x,
                            b.y,
                            nextX,
                            nextY,
                            bulletHitbox,
                            contact
                    )) {
                        float dx = contact.x - b.x;
                        float dy = contact.y - b.y;
                        float distSq = dx * dx + dy * dy;

                        // Only keep this collision if it is closer than
                        // the previously detected wall or enemy collision
                        if (distSq < closestDistSq) {
                            closestDistSq = distSq;
                            hitX = contact.x;
                            hitY = contact.y;
                            collided = true;
                            hitEnemy = enemy;
                        }
                    }
                }
            }

            // ------------------------------------------
            // 3. PROCESS COLLISION
            // Stop the bullet, apply damage if an enemy was hit,
            // and create an impact effect.
            // ------------------------------------------
            if (collided) {
                // Move the bullet to the actual collision point
                b.x = hitX;
                b.y = hitY;

                // Add the impact point to the bullet's visual trail
                b.trail.insert(0, new Vector2(hitX, hitY));

                // Keep the trail limited to five points
                if (b.trail.size > 5) {
                    b.trail.truncate(5);
                }

                // If an enemy was hit, apply the bullet's damage
                if (hitEnemy != null) {
                    hitEnemy.takeDamage(b.damage);

                    // Log the hit and the enemy's remaining health
                    Gdx.app.log(
                        "BulletManager",
                        "Hit enemy! Damage: " + b.damage
                            + ", remaining HP: " + hitEnemy.getHealth()
                    );
                }

                // Create a visual impact effect at the collision point
                impactManager.addImpact(hitX, hitY, b.damage);

                // Mark the bullet as collided and stop its movement
                b.hasCollided = true;
                b.vx = 0f;
                b.vy = 0f;

                continue;
            }

            // ------------------------------------------
            // 4. MOVE BULLET
            // If no collision occurred, update its position and lifetime.
            // ------------------------------------------
            b.x = nextX;
            b.y = nextY;

            // Reduce the bullet's remaining lifetime
            b.lifeRemaining -= delta;

            // Update the bullet's visual trail
            b.updateTrail(delta);

            // Remove the bullet when its lifetime expires.
            // An impact effect is also created at its final position.
            if (b.lifeRemaining <= 0f) {
                impactManager.addImpact(b.x, b.y, b.damage);
                it.remove();
            }
        }

        // Update the age and animation of active impact effects
        impactManager.update(delta);
    }

    // Draws the visual trails behind bullets
    public void drawTrails(
            ShapeRenderer shapeRenderer,
            Matrix4 projectionMatrix
    ) {

        // Use the supplied camera projection for drawing
        shapeRenderer.setProjectionMatrix(projectionMatrix);

        // Begin drawing line shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Process each bullet's trail
        for (Bullet b : bullets) {

            // Calculate the fade amount for bullets that have collided
            float fadeProgress = b.hasCollided
                ? Math.max(0f, b.fadeTimer / 0.15f)
                : 1f;

            // Draw a line between consecutive trail points
            for (int i = 0; i < b.trail.size - 1; i++) {
                Vector2 p1 = b.trail.get(i);
                Vector2 p2 = b.trail.get(i + 1);

                // Calculate the alpha of each endpoint.
                // Points farther along the trail become more transparent.
                float alpha1 =
                    1f - ((float) i / b.trail.size);

                float alpha2 =
                    1f - ((float) (i + 1) / b.trail.size);

                // Average the endpoint alpha values and apply the fade
                float avgAlpha =
                    (alpha1 + alpha2) / 2f * fadeProgress;

                // Set the trail color to pale yellow with partial transparency
                shapeRenderer.setColor(
                    1f,
                    1f,
                    0.8f,
                    avgAlpha * 0.7f
                );

                // Draw the line segment between the two trail points
                shapeRenderer.line(
                    p1.x,
                    p1.y,
                    p2.x,
                    p2.y
                );
            }
        }

        // Finish drawing the trail lines
        shapeRenderer.end();
    }

    // Draws the bullet sprites
    public void draw(SpriteBatch batch) {

        for (Bullet b : bullets) {

            // Get the texture region associated with this bullet's weapon
            TextureRegion region = bulletRegions.get(b.weapon);

            // Use the pistol texture if the weapon's region is unavailable
            if (region == null) {
                region = bulletRegions.get(WeaponProfile.PISTOL);
            }

            // Calculate the bullet's rotation angle in degrees
            float angleDeg = (float) Math.toDegrees(
                Math.atan2(b.vy, b.vx)
            );

            // Scale the bullet sprite to 15% of its texture dimensions
            float width = region.getRegionWidth() * 0.15f;
            float height = region.getRegionHeight() * 0.15f;

            // Draw the bullet centered at its current position and rotate it
            batch.draw(
                region,
                b.x - width / 2f,
                b.y - height / 2f,
                width / 2f,
                height / 2f,
                width,
                height,
                1f,
                1f,
                angleDeg
            );
        }
    }

    // Returns a read-only view of the current bullet list
    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    // Returns the recent impact effects managed by ImpactManager
    public List<ImpactManager.Impact> getRecentImpacts() {
        return impactManager.getRecentImpacts();
    }

    // Returns the configured bullet radius
    public float getBulletRadius() {
        return BULLET_RADIUS;
    }

    // Returns the number of bullets currently stored by this manager
    public int getActiveCount() {
        return bullets.size();
    }

    // Releases the textures loaded by this manager.
    // Call this when the manager is no longer needed.
    public void dispose() {
        for (Texture tex : bulletTextures.values()) {
            if (tex != null) {
                tex.dispose();
            }
        }
    }

    // Removes a specific bullet from the active bullet list
    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
    }
}