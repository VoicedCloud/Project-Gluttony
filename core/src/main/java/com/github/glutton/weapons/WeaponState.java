package com.github.glutton.weapons;

import java.util.EnumSet;
import java.util.EnumMap;
import java.util.Map;

public class WeaponState {

    public WeaponProfile activeWeapon = WeaponProfile.PISTOL;

    public final EnumSet<WeaponProfile> ownedWeapons =
        EnumSet.of(WeaponProfile.PISTOL);

    public int currentAmmo = WeaponProfile.PISTOL.maxAmmo;
    public long reloadStartTime = 0;
    public boolean isReloading = false;
    public long lastFireTime = 0;
    public float aimAngleRad = 0f;

    // Aim offset.
    public float aimOffsetX = 50f;
    public float aimOffsetY = 0f;
    public final float aimRadius = 70f;

    // ==================================================
    // WEAPON UPGRADES
    // ==================================================

    public static final int MAX_UPGRADE_LEVEL = 10;

    private final Map<WeaponProfile, Integer> damageLevels =
        new EnumMap<>(WeaponProfile.class);

    private final Map<WeaponProfile, Integer> magazineLevels =
        new EnumMap<>(WeaponProfile.class);

    private final Map<WeaponProfile, Integer> fireRateLevels =
        new EnumMap<>(WeaponProfile.class);

    public WeaponState() {
        for (WeaponProfile weapon : WeaponProfile.values()) {
            damageLevels.put(weapon, 0);
            magazineLevels.put(weapon, 0);
            fireRateLevels.put(weapon, 0);
        }
    }

    public int getDamageLevel(WeaponProfile weapon) {
        return damageLevels.get(weapon);
    }

    public int getMagazineLevel(WeaponProfile weapon) {
        return magazineLevels.get(weapon);
    }

    public int getFireRateLevel(WeaponProfile weapon) {
        return fireRateLevels.get(weapon);
    }

    public boolean upgradeDamage(WeaponProfile weapon) {
        int level = getDamageLevel(weapon);

        if (level >= MAX_UPGRADE_LEVEL) {
            return false;
        }

        damageLevels.put(weapon, level + 1);
        return true;
    }

    public boolean upgradeMagazine(WeaponProfile weapon) {
        int level = getMagazineLevel(weapon);

        if (level >= MAX_UPGRADE_LEVEL) {
            return false;
        }

        magazineLevels.put(weapon, level + 1);

        if (activeWeapon == weapon) {
            currentAmmo = Math.min(
                getEffectiveMaxAmmo(weapon),
                currentAmmo + 2
            );
        }

        return true;
    }

    public boolean upgradeFireRate(WeaponProfile weapon) {
        int level = getFireRateLevel(weapon);

        if (level >= MAX_UPGRADE_LEVEL) {
            return false;
        }

        fireRateLevels.put(weapon, level + 1);
        return true;
    }

    // ==================================================
    // EFFECTIVE WEAPON STATS
    // ==================================================

    public int getEffectiveDamage(WeaponProfile weapon) {
        return weapon.damage + getDamageLevel(weapon) * 5;
    }

    public int getEffectiveMaxAmmo(WeaponProfile weapon) {
        return weapon.maxAmmo + getMagazineLevel(weapon) * 2;
    }

    public long getEffectiveFireDelay(WeaponProfile weapon) {
        int level = getFireRateLevel(weapon);

        // Reduce firing delay by 5% per level.
        double multiplier = 1.0 - (level * 0.05);

        // Never allow firing delay below 50% of its original value.
        multiplier = Math.max(0.5, multiplier);

        return Math.max(
            1L,
            (long) (weapon.fireDelayMs * multiplier)
        );
    }
}