/*
 * =====================================================================================
 * CLASS HEADER: Pistol.java
 * =====================================================================================
 * WHAT THIS FILE DOES:
 * - Manages the player's primary sidearm state, clip capacity, and firing cooldowns.
 * - Spawns active Bullet instances into the global bullet array based on directional angles.
 * - Handles non-blocking asynchronous reloading using system timestamps.
 *
 * HOW IT CONNECTS TO OTHER SYSTEMS:
 * - Instantiated and updated inside 'PlayerPanel.java'.
 * - Invoked by keyboard (Key 'F') and mouse click event dispatchers.
 * - Appends newly created 'Bullet' objects directly to the active projectile list.
 * =====================================================================================
 */

package com.github.glutton.weapons.pistol;

import java.util.List;

public class Pistol {

    // =====================================================================================
    // SYSTEM VARIABLE DECLARATIONS
    // =====================================================================================
    private int maxAmmo = 12;            // Maximum magazine capacity
    private int currentAmmo = 12;              // Remaining rounds in the active magazine
    private boolean isReloading = false;        // Reload state flag
    private long reloadStartTime = 0;           // System timestamp when reload initiated
    private long reloadDuration = 1500;   // Reload completion threshold (1.5 seconds)

    // =====================================================================================
    // SYSTEM: PROJECTILE FIRING & INSTANTIATION ENGINE
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Checks if rounds are available and the weapon is not reloading. If valid, spawns a new
     * Bullet entity into the game's active tracking list and decrements ammo. Triggers auto-reload on empty.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: The shoot method validates ammunition counts, adds a new Bullet to the active bullet list, 
     * decrements ammo, and automatically calls reload if the magazine hits zero."
     */
    public void shoot(double originX, double originY, double angle, List<Bullet> activeBullets) {
        if (currentAmmo > 0 && !isReloading) {
            // Add 22.0 at the end to match the new Bullet layout
         activeBullets.add(new Bullet(originX, originY, angle, 22.0,25));

            currentAmmo--;
            if (currentAmmo == 0) {
                reload();
            }
        }
    }

    // =====================================================================================
    // SYSTEM: RELOAD STATE CONTROLLER
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Initiates the reload sequence if the clip is below max capacity and not already reloading.
     * Captures current system milliseconds to drive time-based state recovery.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: Reload sets the reloading flag to true and stores the current system time to track progress."
     */
    public void reload() {
        if (currentAmmo < maxAmmo && !isReloading) {
            isReloading = true;
            reloadStartTime = System.currentTimeMillis();
        }
    }

    // =====================================================================================
    // SYSTEM: TIMED TICK & RECOVERY ENGINE
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Runs inside the main game loop tick (PlayerPanel update cycle). Calculates elapsed time since reload start.
     * Restores clip capacity once the 1.5-second threshold is reached.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: Update runs on every frame tick to check if 1.5 seconds have elapsed since reloading started. 
     * Once reached, it restores full ammo and clears the reload state."
     */
    public void update() {
        if (isReloading) {
            if (System.currentTimeMillis() - reloadStartTime >= reloadDuration) {
                currentAmmo = maxAmmo;
                isReloading = false;
            }
        }
    }

    // =====================================================================================
    // SYSTEM: STATE ACCESSORS
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Provides read-only accessors for ammo levels and reloading states, used by PlayerPanel to draw HUD elements.
     */
    public int getCurrentAmmo() { return currentAmmo; }
    public int getMaxAmmo() { return maxAmmo; }
    public boolean isReloading() { return isReloading; }
}