package com.github.glutton.weapons;

public class WeaponState {

	public WeaponProfile activeWeapon = WeaponProfile.PISTOL;
	public int currentAmmo = WeaponProfile.PISTOL.maxAmmo;
	public long reloadStartTime = 0;
	public boolean isReloading = false;
	public long lastFireTime = 0;
	public float aimAngleRad = 0f;

	public float preFireX = 0f, preFireY = 0f;
	public long preFireTimestamp = 0;
	public float recoilVecX = 0f, recoilVecY = 0f;
	public long recoilTimestamp = 0;

	public boolean playedSlideLock = false;
	public boolean playedReload1 = false;
	public boolean playedReload2 = false;
	public boolean playedSlideRelease = false;
}