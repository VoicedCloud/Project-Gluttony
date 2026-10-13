package com.github.glutton.screens;

import com.github.glutton.background.ParallaxBackground;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;
import com.github.glutton.player.PlayerAnimator;
import com.github.glutton.player.PlayerController;
import com.github.glutton.player.PlayerState;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;

public class Page1Screen implements Screen {

	private final GluttonGame game;
	private SceneLoader sceneLoader;
	private Viewport viewport;
	private OrthographicCamera camera;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;

	private PlayerState playerState;
	private PlayerController playerController;
	private PlayerAnimator playerAnimator;

	private ParallaxBackground background;

	private boolean debugHitbox = true;

	public Page1Screen(GluttonGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera();
		camera.zoom = 0.25f;
		viewport = new FitViewport(1280f, 720f, camera);

		background = new ParallaxBackground();

		// Level art only — no player systems/components attached here anymore
		SceneConfiguration config = new SceneConfiguration();
		sceneLoader = new SceneLoader(config);
		sceneLoader.loadScene("Page1", viewport);

		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		playerState = new PlayerState();
		playerState.x = 0f;
		playerState.y = 300f; // spawn above the floor, let it fall and land
		playerController = new PlayerController(camera);
		playerAnimator = new PlayerAnimator();
	}

	@Override
	public void render(float delta) {
		// 1. Clear screen and apply viewport first
		viewport.apply();
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// 2. Update player logic and animator state time
		playerController.update(playerState, delta);
		playerAnimator.update(delta);

		// 3. Calculate camera view boundaries for parallax (including vertical bounds)
		float viewWidth = camera.viewportWidth * camera.zoom;
		float viewHeight = camera.viewportHeight * camera.zoom;
		float cameraLeftEdge = camera.position.x - viewWidth / 2f;
		float cameraBottomEdge = camera.position.y - viewHeight / 2f;

		// 4. Draw Parallax Background anchored to the camera view box
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		background.draw(batch, camera.position.x, cameraLeftEdge, cameraBottomEdge, viewWidth, viewHeight);
		batch.end();

		// 5. Draw HyperLap2D static level art
		sceneLoader.getEngine().setDelta(delta);
		sceneLoader.getEngine().process();

		// 6. Draw Player Sprite
		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		playerAnimator.draw(batch, playerState);
		batch.end();

		// 7. Draw Debug Hitboxes
		if (debugHitbox) {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

			shapeRenderer.setColor(1, 1, 0, 1); // yellow = player hitbox
			float hw = playerState.width / 2f;
			float hh = playerState.height / 2f;
			shapeRenderer.rect(playerState.x - hw, playerState.y - hh, playerState.width, playerState.height);

			shapeRenderer.setColor(1, 0, 0, 1); // red = wall collision box
			shapeRenderer.rect(96f, 64f, 32f, 48f);

			shapeRenderer.end();
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void hide() {
	}

	@Override
	public void dispose() {
		if (batch != null) {
			batch.dispose();
		}
		if (shapeRenderer != null) {
			shapeRenderer.dispose();
		}
		if (playerAnimator != null) {
			playerAnimator.dispose();
		}
	}
}
