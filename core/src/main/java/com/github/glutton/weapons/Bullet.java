package com.github.glutton.weapons;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Bullet {
	public float x, y;
	public float vx, vy;
	public int damage;
	public float lifeRemaining;
	public WeaponProfile weapon;

	public final float spawnX, spawnY;

	public final Array<Vector2> trail = new Array<>();
	private static final int MAX_TRAIL_POINTS = 5;

	public Bullet(float x, float y, float vx, float vy, int damage, float lifeRemaining, WeaponProfile weapon) {
		this.x = x;
		this.y = y;
		this.spawnX = x;
		this.spawnY = y;
		this.vx = vx;
		this.vy = vy;
		this.damage = damage;
		this.lifeRemaining = lifeRemaining;
		this.weapon = weapon;

		trail.add(new Vector2(x, y));
	}

	public void updateTrail(float delta) {
		trail.insert(0, new Vector2(x, y));
		if (trail.size > MAX_TRAIL_POINTS) {
			trail.truncate(MAX_TRAIL_POINTS);
		}
	}
}