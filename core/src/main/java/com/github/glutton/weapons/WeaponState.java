package com.github.glutton.weapons;

public class WeaponState {

	public WeaponProfile activeWeapon = WeaponProfile.PISTOL;
	public int currentAmmo = WeaponProfile.PISTOL.maxAmmo;
	public long reloadStartTime = 0;
	public boolean isReloading = false;
	public long lastFireTime = 0;
	public float aimAngleRad = 0f;

	// Virtual joystick / orbital aim offset relative to the gun pivot
	public float aimOffsetX = 50f;
	public float aimOffsetY = 0f;
	public final float aimRadius = 70f;
}