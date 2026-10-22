package com.github.glutton.world;

public final class Box2DVars {
	private Box2DVars() {}

	/**
	 * Pixels per Box2D meter. Box2D's internal solver caps how far a body
	 * can move in a single step (b2_maxTranslation = 2 world units/step,
	 * ~120 units/sec at 60Hz) to prevent tunneling -- that cap is what was
	 * silently crushing jump velocity and gravity when this project used
	 * 1 pixel = 1 Box2D unit.
	 *
	 * Everything physics-facing (body size, spawn position, gravity,
	 * velocities handed to the Box2D body) is divided by this constant
	 * before reaching Box2D, and multiplied back by it when read out again
	 * for rendering/gameplay, which stays in pixel space everywhere else.
	 */
	public static final float PPM = 32f;
}