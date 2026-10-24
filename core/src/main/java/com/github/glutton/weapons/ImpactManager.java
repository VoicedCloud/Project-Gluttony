package com.github.glutton.weapons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class ImpactManager {

    public static class Impact {

        public final float x, y;
        public final int damage;
        public float age = 0f;

        public Impact(float x, float y, int damage) {
            this.x = x;
            this.y = y;
            this.damage = damage;
        }
    }

    private final List<Impact> recentImpacts = new ArrayList<>();
    private static final float IMPACT_DISPLAY_TIME = 1.0f; // seconds an impact marker stays visible

    public void addImpact(float x, float y, int damage) {
        recentImpacts.add(new Impact(x, y, damage));
    }

    public void update(float delta) {
        Iterator<Impact> impIt = recentImpacts.iterator();
        while (impIt.hasNext()) {
            Impact imp = impIt.next();
            imp.age += delta;
            if (imp.age >= IMPACT_DISPLAY_TIME) {
                impIt.remove();
            }
        }
    }

    public List<Impact> getRecentImpacts() {
        return Collections.unmodifiableList(recentImpacts);
    }
}
