package com.github.glutton.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.glutton.weapons.WeaponProfile;
import com.github.glutton.weapons.BulletManager;
import com.github.glutton.weapons.WeaponState;
import com.github.glutton.world.LevelCollision;
import com.github.glutton.player.PlayerGunRenderer;

public class PlayerController {

    private final OrthographicCamera camera;
    private final Vector3 mouseWorldPos = new Vector3();
    private final BulletManager bulletManager;
    private final PlayerGunRenderer gunRenderer;

    public PlayerController(OrthographicCamera camera, BulletManager bulletManager, PlayerGunRenderer gunRenderer) {
        this.camera = camera;
        this.bulletManager = bulletManager;
        this.gunRenderer = gunRenderer;
    }

    public void update(PlayerState player, float delta, LevelCollision collision) {
        if (delta <= 0f) {
            delta = 1f / 60f;
        }
        if (delta > 0.1f) {
            delta = 0.1f;
        }

        handleMovement(player, delta, collision);
        handleAiming(player);
        handleWeaponInput(player);
        handleFiring(player);

        camera.position.set(player.x, player.y + 32f, 0);
        camera.update();
    }

    private void handleMovement(PlayerState player, float delta, LevelCollision collision) {
        float halfWidth = player.width / 2f;
        float halfHeight = player.height / 2f;
        Rectangle wall = collision.wall;

        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        float currentSpeed = isRunning ? player.runSpeed : player.walkSpeed;

        float nextX = player.x;
        boolean isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            nextX -= currentSpeed * delta;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            nextX += currentSpeed * delta;
            isMoving = true;
        }

        player.movementState = !isMoving ? PlayerState.MovementState.IDLE
                : (isRunning ? PlayerState.MovementState.RUN : PlayerState.MovementState.WALK);

        Rectangle horizontalRect = new Rectangle(nextX - halfWidth, player.y - halfHeight, player.width, player.height);
        if (horizontalRect.overlaps(wall)) {
            if (nextX > player.x) {
                nextX = wall.x - halfWidth;
            } else if (nextX < player.x) {
                nextX = wall.x + wall.width + halfWidth;
            }
        }
        player.x = nextX;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (player.isGrounded) {
                player.velocityY = player.jumpStrength;
                player.isGrounded = false;
            } else if (player.canDoubleJump) {
                player.velocityY = player.jumpStrength;
                player.canDoubleJump = false;
            }
        }

        player.velocityY += player.gravity * delta;
        player.y += player.velocityY * delta;

        boolean xOverlapsWall = (player.x + halfWidth > wall.x) && (player.x - halfWidth < wall.x + wall.width);
        float effectiveGroundTop = xOverlapsWall ? (wall.y + wall.height) : collision.floorTop;

        boolean groundedThisFrame = false;
        if (player.velocityY <= 0f && (player.y - halfHeight) <= effectiveGroundTop) {
            player.y = effectiveGroundTop + halfHeight;
            player.velocityY = 0f;
            groundedThisFrame = true;
        }

        player.isGrounded = groundedThisFrame;
        if (player.isGrounded) {
            player.canDoubleJump = true;
        }
    }

    private void handleAiming(PlayerState player) {
        mouseWorldPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldPos);

        player.facingRight = mouseWorldPos.x >= player.x;

        Vector2 muzzlePos = gunRenderer.getMuzzleWorldPos(player);
        player.weaponState.aimAngleRad = (float) Math.atan2(mouseWorldPos.y - muzzlePos.y, mouseWorldPos.x - muzzlePos.x);
    }

    public void startReload(WeaponState ws) {
        if (ws.isReloading || ws.currentAmmo >= ws.activeWeapon.maxAmmo) {
            return;
        }
        ws.isReloading = true;
        ws.reloadStartTime = TimeUtils.millis();
        ws.playedSlideLock = false;
        ws.playedReload1 = false;
        ws.playedReload2 = false;
        ws.playedSlideRelease = false;
    }

    private void handleWeaponInput(PlayerState player) {
        WeaponState ws = player.weaponState;

        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !ws.isReloading) {
            WeaponProfile[] values = WeaponProfile.values();
            ws.activeWeapon = values[(ws.activeWeapon.ordinal() + 1) % values.length];
            ws.currentAmmo = ws.activeWeapon.maxAmmo;
        }

        // Manual reload trigger via 'R'
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            startReload(ws);
        }

        // Consolidated reload update loop
        if (ws.isReloading) {
            long elapsed = TimeUtils.timeSinceMillis(ws.reloadStartTime);
            float progress = Math.min(1f, (float) elapsed / ws.reloadMs);

            if (!ws.playedSlideLock && progress >= 0.00f) {
                gunRenderer.playSlideLock();
                ws.playedSlideLock = true;
            }
            if (!ws.playedReload1 && progress >= 0.8f) { // Dropped from 0.16f to trigger earlier
                gunRenderer.playReload1();
                ws.playedReload1 = true;
            }
            if (!ws.playedReload2 && progress >= 0.465f) { // Dropped from 0.67f to trigger earlier
                gunRenderer.playReload2();
                ws.playedReload2 = true;
            }
            if (!ws.playedSlideRelease && progress >= 0.735f) { // Dropped from 0.88f to trigger earlier
                gunRenderer.playSlideRelease();
                ws.playedSlideRelease = true;
            }

            if (elapsed >= ws.reloadMs) {
                ws.currentAmmo = ws.activeWeapon.maxAmmo;
                ws.isReloading = false;
            }
        }
    }

    private void handleFiring(PlayerState player) {
        WeaponState ws = player.weaponState;
        boolean isShooting = ws.activeWeapon.isAutomatic ? Gdx.input.isTouched() : Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        if (isShooting && !ws.isReloading && ws.currentAmmo > 0
                && TimeUtils.timeSinceMillis(ws.lastFireTime) >= ws.activeWeapon.fireDelayMs) {
            ws.currentAmmo--;
            ws.lastFireTime = TimeUtils.millis();

            // Play stacking gunshot sound
            gunRenderer.playShootSound();

            ws.preFireX = player.x;
            ws.preFireY = player.y;
            ws.preFireTimestamp = TimeUtils.millis();

            WeaponProfile weapon = ws.activeWeapon;

            float recoilAngle = ws.aimAngleRad + (float) Math.PI;
            ws.recoilVecX = (float) Math.cos(recoilAngle) * weapon.recoilForce;
            ws.recoilVecY = (float) Math.sin(recoilAngle) * weapon.recoilForce;
            ws.recoilTimestamp = TimeUtils.millis();

            Vector2 muzzlePos = gunRenderer.getMuzzleWorldPos(player);
            for (int i = 0; i < weapon.pellets; i++) {
                float spreadRad = (float) Math.toRadians((Math.random() - 0.5) * 2.0 * weapon.spreadDegrees);
                float shotAngle = ws.aimAngleRad + spreadRad;
                bulletManager.spawn(muzzlePos.x, muzzlePos.y, shotAngle, weapon.bulletSpeed, weapon.damage, weapon);
            }

            if (ws.currentAmmo <= 0) {
                startReload(ws);
                return; // Stop firing sequence until reloaded
            }
        }
    }
}
