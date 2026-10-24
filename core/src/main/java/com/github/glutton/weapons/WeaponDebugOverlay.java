package com.github.glutton.weapons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.TimeUtils;

public class WeaponDebugOverlay {

    private final BitmapFont font;

    public WeaponDebugOverlay() {
        font = new BitmapFont();
        font.getData().setScale(0.08f);
        font.setColor(Color.WHITE);
    }

    public void draw(ShapeRenderer shapeRenderer, SpriteBatch batch, Matrix4 projectionMatrix, WeaponDebugContext ctx) {
        WeaponProfile weapon = ctx.player.weaponState.activeWeapon;

        float viewWidth = ctx.camera.viewportWidth * ctx.camera.zoom;
        float viewHeight = ctx.camera.viewportHeight * ctx.camera.zoom;
        float rayLength = (float) Math.hypot(viewWidth, viewHeight);

        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        shapeRenderer.setColor(Color.WHITE);
        drawRay(shapeRenderer, ctx.muzzlePos.x, ctx.muzzlePos.y, ctx.player.weaponState.aimAngleRad, rayLength);

        shapeRenderer.setColor(Color.CYAN);
        float halfSpreadRad = (float) Math.toRadians(weapon.spreadDegrees);
        drawRay(shapeRenderer, ctx.muzzlePos.x, ctx.muzzlePos.y, ctx.player.weaponState.aimAngleRad + halfSpreadRad, rayLength);
        drawRay(shapeRenderer, ctx.muzzlePos.x, ctx.muzzlePos.y, ctx.player.weaponState.aimAngleRad - halfSpreadRad, rayLength);

        float bulletRadius = ctx.bulletManager.getBulletRadius();
        for (Bullet b : ctx.bulletManager.getBullets()) {
            shapeRenderer.setColor(Color.ORANGE);
            shapeRenderer.line(b.spawnX, b.spawnY, b.x, b.y);

            shapeRenderer.setColor(Color.MAGENTA);
            float speed = (float) Math.sqrt(b.vx * b.vx + b.vy * b.vy);
            if (speed > 0.001f) {
                float dirX = b.vx / speed;
                float dirY = b.vy / speed;
                shapeRenderer.line(b.x, b.y, b.x + dirX * 10f, b.y + dirY * 10f);
            }

            shapeRenderer.setColor(Color.LIME);
            shapeRenderer.line(b.x, b.y, b.x + b.vx * (1f / 60f), b.y + b.vy * (1f / 60f));

            shapeRenderer.setColor(Color.RED);
            shapeRenderer.circle(b.x, b.y, bulletRadius + 1f, 8);
        }

        for (ImpactManager.Impact imp : ctx.bulletManager.getRecentImpacts()) {
            shapeRenderer.setColor(Color.WHITE);
            shapeRenderer.circle(imp.x, imp.y, 1.5f, 6);
        }

        float barY = ctx.player.y + ctx.player.height / 2f + 8f;
        float barWidth = 20f;
        float barHeight = 2f;

        long sinceFire = TimeUtils.timeSinceMillis(ctx.player.weaponState.lastFireTime);
        float cooldownPct = Math.min(1f, sinceFire / (float) weapon.fireDelayMs);
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(ctx.player.x - barWidth / 2f, barY, barWidth, barHeight);
        shapeRenderer.setColor(cooldownPct >= 1f ? Color.GREEN : Color.YELLOW);
        shapeRenderer.rect(ctx.player.x - barWidth / 2f, barY, barWidth * cooldownPct, barHeight);

        if (ctx.player.weaponState.isReloading) {
            long sinceReload = TimeUtils.timeSinceMillis(ctx.player.weaponState.reloadStartTime);
            float reloadPct = Math.min(1f, sinceReload / (float) weapon.reloadMs);
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(ctx.player.x - barWidth / 2f, barY + 4f, barWidth, barHeight);
            shapeRenderer.setColor(Color.SKY);
            shapeRenderer.rect(ctx.player.x - barWidth / 2f, barY + 4f, barWidth * reloadPct, barHeight);
        }

        shapeRenderer.end();

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(weapon.isAutomatic ? Color.GREEN : Color.ORANGE);
        shapeRenderer.circle(ctx.player.x + barWidth / 2f + 3f, barY, 1.5f, 8);
        shapeRenderer.end();

        batch.setProjectionMatrix(projectionMatrix);
        batch.begin();
        for (ImpactManager.Impact imp : ctx.bulletManager.getRecentImpacts()) {
            font.draw(batch, String.valueOf(imp.damage), imp.x + 2f, imp.y + imp.age * 10f + 4f);
        }
        batch.end();
    }

    private void drawRay(ShapeRenderer shapeRenderer, float x, float y, float angleRad, float length) {
        shapeRenderer.line(x, y, x + (float) Math.cos(angleRad) * length, y + (float) Math.sin(angleRad) * length);
    }

    public void dispose() {
        font.dispose();
    }
}
