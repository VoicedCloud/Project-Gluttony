package com.github.glutton.player;

import com.github.glutton.weapons.WeaponState;

public class PlayerState {

	public float x, y;
	public float velocityY = 0f;
	public boolean facingRight = true;
	public boolean isGrounded = false;
	public boolean canDoubleJump = true;

	public final WeaponState weaponState = new WeaponState();

	public final float spriteScale = 0.3f;
	public final float gunPivotFrameX = 61f;
	public final float gunPivotFrameY = 85f;

	public float gunScale = 0.15f;
	public float muzzleOffsetX = 50f;
	public float muzzleOffsetY = 37f;

	public float getSpriteDrawX() {
		return (x - width / 2f) - spriteOffsetX;
	}

	public float getSpriteDrawY() {
		return (y - height / 2f) - spriteOffsetY;
	}

	public float getGunPivotWorldX() {
		float localX = facingRight ? gunPivotFrameX * spriteScale : (128f - gunPivotFrameX) * spriteScale;
		return getSpriteDrawX() + localX;
	}

	public float getGunPivotWorldY() {
		float localY = (128f - gunPivotFrameY) * spriteScale;
		return getSpriteDrawY() + localY;
	}

	public enum MovementState {
		IDLE, WALK, RUN
	}
	public MovementState movementState = MovementState.IDLE;

	public final float width = 32f * 0.3f;
	public final float height = 64f * 0.3f;

	public final float spriteOffsetX = 48f * 0.3f;
	public final float spriteOffsetY = 0f * 0.3f;
	public final float spriteFrameSize = 128f * 0.3f;

	public final float moveSpeed = 90.0f;
	public final float walkSpeed = moveSpeed / 2f;
	public final float runSpeed = moveSpeed;

	public final float jumpStrength = 315.0f;
	public final float gravity = -1200.0f;
}