package com.github.glutton.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Bullet {
	public float x, y;
	public float vx, vy;
	public int damage;
	public float lifeRemaining;

	public final float spawnX, spawnY;

	// Store raw positions; we will calculate smooth alpha on the fly during rendering
	public final Array<Vector2> trail = new Array<>();
	private static final int MAX_TRAIL_POINTS = 5; // Increase for a longer trail, decrease for shorter

	public Bullet(float x, float y, float vx, float vy, int damage, float lifeRemaining) {
		this.x = x;
		this.y = y;
		this.spawnX = x;
		this.spawnY = y;
		this.vx = vx;
		this.vy = vy;
		this.damage = damage;
		this.lifeRemaining = lifeRemaining;

		trail.add(new Vector2(x, y));
	}

	public void updateTrail(float delta) {
		// Add current position to the front of the trail every frame
		trail.insert(0, new Vector2(x, y));

		// Keep the trail length fixed so it doesn't grow infinitely
		if (trail.size > MAX_TRAIL_POINTS) {
			trail.truncate(MAX_TRAIL_POINTS);
		}
	}
}