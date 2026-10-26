package com.github.glutton.weapons;

import com.badlogic.gdx.audio.Sound;

public enum WeaponProfile {
    PISTOL("Glock P80", 12, 30, 500f, 3, 150, 2040, false, 1.0f, 1,
            "sprites/weapons/Glock - P80.png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png",
            "sprites/bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/9mm/WAV/9mm Single.wav",
            64, 48, 0.15f, 50f, 37f, false,
            new ReloadMilestone[]{
                new ReloadMilestone(0.00f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Slide Lock.wav"),
                new ReloadMilestone(0.16f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Reload 1.wav"),
                new ReloadMilestone(0.67f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Reload 2.wav"),
                new ReloadMilestone(0.88f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Slide Release.wav")
            }
    ),
    ASSAULT_RIFLE("AK-47", 30, 15, 5500f, 15, 110, 3000, true, 3.0f, 1,
            "sprites/weapons/01 - Individual sprites/Guns/AK 47 [96x48].png",
            "sprites/weapons/02 - Sprite sheets/AK 47 [96x48]/[SHOOT WITH CASING AND MUZZLE FLASH] AK 47.png",
            "sprites/weapons/02 - Sprite sheets/AK 47 [96x48]/[RELOAD] AK 47.png",
            "sprites/weapons/01 - Individual sprites/Bullets & Ammo/AK 47/Bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/7.62x39/WAV/762x39 Single WAV.wav",
            96, 48, 0.15f, 80f, 38.5f, true,
            new ReloadMilestone[]{
                new ReloadMilestone(.1f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/AK Reload Part 1 WAV.wav"),
                new ReloadMilestone(.725f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/AK Reload Part 2 WAV.wav"),
                new ReloadMilestone(.90f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/AK Rack WAV.wav"),}
    ),
    SHOTGUN("Shotgun", 6, 10, 4000f, 18, 750, 2200, false, 12.0f, 8,
            "sprites/weapons/01 - Individual sprites/Guns/[design]Shotgun_V1.02.png",
            "sprites/weapons/02 - Sprite sheets/Shotgun_V1.00/Weapon/[SHOOTING_CHAMBER_OPEN] Shotgun_V1.02.png",
            "sprites/weapons/02 - Sprite sheets/Shotgun_V1.00/Weapon/[RELOAD] Shotgun_V1.02 - Rreloading_01.png",
            "sprites/bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/20 Gauge/WAV/20 Gauge Single.wav",
            64, 32, 0.15f, 81f, 9.5f, false,
            new ReloadMilestone[]{}
    ),
    GUUN("GUUN", 10, 999, 5000f, 1, 500, 1500, false, 10.0f, 1,
            "sprites/weapons/Glock - P80.png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png",
            "sprites/bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/GUUN/GUUN.wav",
            64, 48, 0.15f, 50f, 37f, false,
            new ReloadMilestone[]{
                new ReloadMilestone(0.0f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/GUUN.wav")}
    );

    public static class ReloadMilestone {

        public final float progress;
        public final String soundPath;
        public Sound sound;
        public boolean hasPlayed = false;

        public ReloadMilestone(float progress, String soundPath) {
            this.progress = progress;
            this.soundPath = soundPath;
        }
    }

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
    public final String shootSoundPath;
    public Sound shootSound;
    public final int frameWidth;
    public final int frameHeight;
    public final float gunScale;
    public final float muzzleOffsetX;
    public final float muzzleOffsetY;
    public final boolean reverseStitchOrder;
    public final ReloadMilestone[] milestones;
    // public final boolean hasKnockback; //set it up later

    WeaponProfile(String name, int maxAmmo, int damage, float bulletSpeed, float spreadDegrees,
            long fireDelayMs, long reloadMs, boolean isAutomatic, float recoilForce, int pellets,
            String idleTexturePath, String shootTexturePath, String reloadTexturePath, String bulletTexturePath,
            String shootSoundPath, int frameWidth, int frameHeight, float gunScale, float muzzleOffsetX, float muzzleOffsetY,
            boolean reverseStitchOrder, ReloadMilestone[] milestones) {
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
        this.shootSoundPath = shootSoundPath;
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;
        this.gunScale = gunScale;
        this.muzzleOffsetX = muzzleOffsetX;
        this.muzzleOffsetY = muzzleOffsetY;
        this.reverseStitchOrder = reverseStitchOrder;
        this.milestones = milestones;
    }
}
