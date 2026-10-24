
package com.github.glutton.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.RayCastCallback;
import com.badlogic.gdx.physics.box2d.World;

import com.github.glutton.entities.Enemy;
import games.rednblack.editor.renderer.SceneLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulletManager {

    public static class Impact {
        public final float x;
        public final float y;
        public final int damage;
        public float age = 0f;

        public Impact(float x, float y, int damage) {
            this.x = x;
            this.y = y;
            this.damage = damage;
        }
    }

    private final List<Bullet> bullets = new ArrayList<>();
    private final ImpactManager impactManager;
    private final SceneLoader sceneLoader;

    private final Map<WeaponProfile, Texture> bulletTextures =
        new EnumMap<>(WeaponProfile.class);

    private final Map<WeaponProfile, TextureRegion> bulletRegions =
        new EnumMap<>(WeaponProfile.class);

    private static final float DEFAULT_LIFESPAN = 1.5f;
    private static final float BULLET_RADIUS = 1.5f;

    // Extra collision area around enemies.
    private static final float ENEMY_HITBOX_EXTRA_WIDTH = 12f;
    private static final float ENEMY_HITBOX_EXTRA_HEIGHT = 10f;

    public BulletManager(
            SceneLoader sceneLoader,
            String defaultTexturePath,
            ImpactManager impactManager
    ) {
        this.sceneLoader = sceneLoader;
        this.impactManager = impactManager;

        for (WeaponProfile wp : WeaponProfile.values()) {
            String path = wp.bulletTexturePath;

            if (!Gdx.files.internal(path).exists()) {
                path = defaultTexturePath;
            }

            Texture tex = new Texture(Gdx.files.internal(path));

            bulletTextures.put(wp, tex);
            bulletRegions.put(wp, new TextureRegion(tex));
        }
    }

    public void spawn(
            float x,
            float y,
            float angleRad,
            float speed,
            int damage,
            WeaponProfile weapon
    ) {
        float vx = (float) Math.cos(angleRad) * speed;
        float vy = (float) Math.sin(angleRad) * speed;

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

    public static boolean intersectSegmentRectangle(
            float x1,
            float y1,
            float x2,
            float y2,
            Rectangle rect,
            Vector2 outPoint
    ) {
        float dx = x2 - x1;
        float dy = y2 - y1;

        float tmin = 0f;
        float tmax = 1f;

        if (dx != 0f) {
            float tx1 = (rect.x - x1) / dx;
            float tx2 = (rect.x + rect.width - x1) / dx;

            if (tx1 > tx2) {
                float tmp = tx1;
                tx1 = tx2;
                tx2 = tmp;
            }

            tmin = Math.max(tmin, tx1);
            tmax = Math.min(tmax, tx2);
        } else if (x1 < rect.x || x1 > rect.x + rect.width) {
            return false;
        }

        if (dy != 0f) {
            float ty1 = (rect.y - y1) / dy;
            float ty2 = (rect.y + rect.height - y1) / dy;

            if (ty1 > ty2) {
                float tmp = ty1;
                ty1 = ty2;
                ty2 = tmp;
            }

            tmin = Math.max(tmin, ty1);
            tmax = Math.min(tmax, ty2);
        } else if (y1 < rect.y || y1 > rect.y + rect.height) {
            return false;
        }

        if (tmin > tmax) {
            return false;
        }

        outPoint.set(
            x1 + dx * tmin,
            y1 + dy * tmin
        );

        return true;
    }

    public void update(
            float delta,
            List<Enemy> enemies
    ) {
        World world = sceneLoader != null
            ? sceneLoader.getWorld()
            : null;

        Iterator<Bullet> it = bullets.iterator();

        while (it.hasNext()) {
            Bullet b = it.next();

            if (b.hasCollided) {
                b.fadeTimer -= delta;

                if (b.fadeTimer <= 0f) {
                    it.remove();
                }

                continue;
            }

            float nextX = b.x + b.vx * delta;
            float nextY = b.y + b.vy * delta;

            boolean collided = false;

            float hitX = nextX;
            float hitY = nextY;

            Enemy hitEnemy = null;

            float closestDistSq = Float.MAX_VALUE;

            // 1. Check level obstacles using Box2D raycast.
            // Ignore enemy bodies so the enemy rectangle collision
            // can correctly assign hitEnemy and apply damage.
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
                            // Enemy collisions are handled separately below.
                            if (fixture.getBody().getUserData()
                                    instanceof Enemy) {
                                return -1f;
                            }

                            // Ignore sensors.
                            if (fixture.isSensor()) {
                                return -1f;
                            }

                            if (fraction < closestFraction[0]) {
                                closestFraction[0] = fraction;
                                rayHitPoint.set(point);
                                worldHit[0] = true;
                            }

                            return fraction;
                        }
                    },
                    new Vector2(b.x, b.y),
                    new Vector2(nextX, nextY)
                );

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

            // 2. Check enemy hitboxes.
            if (enemies != null) {
                Vector2 contact = new Vector2();

                for (Enemy enemy : enemies) {
                    if (enemy.isDestroyed()) {
                        continue;
                    }

                    // Expand the hitbox without resizing the visible enemy.
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

                        // Only accept the closest collision.
                        // The enemy must be closer than any wall or obstacle.
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

            // 3. Process collision and apply damage.
            if (collided) {
                b.x = hitX;
                b.y = hitY;

                b.trail.insert(0, new Vector2(hitX, hitY));

                if (b.trail.size > 5) {
                    b.trail.truncate(5);
                }

                if (hitEnemy != null) {
                    hitEnemy.takeDamage(b.damage);

                    Gdx.app.log(
                        "BulletManager",
                        "Hit enemy! Damage: " + b.damage
                            + ", remaining HP: " + hitEnemy.getHealth()
                    );
                }

                impactManager.addImpact(hitX, hitY, b.damage);

                b.hasCollided = true;
                b.vx = 0f;
                b.vy = 0f;

                continue;
            }

            // 4. Continue bullet movement if nothing was hit.
            b.x = nextX;
            b.y = nextY;

            b.lifeRemaining -= delta;
            b.updateTrail(delta);

            if (b.lifeRemaining <= 0f) {
                impactManager.addImpact(b.x, b.y, b.damage);
                it.remove();
            }
        }

        impactManager.update(delta);
    }

    public void drawTrails(
            ShapeRenderer shapeRenderer,
            Matrix4 projectionMatrix
    ) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        for (Bullet b : bullets) {
            float fadeProgress = b.hasCollided
                ? Math.max(0f, b.fadeTimer / 0.15f)
                : 1f;

            for (int i = 0; i < b.trail.size - 1; i++) {
                Vector2 p1 = b.trail.get(i);
                Vector2 p2 = b.trail.get(i + 1);

                float alpha1 =
                    1f - ((float) i / b.trail.size);

                float alpha2 =
                    1f - ((float) (i + 1) / b.trail.size);

                float avgAlpha =
                    (alpha1 + alpha2) / 2f * fadeProgress;

                shapeRenderer.setColor(
                    1f,
                    1f,
                    0.8f,
                    avgAlpha * 0.7f
                );

                shapeRenderer.line(
                    p1.x,
                    p1.y,
                    p2.x,
                    p2.y
                );
            }
        }

        shapeRenderer.end();
    }

    public void draw(SpriteBatch batch) {
        for (Bullet b : bullets) {
            TextureRegion region = bulletRegions.get(b.weapon);

            if (region == null) {
                region = bulletRegions.get(WeaponProfile.PISTOL);
            }

            float angleDeg = (float) Math.toDegrees(
                Math.atan2(b.vy, b.vx)
            );

            float width = region.getRegionWidth() * 0.15f;
            float height = region.getRegionHeight() * 0.15f;

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

    public List<Bullet> getBullets() {
        return Collections.unmodifiableList(bullets);
    }

    public List<ImpactManager.Impact> getRecentImpacts() {
        return impactManager.getRecentImpacts();
    }

    public float getBulletRadius() {
        return BULLET_RADIUS;
    }

    public int getActiveCount() {
        return bullets.size();
    }

    public void dispose() {
        for (Texture tex : bulletTextures.values()) {
            if (tex != null) {
                tex.dispose();
            }
        }
    }

    public void removeBullet(Bullet bullet) {
        bullets.remove(bullet);
    }
}
