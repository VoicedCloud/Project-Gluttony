package com.github.glutton.screens;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.Input;
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
import com.github.glutton.world.GameplaySession;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;

public class Page1Screen implements Screen {

	private final GluttonGame game;
	private SceneLoader sceneLoader;
	private Viewport viewport;
	private OrthographicCamera camera;

	private SpriteBatch batch;
	private ShapeRenderer shapeRenderer;
	private ParallaxBackground background;

	private boolean debugHitbox = false;
	private GameplaySession gameplaySession;

	public Page1Screen(GluttonGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera();
		camera.zoom = 0.25f; // Set optimal starting zoom level[cite: 13]
		viewport = new FitViewport(1280f, 720f, camera);

		background = new ParallaxBackground();

		SceneConfiguration config = new SceneConfiguration();
		sceneLoader = new SceneLoader(config);
		sceneLoader.loadScene("Page1", viewport);

		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();

		gameplaySession = new GameplaySession(
			camera,
			"sprites/weapons/01 - Individual sprites/Guns/Glock - P80 [64x48].png",
			"sprites/weapons/01 - Individual sprites/Bullets & Ammo/Glock - P80/Bullet.png"
		);
	}

	@Override
	public void render(float delta) {
		viewport.apply();
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		if (Gdx.input.isKeyJustPressed(Input.Keys.F12)) {
			debugHitbox = !debugHitbox;
		}

		// Zoom controls (+ / - keys) with 10% increments and safety bounds
		if (Gdx.input.isKeyJustPressed(Input.Keys.PLUS) || Gdx.input.isKeyJustPressed(Input.Keys.EQUALS) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_ADD)) {
			camera.zoom = Math.max(0.05f, camera.zoom / 1.1f);
		}
		if (Gdx.input.isKeyJustPressed(Input.Keys.MINUS) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_SUBTRACT)) {
			camera.zoom = Math.min(1.5f, camera.zoom * 1.1f);
		}

		gameplaySession.update(delta);

		float viewWidth = camera.viewportWidth * camera.zoom;
		float viewHeight = camera.viewportHeight * camera.zoom;
		float cameraLeftEdge = camera.position.x - viewWidth / 2f;
		float cameraBottomEdge = camera.position.y - viewHeight / 2f;

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		background.draw(batch, camera.position.x, cameraLeftEdge, cameraBottomEdge, viewWidth, viewHeight);
		batch.end();

		sceneLoader.getEngine().setDelta(delta);
		sceneLoader.getEngine().process();

		batch.setProjectionMatrix(camera.combined);
		batch.begin();
		gameplaySession.render(batch, shapeRenderer);
		batch.end();

		// Render bullet motion trails
		gameplaySession.renderTrails(shapeRenderer, camera.combined);

		shapeRenderer.setProjectionMatrix(camera.combined);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
		gameplaySession.renderMuzzleFlash(shapeRenderer);
		shapeRenderer.end();

		if (debugHitbox) {
			shapeRenderer.setProjectionMatrix(camera.combined);
			shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

			shapeRenderer.setColor(1, 1, 0, 1);
			float hw = gameplaySession.getPlayerState().width / 2f;
			float hh = gameplaySession.getPlayerState().height / 2f;
			shapeRenderer.rect(gameplaySession.getPlayerState().x - hw, gameplaySession.getPlayerState().y - hh, gameplaySession.getPlayerState().width, gameplaySession.getPlayerState().height);

			shapeRenderer.setColor(1, 0, 0, 1);
			shapeRenderer.rect(96f, 64f, 32f, 48f);

			shapeRenderer.end();
			Vector2 muzzle = gameplaySession.getGunRenderer().getMuzzleWorldPos(gameplaySession.getPlayerState());
			shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
			shapeRenderer.setColor(1, 0, 1, 1);
			shapeRenderer.circle(muzzle.x, muzzle.y, 2f, 8);
			shapeRenderer.end();
		}

		// Keep camera locked onto player at the standard vertical offset level[cite: 13]
		camera.position.set(gameplaySession.getPlayerState().x, gameplaySession.getPlayerState().y + 32f, 0);
		camera.update();
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
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
		if (gameplaySession != null) {
			gameplaySession.dispose();
		}
	}
}