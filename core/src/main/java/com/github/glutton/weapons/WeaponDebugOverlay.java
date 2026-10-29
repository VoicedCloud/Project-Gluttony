package com.github.glutton.weapons;

// Provides colors for drawing debug lines, shapes, and text
import com.badlogic.gdx.graphics.Color;

// Provides the font used to display damage numbers
import com.badlogic.gdx.graphics.g2d.BitmapFont;

// Allows drawing text and sprites
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

// Allows drawing debug shapes such as lines, circles, and rectangles
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

// Provides the projection matrix used to align rendering with the camera
import com.badlogic.gdx.math.Matrix4;

// Provides timing utilities for measuring elapsed time
import com.badlogic.gdx.utils.TimeUtils;

public class WeaponDebugOverlay {

    // Font used to display damage numbers above impact positions
    private final BitmapFont font;

    // Creates the font and configures its size and color
    public WeaponDebugOverlay() {
        font = new BitmapFont();

        // Use a small font suitable for the game's world coordinates
        font.getData().setScale(0.08f);

        // Set damage text to white
        font.setColor(Color.WHITE);
    }

    // Draws weapon debugging information, including aim rays,
    // bullet paths, impact markers, and weapon timers
    public void draw(
            ShapeRenderer shapeRenderer,
            SpriteBatch batch,
            Matrix4 projectionMatrix,
            WeaponDebugContext ctx) {

        // Get the weapon currently equipped by the player
        WeaponProfile weapon = ctx.player.weaponState.activeWeapon;

        // Calculate the visible camera width and height, accounting for zoom
        float viewWidth = ctx.camera.viewportWidth * ctx.camera.zoom;
        float viewHeight = ctx.camera.viewportHeight * ctx.camera.zoom;

        // Calculate the diagonal length of the camera's view.
        // This allows the aim rays to extend across the visible game area.
        float rayLength = (float) Math.hypot(viewWidth, viewHeight);

        // Use the supplied projection matrix for drawing debug shapes
        shapeRenderer.setProjectionMatrix(projectionMatrix);

        // Begin drawing unfilled shapes such as lines and circles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw a white line showing the player's current aiming direction
        shapeRenderer.setColor(Color.WHITE);
        drawRay(
                shapeRenderer,
                ctx.muzzlePos.x,
                ctx.muzzlePos.y,
                ctx.player.weaponState.aimAngleRad,
                rayLength
        );

        // Calculate half of the weapon's bullet spread angle in radians
        float halfSpreadRad = (float) Math.toRadians(weapon.spreadDegrees);

        // Draw cyan lines showing the edges of the weapon's spread.
        // These lines help visualize the possible firing directions.
        shapeRenderer.setColor(Color.CYAN);
        drawRay(
                shapeRenderer,
                ctx.muzzlePos.x,
                ctx.muzzlePos.y,
                ctx.player.weaponState.aimAngleRad + halfSpreadRad,
                rayLength
        );
        drawRay(
                shapeRenderer,
                ctx.muzzlePos.x,
                ctx.muzzlePos.y,
                ctx.player.weaponState.aimAngleRad - halfSpreadRad,
                rayLength
        );

        // Get the bullet radius used for displaying collision boundaries
        float bulletRadius = ctx.bulletManager.getBulletRadius();

        // Draw debugging information for every active bullet
        for (Bullet b : ctx.bulletManager.getBullets()) {

            // Orange line showing the bullet's path from its spawn position
            // to its current position
            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.line(b.spawnX, b.spawnY, b.x, b.y);

            // Calculate the bullet's current speed
            shapeRenderer.setColor(Color.MAGENTA);
            float speed = (float) Math.sqrt(b.vx * b.vx + b.vy * b.vy);

            // Draw a short magenta line showing the bullet's direction.
            // The speed check prevents division by zero or a near-zero value.
            if (speed > 0.001f) {
                float dirX = b.vx / speed;
                float dirY = b.vy / speed;

                shapeRenderer.line(
                        b.x,
                        b.y,
                        b.x + dirX * 10f,
                        b.y + dirY * 10f
                );
            }

            // Draw a lime line showing the bullet's approximate movement
            // over one frame at 60 frames per second
            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.line(
                    b.x,
                    b.y,
                    b.x + b.vx * (1f / 60f),
                    b.y + b.vy * (1f / 60f)
            );

            // Draw a red circle around the bullet to visualize its radius
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.circle(b.x, b.y, bulletRadius + 1f, 8);
        }

        // Draw a white circle at each recent bullet impact position
        for (ImpactManager.Impact imp : ctx.bulletManager.getRecentImpacts()) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(imp.x, imp.y, 1.5f, 6);
        }

        // Calculate the position above the player's character
        // where the weapon timer bars will appear
        float barY = ctx.player.y + ctx.player.height / 2f + 8f;

        // Set the dimensions of the weapon timer bars
        float barWidth = 20f;
        float barHeight = 2f;

        // Calculate the time elapsed since the last shot was fired
        long sinceFire = TimeUtils.timeSinceMillis(
                ctx.player.weaponState.lastFireTime
        );

        // Calculate the firing cooldown progress, capped at 100%.
        // This assumes fireDelayMs is greater than zero.
        float cooldownPct = Math.min(
                1f,
                sinceFire / (float) weapon.fireDelayMs
        );

        // Draw the dark background of the firing cooldown bar
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(
                ctx.player.x - barWidth / 2f,
                barY,
                barWidth,
                barHeight
        );

        // Show green when the cooldown is complete;
        // otherwise, show yellow while waiting
        shapeRenderer.setColor(
                cooldownPct >= 1f ? Color.GREEN : Color.YELLOW
        );
        shapeRenderer.rect(
                ctx.player.x - barWidth / 2f,
                barY,
                barWidth * cooldownPct,
                barHeight
        );

        // Display a reload progress bar only while the player is reloading
        if (ctx.player.weaponState.isReloading) {

            // Calculate the time elapsed since reloading started
            long sinceReload = TimeUtils.timeSinceMillis(
                    ctx.player.weaponState.reloadStartTime
            );

            // Calculate reload progress, capped at 100%.
            // This assumes reloadMs is greater than zero.
            float reloadPct = Math.min(
                    1f,
                    sinceReload / (float) weapon.reloadMs
            );

            // Draw the dark background of the reload progress bar
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(
                    ctx.player.x - barWidth / 2f,
                    barY + 4f,
                    barWidth,
                    barHeight
            );

            // Draw the sky-blue portion representing reload progress
            shapeRenderer.setColor(Color.SKY);
            shapeRenderer.rect(
                    ctx.player.x - barWidth / 2f,
                    barY + 4f,
                    barWidth * reloadPct,
                    barHeight
            );
        }

        // Finish drawing all line-based debug shapes
        shapeRenderer.end();

        // Begin drawing filled shapes
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Display a small status indicator beside the player:
        // green means the weapon is automatic, orange means it is not
        shapeRenderer.setColor(
                weapon.isAutomatic ? Color.GREEN : Color.ORANGE
        );
        shapeRenderer.circle(
                ctx.player.x + barWidth / 2f + 3f,
                barY,
                1.5f,
                8
        );

        // Finish drawing filled debug shapes
        shapeRenderer.end();

        // Apply the same projection matrix to the text and sprite batch
        batch.setProjectionMatrix(projectionMatrix);

        // Begin drawing text
        batch.begin();

        // Display the damage value for each recent bullet impact.
        // The text moves upward as the impact gets older.
        for (ImpactManager.Impact imp : ctx.bulletManager.getRecentImpacts()) {
            font.draw(
                    batch,
                    String.valueOf(imp.damage),
                    imp.x + 2f,
                    imp.y + imp.age * 10f + 4f
            );
        }

        // Finish drawing text
        batch.end();
    }

    // Draws a line starting at (x, y) in the specified angle and length.
    // The angle must be in radians.
    private void drawRay(
            ShapeRenderer shapeRenderer,
            float x,
            float y,
            float angleRad,
            float length) {

        // Use cosine for horizontal movement and sine for vertical movement
        shapeRenderer.line(
                x,
                y,
                x + (float) Math.cos(angleRad) * length,
                y + (float) Math.sin(angleRad) * length
        );
    }

    // Releases the font's resources when this debug overlay is no longer needed
    public void dispose() {
        font.dispose();
    }
}