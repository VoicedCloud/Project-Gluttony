package com.github.glutton.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.utils.TimeUtils;
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

	public PlayerController(OrthographicCamera camera, BulletManager bulletManager, PlayerGunRenderer gunRenderer, SceneLoader sceneLoader) {
		this.camera = camera;
		this.bulletManager = bulletManager;
		this.gunRenderer = gunRenderer;
		Gdx.input.setCursorCatched(true);

		World world = sceneLoader.getWorld();

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = BodyDef.BodyType.DynamicBody;
		bodyDef.position.set(PhysicsConfig.PLAYER_SPAWN_X, PhysicsConfig.PLAYER_SPAWN_Y);
		bodyDef.fixedRotation = true;
		bodyDef.bullet = true;
		playerBody = world.createBody(bodyDef);

		float halfWidth = (PhysicsConfig.PLAYER_WIDTH_PX * PhysicsConfig.PLAYER_SPRITE_SCALE) / 2f;
		float halfHeight = (PhysicsConfig.PLAYER_HEIGHT_PX * PhysicsConfig.PLAYER_SPRITE_SCALE) / 2f;

		PolygonShape shape = new PolygonShape();
		shape.setAsBox(halfWidth, halfHeight);

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		fixtureDef.density = PhysicsConfig.PLAYER_DENSITY;
		fixtureDef.friction = PhysicsConfig.PLAYER_FRICTION;
		fixtureDef.restitution = PhysicsConfig.PLAYER_RESTITUTION;
		playerBody.createFixture(fixtureDef);
		shape.dispose();

		PolygonShape footShape = new PolygonShape();
		footShape.setAsBox(halfWidth * 0.9f, halfHeight * 0.2f, new Vector2(0f, -halfHeight), 0f);
		FixtureDef footDef = new FixtureDef();
		footDef.shape = footShape;
		footDef.isSensor = true;
		Fixture footFixture = playerBody.createFixture(footDef);
		footFixture.setUserData(FOOT_SENSOR_TAG);
		footShape.dispose();

		world.setContactListener(new GameContactListener(this));
	}

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
		return FOOT_SENSOR_TAG.equals(dataA) || FOOT_SENSOR_TAG.equals(dataB);
	}

	public void update(PlayerState player, float delta) {
		if (delta <= 0f) {
			delta = 1f / 60f;
		}
		if (delta > 0.1f) {
			delta = 0.1f;
		}

		if (player.isDead()) {
			player.respawn();
			playerBody.setTransform(PhysicsConfig.PLAYER_SPAWN_X, PhysicsConfig.PLAYER_SPAWN_Y, 0f);
			playerBody.setLinearVelocity(0f, 0f);
			return;
		}

		player.isGrounded = groundContacts > 0;
		if (player.isGrounded) {
			player.canDoubleJump = true;
		}

		handleMovement(player, delta);
		handleAiming(player);
		handleWeaponInput(player);
		handleFiring(player);

		Vector2 pos = playerBody.getPosition();
		player.x = pos.x;
		player.y = pos.y;

		camera.position.set(player.x, player.y + 32f, 0);
		camera.update();
	}

	private void handleMovement(PlayerState player, float delta) {
		boolean isRunning = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) || Gdx.input.isKeyPressed(Input.Keys.SHIFT_RIGHT);
		float currentSpeed = isRunning ? player.runSpeed : player.walkSpeed;

		boolean isMoving = false;
		float moveX = 0f;
		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			moveX -= currentSpeed;
			isMoving = true;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			moveX += currentSpeed;
			isMoving = true;
		}

		Vector2 vel = playerBody.getLinearVelocity();
		float clampedVelY = Math.max(vel.y, PhysicsConfig.MAX_FALL_SPEED);
		playerBody.setLinearVelocity(moveX, clampedVelY);

		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if (player.isGrounded) {
				playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, PhysicsConfig.JUMP_VELOCITY);
				player.isGrounded = false;
				player.jumpStartY = player.y;
				player.peakY = player.y;
			} else if (player.canDoubleJump) {
				playerBody.setLinearVelocity(playerBody.getLinearVelocity().x, PhysicsConfig.JUMP_VELOCITY);
				player.canDoubleJump = false;
				player.jumpStartY = player.y;
				player.peakY = player.y;
			}
		}

		if (!player.isGrounded && player.y > player.peakY) {
			player.peakY = player.y;
		}

		if (!player.isGrounded) {
			player.movementState = (playerBody.getLinearVelocity().y > 0f) ? PlayerState.MovementState.JUMP : PlayerState.MovementState.FALL;
		} else {
			player.movementState = !isMoving ? PlayerState.MovementState.IDLE
				: (isRunning ? PlayerState.MovementState.RUN : PlayerState.MovementState.WALK);
		}
	}

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

			float currentDist = (float) Math.hypot(ws.aimOffsetX, ws.aimOffsetY);
			if (currentDist > ws.aimRadius) {
				ws.aimOffsetX = (ws.aimOffsetX / currentDist) * ws.aimRadius;
				ws.aimOffsetY = (ws.aimOffsetY / currentDist) * ws.aimRadius;
			}
		}

		float targetWorldX = pivotX + ws.aimOffsetX;
		float targetWorldY = pivotY + ws.aimOffsetY;
		player.facingRight = targetWorldX >= pivotX;

		Vector2 muzzlePos = gunRenderer.getMuzzleWorldPos(player);
		ws.aimAngleRad = (float) Math.atan2(targetWorldY - muzzlePos.y, targetWorldX - muzzlePos.x);
		mouseWorldPos.set(targetWorldX, targetWorldY, 0);
	}

	public void startReload(WeaponState ws) {
		if (ws.isReloading || ws.currentAmmo >= ws.activeWeapon.maxAmmo) {
			return;
		}
		ws.isReloading = true;
		ws.reloadStartTime = TimeUtils.millis();
		for (WeaponProfile.ReloadMilestone m : ws.activeWeapon.milestones) {
			m.hasPlayed = false;
		}
	}

	private void handleWeaponInput(PlayerState player) {
		WeaponState ws = player.weaponState;
		if (Gdx.input.isKeyJustPressed(Input.Keys.E) && !ws.isReloading) {
			WeaponProfile[] values = WeaponProfile.values();
			ws.activeWeapon = values[(ws.activeWeapon.ordinal() + 1) % values.length];
			ws.currentAmmo = ws.activeWeapon.maxAmmo;
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			startReload(ws);
		}

		if (ws.isReloading) {
			long elapsed = TimeUtils.timeSinceMillis(ws.reloadStartTime);
			float progress = Math.min(1f, (float) elapsed / ws.activeWeapon.reloadMs);
			for (WeaponProfile.ReloadMilestone milestone : ws.activeWeapon.milestones) {
				if (!milestone.hasPlayed && progress >= milestone.progress) {
					if (milestone.sound != null) {
						milestone.sound.play();
					}
					milestone.hasPlayed = true;
				}
			}
			if (elapsed >= ws.activeWeapon.reloadMs) {
				ws.currentAmmo = ws.activeWeapon.maxAmmo;
				ws.isReloading = false;
				for (WeaponProfile.ReloadMilestone m : ws.activeWeapon.milestones) {
					m.hasPlayed = false;
				}
			}
		}
	}

	private void handleFiring(PlayerState player) {
		WeaponState ws = player.weaponState;
		WeaponProfile weapon = ws.activeWeapon;
		boolean isShooting = weapon.isAutomatic ? Gdx.input.isTouched() : Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);

		if (isShooting && !ws.isReloading && ws.currentAmmo > 0
			&& TimeUtils.timeSinceMillis(ws.lastFireTime) >= weapon.fireDelayMs) {

			ws.currentAmmo--;
			ws.lastFireTime = TimeUtils.millis();
			gunRenderer.playShootSound(weapon);

			Vector2 muzzlePos = gunRenderer.getMuzzleWorldPos(player);
			for (int i = 0; i < weapon.pellets; i++) {
				float spreadRad = (float) Math.toRadians((Math.random() - 0.5) * 2.0 * weapon.spreadDegrees);
				bulletManager.spawn(muzzlePos.x, muzzlePos.y, ws.aimAngleRad + spreadRad, weapon.bulletSpeed, weapon.damage, weapon);
			}

			ws.aimOffsetY += weapon.recoilForce * 2.5f;
			float currentDist = (float) Math.hypot(ws.aimOffsetX, ws.aimOffsetY);
			if (currentDist > ws.aimRadius) {
				ws.aimOffsetX = (ws.aimOffsetX / currentDist) * ws.aimRadius;
				ws.aimOffsetY = (ws.aimOffsetY / currentDist) * ws.aimRadius;
			}

			float pivotX = player.getGunPivotWorldX();
			float pivotY = player.getGunPivotWorldY();
			ws.aimAngleRad = (float) Math.atan2((pivotY + ws.aimOffsetY) - muzzlePos.y, (pivotX + ws.aimOffsetX) - muzzlePos.x);

			if (ws.currentAmmo <= 0) {
				startReload(ws);
			}
		}
	}
}