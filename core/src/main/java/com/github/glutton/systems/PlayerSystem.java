package com.github.glutton.systems;

import com.artemis.ComponentMapper;
import com.artemis.Aspect;
import com.artemis.systems.IteratingSystem;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.glutton.components.PlayerComponent;
import com.github.glutton.weapons.WeaponProfile;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.physics.PhysicsBodyComponent;

public class PlayerSystem extends IteratingSystem {

	protected ComponentMapper<PlayerComponent> playerMapper;
	protected ComponentMapper<TransformComponent> transformMapper;
	protected ComponentMapper<PhysicsBodyComponent> physicsMapper;

	private final OrthographicCamera camera;
	private final Vector3 mouseWorldPos = new Vector3();

	public PlayerSystem(OrthographicCamera camera) {
		super(Aspect.all(PlayerComponent.class, TransformComponent.class, PhysicsBodyComponent.class));
		this.camera = camera;
	}

	@Override
	protected void process(int entityId) {
		PlayerComponent player = playerMapper.get(entityId);
		TransformComponent transform = transformMapper.get(entityId);
		PhysicsBodyComponent physics = physicsMapper.get(entityId);
		Body body = physics.body;

		if (player == null || player.isDead || body == null) {
			return;
		}

		// 1. UPDATE AIM ANGLE FROM MOUSE WORLD POSITION
		mouseWorldPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		camera.unproject(mouseWorldPos);

		float pCenterX = transform.x + (transform.originX);
		float pCenterY = transform.y + (transform.originY);
		player.aimAngleRad = (float) Math.atan2(mouseWorldPos.y - pCenterY, mouseWorldPos.x - pCenterX);
		player.facingRight = mouseWorldPos.x >= pCenterX;

		// 2. HORIZONTAL MOVEMENT (A / D or Left / Right)
		Vector2 vel = body.getLinearVelocity();
		float targetVelX = 0f;

		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			targetVelX = -player.maxSpeed;
		} else if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			targetVelX = player.maxSpeed;
		}

		body.setLinearVelocity(targetVelX, vel.y);

		// 3. JUMPING MECHANICS (SPACEBAR)
		if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
			if (player.isGrounded) {
				body.setLinearVelocity(body.getLinearVelocity().x, player.jumpStrength);
				player.isGrounded = false;
			} else if (player.canDoubleJump) {
				body.setLinearVelocity(body.getLinearVelocity().x, player.jumpStrength);
				player.canDoubleJump = false;
			}
		}

		// Ground check via short downward raycast
		final boolean[] grounded = {false};
		Vector2 feetPos = body.getPosition();
		Vector2 rayEnd = new Vector2(feetPos.x, feetPos.y - 30f); // tune this distance to your collider size
		body.getWorld().rayCast((fixture, point, normal, fraction) -> {
			if (fixture.getBody() != body) {
				grounded[0] = true;
				return 0;
			}
			return -1;
		}, feetPos, rayEnd);

		player.isGrounded = grounded[0];
		if (player.isGrounded) {
			player.canDoubleJump = true;
		}

		// Sync TransformComponent from the live Box2D body so rendering & camera follow physics
		Vector2 bodyPos = body.getPosition();
		transform.x = bodyPos.x - transform.originX;
		transform.y = bodyPos.y - transform.originY;

		// 4. WEAPON SWITCHING (E KEY)
		if (Gdx.input.isKeyJustPressed(Input.Keys.E)) {
			if (!player.isReloading) {
				WeaponProfile[] values = WeaponProfile.values();
				player.activeWeapon = values[(player.activeWeapon.ordinal() + 1) % values.length];
				player.currentAmmo = player.activeWeapon.maxAmmo;
			}
		}

		// 5. RELOAD MECHANIC (R KEY)
		if (Gdx.input.isKeyJustPressed(Input.Keys.R)) {
			if (!player.isReloading && player.currentAmmo < player.activeWeapon.maxAmmo) {
				player.isReloading = true;
				player.reloadStartTime = TimeUtils.millis();
			}
		}

		if (player.isReloading && TimeUtils.timeSinceMillis(player.reloadStartTime) >= player.activeWeapon.reloadMs) {
			player.currentAmmo = player.activeWeapon.maxAmmo;
			player.isReloading = false;
		}

		// 6. FIRING MECHANICS (LEFT MOUSE CLICK)
		boolean isShooting = player.activeWeapon.isAutomatic ? Gdx.input.isTouched() : Gdx.input.isButtonJustPressed(Input.Buttons.LEFT);
		if (isShooting && !player.isReloading && player.currentAmmo > 0) {
			if (TimeUtils.timeSinceMillis(player.lastFireTime) >= player.activeWeapon.fireDelayMs) {
				player.currentAmmo--;
				player.lastFireTime = TimeUtils.millis();

				if (player.currentAmmo <= 0) {
					player.isReloading = true;
					player.reloadStartTime = TimeUtils.millis();
				}
			}
		}

		// 7. CAMERA TRACKING
		float centerX = transform.x + transform.originX;
		float centerY = transform.y + transform.originY;
		camera.position.set(centerX, centerY, 0);
		camera.update();
	}
}
