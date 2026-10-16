package com.github.glutton.weapons;

public enum WeaponProfile {
	// Profile / name / max ammo / damage / bulletspeed / spreadDegrees / fire delay / reloadMs / isAutomatic / recoilForce / pellets / idlePath / shootPath / reloadPath / bulletPath / width / height
	PISTOL("Glock P80", 12, 10, 500f, 3, 150, 2040, false, 2.5f, 1,
		"sprites/weapons/Glock - P80.png",
		"sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png",
		"sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png",
		"sprites/bullet.png", 64, 48),

	ASSAULT_RIFLE("AK-47", 30, 8, 5500f, 15, 110, 1800, true, 4.0f, 1,
		"sprites/weapons/AK 47.png",
		"sprites/weapons/02 - Sprite sheets/AK 47 [96x48]/[SHOOT WITH CASING AND MUZZLE FLASH] AK 47.png",
		"sprites/weapons/02 - Sprite sheets/AK 47 [96x48]/[RELOAD] AK 47.png",
		"sprites/bullet_ak.png", 96, 48),

	SHOTGUN("Shotgun", 6, 8, 4000f, 18, 750, 2200, false, 12.0f, 8,
		"sprites/weapons/Shotgun.png",
		"sprites/weapons/02 - Sprite sheets/Shotgun [64x32]/[SHOOT] Shotgun.png",
		"sprites/weapons/02 - Sprite sheets/Shotgun [64x32]/[RELOAD] Shotgun.png",
		"sprites/bullet.png", 64, 32),

	GUUN("GUUN", 999, 100, 35f, 10, 10, 1500, true, 2.0f, 10,
		"sprites/weapons/Glock - P80.png",
		"sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png",
		"sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png",
		"sprites/bullet.png", 64, 48);

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
	public final String idleTexturePath;
	public final String shootTexturePath;
	public final String reloadTexturePath;
	public final String bulletTexturePath;
	public final int frameWidth;
	public final int frameHeight;

	WeaponProfile(String name, int maxAmmo, int damage, float bulletSpeed, float spreadDegrees,
			long fireDelayMs, long reloadMs, boolean isAutomatic, float recoilForce, int pellets,
			String idleTexturePath, String shootTexturePath, String reloadTexturePath, String bulletTexturePath,
			int frameWidth, int frameHeight) {
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
		this.idleTexturePath = idleTexturePath;
		this.shootTexturePath = shootTexturePath;
		this.reloadTexturePath = reloadTexturePath;
		this.bulletTexturePath = bulletTexturePath;
		this.frameWidth = frameWidth;
		this.frameHeight = frameHeight;
	}
}