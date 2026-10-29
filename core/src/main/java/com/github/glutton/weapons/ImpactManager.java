
package com.github.glutton.weapons;

// Java collection classes for storing and managing impact records
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ImpactManager {

    // Represents a bullet impact at a specific position in the game world
    public static class Impact {

        // World coordinates where the bullet impact occurred
        public final float x, y;

        // Damage associated with the impact
        public final int damage;

        // Time elapsed since the impact was created, measured in seconds
        public float age = 0f;

        // Creates an impact record with its position and damage value
        public Impact(float x, float y, int damage) {
            this.x = x;
            this.y = y;
            this.damage = damage;
        }
    }

    // Stores impacts that are still within their display duration
    private final List<Impact> recentImpacts = new ArrayList<>();

    // Number of seconds an impact marker remains available
    private static final float IMPACT_DISPLAY_TIME = 1.0f;

    // Creates and stores a new impact at the specified position
    public void addImpact(float x, float y, int damage) {
        recentImpacts.add(new Impact(x, y, damage));
    }

    // Updates the age of every impact and removes expired ones
    public void update(float delta) {

        // Use an iterator so impacts can be safely removed while looping
        Iterator<Impact> impIt = recentImpacts.iterator();

        while (impIt.hasNext()) {

            // Get the next impact in the list
            Impact imp = impIt.next();

            // Increase its age by the time elapsed since the previous frame
            imp.age += delta;

            // Remove the impact once it reaches the display time limit
            if (imp.age >= IMPACT_DISPLAY_TIME) {
                impIt.remove();
            }
        }
    }

    // Returns a read-only view of the current impact records.
    // Other classes can read the list but cannot modify it through this view.
    public List<Impact> getRecentImpacts() {
        return Collections.unmodifiableList(recentImpacts);
    }
}
