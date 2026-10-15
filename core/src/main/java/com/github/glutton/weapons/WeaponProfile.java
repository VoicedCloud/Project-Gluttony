package com.github.glutton.weapons;

public enum WeaponProfile {
	// Profile / name / max ammo / damage / bulletspeed / spreadDegrees / fire delay / reloadMs / isAutomatic / recoilForce / pellets
	PISTOL("Pistol", 12, 10, 500f, 3, 150, 1200, false, 2.5f, 1),
	// PISTOL("KAMHAMEHA", 120, 10, 250f, 15, 50, 1200, true , 2.5f, 100), // not for official game, just for fun
	ASSAULT_RIFLE("Assault Rifle", 30, 8, 5500f, 15, 110, 1800, true, 4.0f, 1),
	SHOTGUN("Shotgun", 6, 8, 4000f, 18, 750, 2200, false, 12.0f, 8);
	//PISTOL("Shotgun", 6, 8, 1000f, 5, 750, 2200, false, 12.0f, 8);

	public final String displayName;
	public final int maxAmmo;
	public final int damage;
	public final float bulletSpeed;
	public final float spreadDegrees;
	public final long fireDelayMs;
	public final long reloadMs;
	public final boolean isAutomatic;
	public final float recoilForce;
	public final int pellets;

	WeaponProfile(String name, int maxAmmo, int damage, float bulletSpeed, float spreadDegrees,
		long fireDelayMs, long reloadMs, boolean isAutomatic, float recoilForce, int pellets) {
		this.displayName = name;
		this.maxAmmo = maxAmmo;
		this.damage = damage;
		this.bulletSpeed = bulletSpeed;
		this.spreadDegrees = spreadDegrees;
		this.fireDelayMs = fireDelayMs;
		this.reloadMs = reloadMs;
		this.isAutomatic = isAutomatic;
		this.recoilForce = recoilForce;
		this.pellets = pellets;
	}
}
