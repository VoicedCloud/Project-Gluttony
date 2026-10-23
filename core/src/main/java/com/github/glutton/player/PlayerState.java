package com.github.glutton.player;

import com.github.glutton.weapons.WeaponState;
import com.github.glutton.world.PhysicsConfig;

public class PlayerState {

    public float x, y;
    public float velocityY = 0f;
    public boolean facingRight = true;
    public boolean isGrounded = false;
    public boolean canDoubleJump = true;

    public final WeaponState weaponState = new WeaponState();

    public final float spriteScale = PhysicsConfig.PLAYER_SPRITE_SCALE;
    public final float gunPivotFrameX = 61f;
    public final float gunPivotFrameY = 85f;

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
        IDLE, WALK, RUN, JUMP, FALL
    }
    public MovementState movementState = MovementState.IDLE;

    public float jumpStartY = 0f;
    public float peakY = 0f;
    public float maxJumpHeight = 0f;

    public final float width = PhysicsConfig.PLAYER_WIDTH_PX * spriteScale;
    public final float height = PhysicsConfig.PLAYER_HEIGHT_PX * spriteScale;

    public final float spriteOffsetX = 48f * spriteScale;
    public final float spriteOffsetY = 0f * spriteScale;
    public final float spriteFrameSize = 128f * spriteScale;

    public final float moveSpeed = PhysicsConfig.MOVE_SPEED;
    public final float walkSpeed = PhysicsConfig.WALK_SPEED;
    public final float runSpeed = PhysicsConfig.RUN_SPEED;
}