package com.github.glutton.world;

/**
 * Single home for every physics-feel number in the game: world settings,
 * player body/fixture definition, movement speeds, and jump/fall tuning.
 * Change values here instead of hunting through Page1Screen /
 * PlayerController / PlayerState for a specific magic number.
 */
public final class PhysicsConfig {
	private PhysicsConfig() {}

	// ---- World ----
	public static final float GRAVITY_Y = -1000f;

	// Box2D caps how far a body can move in a single step (~2 units), so
	// more, smaller steps raises the effective velocity ceiling without
	// needing to rescale the world.
	public static final int PHYSICS_SUBSTEPS = 8;
	public static final int VELOCITY_ITERATIONS = 6;
	public static final int POSITION_ITERATIONS = 2;

	// ---- Player body / fixture ----
	public static final float PLAYER_SPRITE_SCALE = 0.3f;
	public static final float PLAYER_WIDTH_PX = 32f;
	public static final float PLAYER_HEIGHT_PX = 64f;

	public static final float PLAYER_DENSITY = 1.0f;
	public static final float PLAYER_FRICTION = 0.2f;
	public static final float PLAYER_RESTITUTION = 0.0f;

	public static final float PLAYER_SPAWN_X = 60f;
	public static final float PLAYER_SPAWN_Y = 100f; // spawn high enough to fall onto ground cleanly

	// ---- Player movement ----
	public static final float MOVE_SPEED = 90f;
	public static final float WALK_SPEED = MOVE_SPEED / 2f;
	public static final float RUN_SPEED = MOVE_SPEED;

	// ---- Jump / fall ----
	public static final float JUMP_VELOCITY = 225f;   // tune jump height here
	public static final float MAX_FALL_SPEED = -800f; // terminal velocity (negative = downward)
}