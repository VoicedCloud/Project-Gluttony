package com.github.glutton.components;

import com.artemis.Component;
import com.github.glutton.weapons.WeaponProfile;

public class PlayerComponent extends Component {
	// === HEALTH & STATE ===

	public int maxHealth = 100;
	public int currentHealth = 100;
	public boolean isDead = false;
	public long deathTime = 0;

	// === MOVEMENT SPEEDS & PHYSICS CONSTANTS ===
	public final float maxSpeed = 220.0f;
	public final float jumpStrength = 200.0f; // Box2D impulse scalar
	public final float climbSpeed = 4.0f;

	// === MOVEMENT STATES ===
	public boolean isGrounded = false;
	public boolean isClimbing = false;
	public boolean canDoubleJump = true;
	public boolean isHuggingWall = false;
	public boolean touchingWallLeft = false;
	public boolean touchingWallRight = false;

	// Wall Jump & Charge Counters
	public int wallHoldFrames = 0;
	public final int minWallJumpChargeFrames = 20;
	public boolean spaceHeld = false;

	// === WEAPON TRACKING ===
	public WeaponProfile activeWeapon = WeaponProfile.PISTOL;
	public int currentAmmo = WeaponProfile.PISTOL.maxAmmo;
	public boolean isReloading = false;
	public long reloadStartTime = 0;
	public long lastFireTime = 0;
	public float aimAngleRad = 0.0f;
	public boolean facingRight = true;
}
