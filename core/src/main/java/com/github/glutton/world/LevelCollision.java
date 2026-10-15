package com.github.glutton.world;

import com.badlogic.gdx.math.Rectangle;

public class LevelCollision {
	public float floorTop;
	public Rectangle wall;

	public LevelCollision(float floorTop, Rectangle wall) {
		this.floorTop = floorTop;
		this.wall = wall;
	}

	public LevelCollision() {
		this.floorTop = 64.0f;
		this.wall = new Rectangle(96f, 64f, 32f, 48f);
	}
}