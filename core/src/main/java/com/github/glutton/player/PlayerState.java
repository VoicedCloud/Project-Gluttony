package com.github.glutton.player;

import com.github.glutton.weapons.WeaponProfile;

public class PlayerState {

    public float x, y; // position of hitbox center, in world units
    public float velocityY = 0f;
    public boolean facingRight = true;
    public boolean isGrounded = false;
    public boolean canDoubleJump = true;

    public enum MovementState {
        IDLE, WALK, RUN
    }
    public MovementState movementState = MovementState.IDLE;

    // Hitbox size (world units == pixels at 1:1 scale)
    // Hitbox size, scaled to match the old 0.3 sprite scale
    public final float width = 32f * 0.3f;   // 9.6f;
    public final float height = 64f * 0.3f;  // 19.2f;

    // Offset from hitbox's bottom-left corner to the sprite frame's bottom-left corner,
    // i.e. how far to shift when drawing the full 128x128 frame around this hitbox.
    // Sprite offset/frame size, scaled the same way
    public final float spriteOffsetX = 48f * 0.3f;   // 14.4f;
    public final float spriteOffsetY = 0f * 0.3f;    // 0f;
    public final float spriteFrameSize = 128f * 0.3f; // 38.4f;

    public final float moveSpeed = 90.0f;
    public final float walkSpeed = moveSpeed / 2f;
    public final float runSpeed = moveSpeed;

    public final float jumpStrength = 315.0f;
    public final float gravity = -1200.0f;

    public WeaponProfile activeWeapon = WeaponProfile.PISTOL;
    public int currentAmmo = WeaponProfile.PISTOL.maxAmmo;
    public boolean isReloading = false;
    public long reloadStartTime = 0;
    public long lastFireTime = 0;
    public float aimAngleRad = 0f;
}
