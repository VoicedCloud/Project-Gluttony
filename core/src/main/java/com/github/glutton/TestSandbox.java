package com.github.glutton;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;
import com.github.glutton.entities.TargetDummy;

public class TestSandbox extends ApplicationAdapter {

	private OrthographicCamera camera;
	private ShapeRenderer shapeRenderer;
	
	private Rectangle player;
	private TargetDummy targetDummy;
	private Rectangle floor;

	private float velocityY = 0;
	private final float gravity = -1000f;
	private final float jumpVelocity = 450f;
	private final float moveSpeed = 250f;
	private boolean isGrounded = false;

	@Override
	public void create() {
		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 600);
		shapeRenderer = new ShapeRenderer();

		player = new Rectangle(100, 150, 24, 48);
		floor = new Rectangle(50, 50, 700, 20);

		// Spawn dummy directly on top of the floor (floor.y + floor.height)
		targetDummy = new TargetDummy(player.x + 150f, floor.y + floor.height);
	}

	@Override
	public void render() {
		float delta = Gdx.graphics.getDeltaTime();

		float nextX = player.x;
		if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
			nextX -= moveSpeed * delta;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
			nextX += moveSpeed * delta;
		}

		if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W)) && isGrounded) {
			velocityY = jumpVelocity;
			isGrounded = false;
		}

		velocityY += gravity * delta;
		float nextY = player.y + velocityY * delta;

		player.setPosition(nextX, nextY);

		if (player.overlaps(floor)) {
			player.setPosition(nextX, floor.y + floor.height);
			velocityY = 0;
			isGrounded = true;
		}

		// --- DUMMY HIT TEST (Press 'F' to attack) ---
		if (Gdx.input.isKeyJustPressed(Input.Keys.F) && !targetDummy.isDestroyed()) {
			if (player.overlaps(targetDummy.bounds)) {
				targetDummy.takeDamage(10);
			}
		}

		ScreenUtils.clear(0.12f, 0.12f, 0.15f, 1f);

		camera.update();
		shapeRenderer.setProjectionMatrix(camera.combined);

		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

		// Draw Floor
		shapeRenderer.setColor(Color.DARK_GRAY);
		shapeRenderer.rect(floor.x, floor.y, floor.width, floor.height);

		// Draw Player (Cyan Box)
		shapeRenderer.setColor(Color.CYAN);
		shapeRenderer.rect(player.x, player.y, player.width, player.height);

		// Draw Target Dummy and its health bar using its built-in render method
		if (!targetDummy.isDestroyed()) {
			targetDummy.render(shapeRenderer);
		}

		shapeRenderer.end();
	}

	@Override
	public void dispose() {
		shapeRenderer.dispose();
	}
}