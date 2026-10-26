package com.github.glutton.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import com.github.glutton.entities.Enemy;
import com.github.glutton.weapons.BulletManager;
import com.github.glutton.weapons.WeaponProfile;
import com.github.glutton.weapons.WeaponState;
import com.github.glutton.world.GameContactListener;
import com.github.glutton.world.PhysicsConfig;

import games.rednblack.editor.renderer.SceneLoader;

public class PlayerController {

    private final OrthographicCamera camera;
    private final Vector3 mouseWorldPos = new Vector3();

    private final BulletManager bulletManager;
    private final PlayerGunRenderer gunRenderer;
    private final Body playerBody;

    private int groundContacts = 0;

    private static final String FOOT_SENSOR_TAG = "foot";

    // ==================================================
    // DASH SYSTEM
    // ==================================================

    private boolean isDashing = false;

    private float dashTimer = 0f;
    private float dashCooldownTimer = 0f;

    private final float DASH_DURATION = 0.15f;
    private final float DASH_COOLDOWN = 1.5f;
    private final float DASH_SPEED = 900f;

    private float dashDirection = 1f;

    // ==================================================
    // PUSH SYSTEM
    // ==================================================

    private float pushCooldownTimer = 0f;

    private final float PUSH_COOLDOWN = 4.0f;
    private final float PUSH_RADIUS = 180f;
    private final int PUSH_DAMAGE = 15;

    private final float PUSH_FORCE_X = 350f;
    private final float PUSH_FORCE_Y = 180f;

    // ==================================================
    // CONSTRUCTOR
    // ==================================================

    public PlayerController(
            OrthographicCamera camera,
            BulletManager bulletManager,
            PlayerGunRenderer gunRenderer,
            SceneLoader sceneLoader) {

        this.camera = camera;
        this.bulletManager = bulletManager;
        this.gunRenderer = gunRenderer;

        Gdx.input.setCursorCatched(true);

        World world = sceneLoader.getWorld();

        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;

        bodyDef.position.set(
                PhysicsConfig.PLAYER_SPAWN_X,
                PhysicsConfig.PLAYER_SPAWN_Y
        );

        bodyDef.fixedRotation = true;
        bodyDef.bullet = true;

        playerBody = world.createBody(bodyDef);

        float halfWidth =
                (PhysicsConfig.PLAYER_WIDTH_PX
                        * PhysicsConfig.PLAYER_SPRITE_SCALE) / 2f;

        float halfHeight =
                (PhysicsConfig.PLAYER_HEIGHT_PX
                        * PhysicsConfig.PLAYER_SPRITE_SCALE) / 2f;

        PolygonShape shape = new PolygonShape();

        shape.setAsBox(halfWidth, halfHeight);

        FixtureDef fixtureDef = new FixtureDef();

        fixtureDef.shape = shape;
        fixtureDef.density = PhysicsConfig.PLAYER_DENSITY;
        fixtureDef.friction = PhysicsConfig.PLAYER_FRICTION;
        fixtureDef.restitution = PhysicsConfig.PLAYER_RESTITUTION;

        playerBody.createFixture(fixtureDef);

        shape.dispose();

        // ==================================================
        // FOOT SENSOR
        // ==================================================

        PolygonShape footShape = new PolygonShape();

        footShape.setAsBox(
                halfWidth * 0.9f,
                halfHeight * 0.2f,
                new Vector2(0f, -halfHeight),
                0f
        );

        FixtureDef footDef = new FixtureDef();

        footDef.shape = footShape;
        footDef.isSensor = true;

        Fixture footFixture = playerBody.createFixture(footDef);

        footFixture.setUserData(FOOT_SENSOR_TAG);

        footShape.dispose();

        world.setContactListener(new GameContactListener(this));
    }

    // ==================================================
    // CONTACT HANDLING
    // ==================================================

    public void handleBeginContact(Contact contact) {

        if (isFootSensor(contact)) {
            groundContacts++;
        }
    }

    public void handleEndContact(Contact contact) {

        if (isFootSensor(contact)) {
            groundContacts = Math.max(0, groundContacts - 1);
        }
    }

    private boolean isFootSensor(Contact contact) {

        Object dataA = contact.getFixtureA().getUserData();
        Object dataB = contact.getFixtureB().getUserData();

        return FOOT_SENSOR_TAG.equals(dataA)
                || FOOT_SENSOR_TAG.equals(dataB);
    }

    // ==================================================
    // MAIN UPDATE
    // ==================================================

    public void update(PlayerState player, float delta) {

        if (delta <= 0f) {
            delta = 1f / 60f;
        }

        if (delta > 0.1f) {
            delta = 0.1f;
        }

        // ==================================================
        // PLAYER DEATH / RESPAWN
        // ==================================================

        if (player.isDead()) {

            player.respawn();

            playerBody.setTransform(
                    PhysicsConfig.PLAYER_SPAWN_X,
                    PhysicsConfig.PLAYER_SPAWN_Y,
                    0f
            );

            playerBody.setLinearVelocity(0f, 0f);

            // Reset ability states on respawn.
            isDashing = false;
            dashTimer = 0f;
            dashCooldownTimer = 0f;
            pushCooldownTimer = 0f;

            return;
        }

        // ==================================================
        // UPDATE ABILITY COOLDOWNS
        // ==================================================

        dashCooldownTimer = Math.max(
                0f,
                dashCooldownTimer - delta
        );

        pushCooldownTimer = Math.max(
                0f,
                pushCooldownTimer - delta
        );

        // ==================================================
        // GROUNDING
        // ==================================================

        player.isGrounded = groundContacts > 0;

        if (player.isGrounded) {
            player.canDoubleJump = true;
        }

        // ==================================================
        // PLAYER CONTROLS
        // ==================================================

        handleMovement(player, delta);
        handleAiming(player);
        handleWeaponInput(player);
        handleFiring(player);

        // ==================================================
        // PUSH SKILL
        // ==================================================

        if (Gdx.input.isKeyJustPressed(Input.Keys.F)
                && pushCooldownTimer <= 0f) {

            activatePush();
        }

        // ==================================================
        // UPDATE PLAYER POSITION
        // ==================================================

        Vector2 pos = playerBody.getPosition();

        player.x = pos.x;
        player.y = pos.y;

        camera.position.set(
                player.x,
                player.y + 32f,
                0f
        );

        camera.update();
    }

    // ==================================================
    // MOVEMENT
    // ==================================================

    private void handleMovement(PlayerState player, float delta) {

        // ==================================================
        // NORMAL MOVEMENT
        // ==================================================

        boolean isRunning =
                Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);

        float currentSpeed = isRunning
                ? player.runSpeed
                : player.walkSpeed;

        boolean movingLeft =
                Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT);

        boolean movingRight =
                Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

        boolean isMoving = false;

        float moveX = 0f;

        if (movingLeft) {
            moveX -= currentSpeed;
            isMoving = true;
        }

        if (movingRight) {
            moveX += currentSpeed;
            isMoving = true;
        }

        // ==================================================
        // START DASH
        // ==================================================

        if (Gdx.input.isKeyJustPressed(Input.Keys.Q)
                && dashCooldownTimer <= 0f
                && !isDashing) {

            if (movingLeft && !movingRight) {

                dashDirection = -1f;

            } else if (movingRight && !movingLeft) {

                dashDirection = 1f;

            } else {

                dashDirection = player.facingRight ? 1f : -1f;
            }

            isDashing = true;

            dashTimer = DASH_DURATION;
            dashCooldownTimer = DASH_COOLDOWN;
        }

        // ==================================================
        // APPLY DASH
        // ==================================================

        if (isDashing) {

            dashTimer -= delta;

            moveX = dashDirection * DASH_SPEED;

            isMoving = true;

            if (dashTimer <= 0f) {

                dashTimer = 0f;
                isDashing = false;

                if (movingLeft && !movingRight) {

                    moveX = -currentSpeed;

                } else if (movingRight && !movingLeft) {

                    moveX = currentSpeed;

                } else {

                    moveX = 0f;
                }
            }
        }

        // ==================================================
        // APPLY PHYSICS
        // ==================================================

        Vector2 vel = playerBody.getLinearVelocity();

        float clampedVelY = Math.max(
                vel.y,
                PhysicsConfig.MAX_FALL_SPEED
        );

        playerBody.setLinearVelocity(
                moveX,
                clampedVelY
        );

        // ==================================================
        // JUMP AND DOUBLE JUMP
        // ==================================================

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {

            if (player.isGrounded) {

                playerBody.setLinearVelocity(
                        playerBody.getLinearVelocity().x,
                        PhysicsConfig.JUMP_VELOCITY
                );

                player.isGrounded = false;

                player.jumpStartY = player.y;
                player.peakY = player.y;

            } else if (player.canDoubleJump) {

                playerBody.setLinearVelocity(
                        playerBody.getLinearVelocity().x,
                        PhysicsConfig.JUMP_VELOCITY
                );

                player.canDoubleJump = false;

                player.jumpStartY = player.y;
                player.peakY = player.y;
            }
        }

        // ==================================================
        // TRACK JUMP HEIGHT
        // ==================================================

        if (!player.isGrounded && player.y > player.peakY) {
            player.peakY = player.y;
        }

        // ==================================================
        // MOVEMENT ANIMATION
        // ==================================================

        if (!player.isGrounded) {

            player.movementState =
                    playerBody.getLinearVelocity().y > 0f
                    ? PlayerState.MovementState.JUMP
                    : PlayerState.MovementState.FALL;

        } else {

            player.movementState = !isMoving
                    ? PlayerState.MovementState.IDLE
                    : (isRunning
                        ? PlayerState.MovementState.RUN
                        : PlayerState.MovementState.WALK);
        }
    }

    // ==================================================
    // PUSH SKILL
    // ==================================================

    private void activatePush() {

        if (pushCooldownTimer > 0f) {
            return;
        }

        World world = playerBody.getWorld();

        // Do not modify the physics world while it is locked.
        if (world.isLocked()) {
            return;
        }

        Vector2 playerPosition = playerBody.getPosition();

        Array<Body> bodies = new Array<>();

        world.getBodies(bodies);

        boolean pushedAtLeastOneEnemy = false;

        for (Body body : bodies) {

            if (body == playerBody) {
                continue;
            }

            Object userData = body.getUserData();

            // Enemy bodies store their Enemy instance as user data.
            if (!(userData instanceof Enemy)) {
                continue;
            }

            Enemy enemy = (Enemy) userData;

            if (enemy.isDestroyed() || enemy.isBodyDisposed()) {
                continue;
            }

            Vector2 enemyPosition = body.getPosition();

            float dx = enemyPosition.x - playerPosition.x;
            float dy = enemyPosition.y - playerPosition.y;

            float distanceSquared = dx * dx + dy * dy;

            // Only push enemies inside the skill radius.
            if (distanceSquared > PUSH_RADIUS * PUSH_RADIUS) {
                continue;
            }

            float distance = (float) Math.sqrt(distanceSquared);

            // Avoid division by zero when the enemy overlaps the player.
            if (distance < 0.001f) {

                dx = 1f;
                dy = 0f;

                distance = 1f;
            }

            // Normalize the direction away from the player.
            dx /= distance;
            dy /= distance;

            // Apply immediate knockback.
            body.setLinearVelocity(
                    dx * PUSH_FORCE_X,
                    Math.max(0f, dy * PUSH_FORCE_Y)
            );

            // Damage the enemy.
            enemy.takeDamage(PUSH_DAMAGE);

            pushedAtLeastOneEnemy = true;
        }

        // Start the cooldown when the skill is successfully activated,
        // even if there are no enemies in range.
        pushCooldownTimer = PUSH_COOLDOWN;
    }

    // ==================================================
    // AIMING
    // ==================================================

    private void handleAiming(PlayerState player) {

        float pivotX = player.getGunPivotWorldX();
        float pivotY = player.getGunPivotWorldY();

        WeaponState ws = player.weaponState;

        float deltaX = Gdx.input.getDeltaX();
        float deltaY = -Gdx.input.getDeltaY();

        if (deltaX != 0f || deltaY != 0f) {

            float sensitivity = 0.75f;

            ws.aimOffsetX += deltaX * sensitivity;
            ws.aimOffsetY += deltaY * sensitivity;

            float currentDist = (float) Math.hypot(
                    ws.aimOffsetX,
                    ws.aimOffsetY
            );

            if (currentDist > ws.aimRadius) {

                ws.aimOffsetX =
                        (ws.aimOffsetX / currentDist) * ws.aimRadius;

                ws.aimOffsetY =
                        (ws.aimOffsetY / currentDist) * ws.aimRadius;
            }
        }

        float targetWorldX = pivotX + ws.aimOffsetX;
        float targetWorldY = pivotY + ws.aimOffsetY;

        player.facingRight = targetWorldX >= pivotX;

        Vector2 muzzlePos = gunRenderer.getMuzzleWorldPos(player);

        ws.aimAngleRad = (float) Math.atan2(
                targetWorldY - muzzlePos.y,
                targetWorldX - muzzlePos.x
        );

        mouseWorldPos.set(
                targetWorldX,
                targetWorldY,
                0f
        );
    }

    // ==================================================
    // RELOADING
    // ==================================================

    public void startReload(WeaponState ws) {

        if (ws.isReloading
                || ws.currentAmmo >= ws.activeWeapon.maxAmmo) {
            return;
        }

        ws.isReloading = true;
        ws.reloadStartTime = TimeUtils.millis();

        for (WeaponProfile.ReloadMilestone milestone
                : ws.activeWeapon.milestones) {

            milestone.hasPlayed = false;
        }
    }

    // ==================================================
    // WEAPON INPUT
    // ==================================================

    private void handleWeaponInput(PlayerState player) {

        WeaponState ws = player.weaponState;

        // Switch only between weapons the player owns.
        if (Gdx.input.isKeyJustPressed(Input.Keys.E)
                && !ws.isReloading) {

            WeaponProfile[] weapons = WeaponProfile.values();

            int currentIndex = ws.activeWeapon.ordinal();

            for (int i = 1; i < weapons.length; i++) {

                WeaponProfile nextWeapon =
                        weapons[(currentIndex + i) % weapons.length];

                if (ws.ownedWeapons.contains(nextWeapon)) {

                    ws.activeWeapon = nextWeapon;
                    ws.currentAmmo = nextWeapon.maxAmmo;

                    break;
                }
            }
        }

        // Press R to reload.
        if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
            startReload(ws);
        }

        // ==================================================
        // RELOAD PROGRESS
        // ==================================================

        if (ws.isReloading) {

            long elapsed =
                    TimeUtils.timeSinceMillis(ws.reloadStartTime);

            float progress = Math.min(
                    1f,
                    (float) elapsed / ws.activeWeapon.reloadMs
            );

            for (WeaponProfile.ReloadMilestone milestone
                    : ws.activeWeapon.milestones) {

                if (!milestone.hasPlayed
                        && progress >= milestone.progress) {

                    if (milestone.sound != null) {
                        milestone.sound.play();
                    }

                    milestone.hasPlayed = true;
                }
            }

            if (elapsed >= ws.activeWeapon.reloadMs) {

                ws.currentAmmo = ws.activeWeapon.maxAmmo;
                ws.isReloading = false;

                for (WeaponProfile.ReloadMilestone milestone
                        : ws.activeWeapon.milestones) {

                    milestone.hasPlayed = false;
                }
            }
        }
    }

    // ==================================================
    // FIRING
    // ==================================================

    private void handleFiring(PlayerState player) {

        WeaponState ws = player.weaponState;
        WeaponProfile weapon = ws.activeWeapon;

        // Right-click activates aim mode.
        boolean isAiming =
                Gdx.input.isButtonPressed(Input.Buttons.RIGHT);

        // Left-click fires the weapon.
        boolean isShooting = weapon.isAutomatic
                ? Gdx.input.isButtonPressed(Input.Buttons.LEFT)
                : Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

        // ==================================================
        // FIRE WEAPON
        // ==================================================

        if (isShooting
                && !ws.isReloading
                && ws.currentAmmo > 0
                && TimeUtils.timeSinceMillis(ws.lastFireTime)
                        >= weapon.fireDelayMs) {

            ws.currentAmmo--;

            ws.lastFireTime = TimeUtils.millis();

            gunRenderer.playShootSound(weapon);

            Vector2 muzzlePos =
                    gunRenderer.getMuzzleWorldPos(player);

            // ==================================================
            // SPAWN BULLETS
            // ==================================================

            for (int i = 0; i < weapon.pellets; i++) {

                float spreadRad = 0f;

                if (!isAiming) {

                    spreadRad = (float) Math.toRadians(
                            (Math.random() - 0.5)
                                    * 2.0
                                    * weapon.spreadDegrees
                    );
                }

                bulletManager.spawn(
                        muzzlePos.x,
                        muzzlePos.y,
                        ws.aimAngleRad + spreadRad,
                        weapon.bulletSpeed,
                        weapon.damage,
                        weapon
                );
            }

            // ==================================================
            // APPLY RECOIL
            // ==================================================

            ws.aimOffsetY += weapon.recoilForce * 2.5f;

            float currentDist = (float) Math.hypot(
                    ws.aimOffsetX,
                    ws.aimOffsetY
            );

            if (currentDist > ws.aimRadius) {

                ws.aimOffsetX =
                        (ws.aimOffsetX / currentDist) * ws.aimRadius;

                ws.aimOffsetY =
                        (ws.aimOffsetY / currentDist) * ws.aimRadius;
            }

            // ==================================================
            // UPDATE AIM ANGLE
            // ==================================================

            float pivotX = player.getGunPivotWorldX();
            float pivotY = player.getGunPivotWorldY();

            ws.aimAngleRad = (float) Math.atan2(
                    (pivotY + ws.aimOffsetY) - muzzlePos.y,
                    (pivotX + ws.aimOffsetX) - muzzlePos.x
            );

            // ==================================================
            // AUTO RELOAD
            // ==================================================

            if (ws.currentAmmo <= 0) {
                startReload(ws);
            }
        }
    }

    // ==================================================
    // COOLDOWN GETTERS FOR THE HUD
    // ==================================================

    public float getDashCooldownRemaining() {
        return Math.max(0f, dashCooldownTimer);
    }

    public float getPushCooldownRemaining() {
        return Math.max(0f, pushCooldownTimer);
    }
}