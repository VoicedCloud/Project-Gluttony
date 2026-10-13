package com.github.glutton.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.glutton.weapons.WeaponProfile;
import com.github.glutton.weapons.BulletManager;

public class PlayerController {

    private final BulletManager bulletManager;

    private final OrthographicCamera camera;
    private final Vector3 mouseWorldPos = new Vector3();

    private final float FLOOR_TOP = 64.0f;
    private final Rectangle floor = new Rectangle(-5000f, 0f, 15000f, FLOOR_TOP);
    private final Rectangle wall = new Rectangle(96f, 64f, 32f, 48f);

    public PlayerController(OrthographicCamera camera, BulletManager bulletManager) {
        this.camera = camera;
        this.bulletManager = bulletManager;
    }

    public void update(PlayerState player, float delta) {
        if (delta <= 0f) {
            delta = 1f / 60f;
        }
        if (delta > 0.1f) {
            delta = 0.1f;
        }

        float halfWidth = player.width / 2f;
        float halfHeight = player.height / 2f;

// 1. HORIZONTAL MOVEMENT & WALL COLLISION
        boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
        float currentSpeed = isRunning ? player.runSpeed : player.walkSpeed;

        float nextX = player.x;
        boolean isMoving = false;

        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            nextX -= currentSpeed * delta;
            player.facingRight = false;
            isMoving = true;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            nextX += currentSpeed * delta;
            player.facingRight = true;
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

        // Jump
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            if (player.isGrounded) {
                player.velocityY = player.jumpStrength;
                player.isGrounded = false;
            } else if (player.canDoubleJump) {
                player.velocityY = player.jumpStrength;
                player.canDoubleJump = false;
            }
        }

        // Gravity
        player.velocityY += player.gravity * delta;
        player.y += player.velocityY * delta;

        // 3. FLOOR & WALL VERTICAL COLLISION (unified: treat pillar top as ground where it overlaps)
        boolean xOverlapsWall = (player.x + halfWidth > wall.x) && (player.x - halfWidth < wall.x + wall.width);
        float effectiveGroundTop = xOverlapsWall ? (wall.y + wall.height) : FLOOR_TOP;

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

        // Aim angle
        mouseWorldPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        camera.unproject(mouseWorldPos);
        player.aimAngleRad = (float) Math.atan2(mouseWorldPos.y - player.y, mouseWorldPos.x - player.x);

        // Weapon switch / reload
        if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !player.isReloading) {
            WeaponProfile[] values = WeaponProfile.values();
            player.activeWeapon = values[(player.activeWeapon.ordinal() + 1) % values.length];
            player.currentAmmo = player.activeWeapon.maxAmmo;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.R) && !player.isReloading && player.currentAmmo < player.activeWeapon.maxAmmo) {
            player.isReloading = true;
            player.reloadStartTime = TimeUtils.millis();
        }
        if (player.isReloading && TimeUtils.timeSinceMillis(player.reloadStartTime) >= player.activeWeapon.reloadMs) {
            player.currentAmmo = player.activeWeapon.maxAmmo;
            player.isReloading = false;
        }

// Firing logic
        boolean isShooting = player.activeWeapon.isAutomatic ? Gdx.input.isTouched() : Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
        if (isShooting && !player.isReloading && player.currentAmmo > 0
                && TimeUtils.timeSinceMillis(player.lastFireTime) >= player.activeWeapon.fireDelayMs) {
            player.currentAmmo--;
            player.lastFireTime = TimeUtils.millis();

            WeaponProfile weapon = player.activeWeapon;
            float gunOffsetY = 10f; // rough world-unit offset so bullets spawn near chest/hand height, not feet
            for (int i = 0; i < weapon.pellets; i++) {
                float spreadRad = (float) Math.toRadians(
                        (Math.random() - 0.5) * 2.0 * weapon.spreadDegrees
                );
                float shotAngle = player.aimAngleRad + spreadRad;
                bulletManager.spawn(player.x, player.y + gunOffsetY, shotAngle, weapon.bulletSpeed, weapon.damage);
            }

            if (player.currentAmmo <= 0) {
                player.isReloading = true;
                player.reloadStartTime = TimeUtils.millis();
            }
        }

        // Camera follow with vertical offset to hide the void below the ground
        float cameraOffsetY = 32f;
        camera.position.set(player.x, player.y + cameraOffsetY, 0);
        camera.update();
    }
}
