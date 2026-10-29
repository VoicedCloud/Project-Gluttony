
package com.github.glutton.weapons;

// Provides sound effects for shooting and reloading
import com.badlogic.gdx.audio.Sound;

// Defines the available weapons and their properties
public enum WeaponProfile {

    // PISTOL weapon configuration
    PISTOL("Glock P80", 12, 30, 500f, 3, 150, 2040, false, 1.0f, 1,
            // Paths to the pistol's idle, shooting, reloading, bullet, and shooting sound assets
            "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png",
            "sprites/bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/9mm/WAV/9mm Single.wav",

            // Sprite frame width and height, gun scale, and muzzle position offsets
            64, 48, 0.15f, 50f, 37f, false,

            // Sound effects played at specific points during the reload animation
            new ReloadMilestone[]{
                new ReloadMilestone(0.00f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Slide Lock.wav"),
                new ReloadMilestone(0.16f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Reload 1.wav"),
                new ReloadMilestone(0.67f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Reload 2.wav"),
                new ReloadMilestone(0.88f, "audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Slide Release.wav")
            }
    ),

    // ASSAULT RIFLE weapon configuration
    ASSAULT_RIFLE("AK-47", 30, 15, 5500f, 15, 110, 3000, true, 3.0f, 1,
            // Paths to the AK-47's visual and audio assets
            "sprites/weapons/01 - Individual sprites/Guns/AK 47 [96x48].png",
            "sprites/weapons/02 - Sprite sheets/AK 47 [96x48]/[SHOOT WITH CASING AND MUZZLE FLASH] AK 47.png",
            "sprites/weapons/02 - Sprite sheets/AK 47 [96x48]/[RELOAD] AK 47.png",
            "sprites/weapons/01 - Individual sprites/Bullets & Ammo/AK 47/Bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/7.62x39/WAV/762x39 Single WAV.wav",

            // Sprite dimensions, display scale, muzzle offsets, and sprite stitching order
            96, 48, 0.15f, 80f, 38.5f, true,

            // Reload sound effects triggered at specific reload progress values
            new ReloadMilestone[]{
                new ReloadMilestone(.1f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/AK Reload Part 1 WAV.wav"),
                new ReloadMilestone(.725f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/AK Reload Part 2 WAV.wav"),
                new ReloadMilestone(.90f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/AK Rack WAV.wav"),}
    ),

    // SHOTGUN weapon configuration
    SHOTGUN("Shotgun", 6, 10, 4000f, 18, 750, 2200, false, 12.0f, 8,
            // Paths to the shotgun's visual and audio assets
            "sprites/weapons/01 - Individual sprites/Guns/[design]Shotgun_V1.02.png",
            "sprites/weapons/02 - Sprite sheets/Shotgun_V1.00/Weapon/[SHOOTING_CHAMBER_OPEN] Shotgun_V1.02.png",
            "sprites/weapons/02 - Sprite sheets/Shotgun_V1.00/Weapon/[RELOAD] Shotgun_V1.02 - Rreloading_01.png",
            "sprites/bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/20 Gauge/WAV/20 Gauge Single.wav",

            // Sprite dimensions, display scale, and muzzle position offsets
            64, 32, 0.15f, 81f, 9.5f, false,

            // The shotgun currently has no reload milestone sounds configured
            new ReloadMilestone[]{}
    ),

    // GUUN weapon configuration
    GUUN("GUUN", 10, 999, 5000f, 1, 500, 1500, false, 10.0f, 1,
            // Uses the pistol's sprites but has its own shooting sound
            "sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png",
            "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png",
            "sprites/bullet.png",
            "audio/weapon/Snake's Authentic Gun Sounds/Full Sound/GUUN/GUUN.wav",

            // Sprite dimensions, display scale, and muzzle offsets
            64, 48, 0.15f, 50f, 37f, false,

            // Sound effect played when the reload reaches the configured progress point
            new ReloadMilestone[]{
                new ReloadMilestone(0.0f, "audio/weapon/Snake's Authentic Gun Sounds/Reloads, Cycling & More/WAV/GUUN.wav")}
    );

    // Represents a sound effect that should play at a particular reload progress
    public static class ReloadMilestone {

        // Reload progress at which the sound should play, usually between 0.0 and 1.0
        public final float progress;

        // File path of the reload sound effect
        public final String soundPath;

        // Loaded sound object; assigned when the sound asset is loaded
        public Sound sound;

        // Tracks whether this milestone's sound has already played during the reload
        public boolean hasPlayed = false;

        // Creates a reload milestone with its progress point and sound file
        public ReloadMilestone(float progress, String soundPath) {
            this.progress = progress;
            this.soundPath = soundPath;
        }
    }

    // Weapon's display name
    public final String displayName;

    // Maximum ammunition capacity for one magazine
    public final int maxAmmo;

    // Damage value assigned to each bullet or projectile
    public final int damage;

    // Speed at which bullets travel through the game world
    public final float bulletSpeed;

    // Spread angle in degrees; larger values generally mean less accurate shots
    public final float spreadDegrees;

    // Minimum delay between shots, measured in milliseconds
    public final long fireDelayMs;

    // Time required to complete a reload, measured in milliseconds
    public final long reloadMs;

    // True if the weapon supports automatic firing
    public final boolean isAutomatic;

    // Strength of the recoil applied when firing
    public final float recoilForce;

    // Number of projectiles fired in a single shot
    public final int pellets;

    // File path for the weapon's idle sprite
    public final String idleTexturePath;

    // File path for the weapon's shooting animation sprite sheet
    public final String shootTexturePath;

    // File path for the weapon's reload animation sprite sheet
    public final String reloadTexturePath;

    // File path for the bullet texture
    public final String bulletTexturePath;

    // File path for the weapon's shooting sound
    public final String shootSoundPath;

    // Loaded shooting sound object; assigned when the sound is loaded
    public Sound shootSound;

    // Width and height of an individual animation frame
    public final int frameWidth;
    public final int frameHeight;

    // Scale used when displaying the weapon sprite
    public final float gunScale;

    // Horizontal and vertical offsets used to position the muzzle
    public final float muzzleOffsetX;
    public final float muzzleOffsetY;

    // Determines whether the animation frames should be stitched in reverse order
    public final boolean reverseStitchOrder;

    // Collection of sound milestones used during the reload animation
    public final ReloadMilestone[] milestones;

    // Future feature: knockback configuration
    // public final boolean hasKnockback; //set it up later

    // Constructor used to configure each weapon in the enum
    WeaponProfile(
            String name,
            int maxAmmo,
            int damage,
            float bulletSpeed,
            float spreadDegrees,
            long fireDelayMs,
            long reloadMs,
            boolean isAutomatic,
            float recoilForce,
            int pellets,
            String idleTexturePath,
            String shootTexturePath,
            String reloadTexturePath,
            String bulletTexturePath,
            String shootSoundPath,
            int frameWidth,
            int frameHeight,
            float gunScale,
            float muzzleOffsetX,
            float muzzleOffsetY,
            boolean reverseStitchOrder,
            ReloadMilestone[] milestones) {

        // Store the weapon's basic combat properties
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

        // Store the paths to the weapon's visual and audio assets
        this.idleTexturePath = idleTexturePath;
        this.shootTexturePath = shootTexturePath;
        this.reloadTexturePath = reloadTexturePath;
        this.bulletTexturePath = bulletTexturePath;
        this.shootSoundPath = shootSoundPath;

        // Store the animation frame dimensions
        this.frameWidth = frameWidth;
        this.frameHeight = frameHeight;

        // Store the weapon's display scale and muzzle offsets
        this.gunScale = gunScale;
        this.muzzleOffsetX = muzzleOffsetX;
        this.muzzleOffsetY = muzzleOffsetY;

        // Store the animation stitching configuration
        this.reverseStitchOrder = reverseStitchOrder;

        // Store the reload sound milestones
        this.milestones = milestones;
    }
}