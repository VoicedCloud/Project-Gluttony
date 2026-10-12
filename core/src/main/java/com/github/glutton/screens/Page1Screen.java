package com.github.glutton.screens;

import com.artemis.World;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;
import com.github.glutton.components.PlayerComponent;
import com.github.glutton.systems.PlayerSystem;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.utils.ItemWrapper;

public class Page1Screen implements Screen {

	private final GluttonGame game;
	private SceneLoader sceneLoader;
	private Viewport viewport;
	private OrthographicCamera camera;
	private com.badlogic.gdx.physics.box2d.World physicsWorld;

	private Box2DDebugRenderer debugRenderer;
	private boolean debugMode = true;

	public Page1Screen(GluttonGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		camera = new OrthographicCamera();
		camera.zoom = 0.35f;
		viewport = new FitViewport(1280f, 720f, camera);

		physicsWorld = new com.badlogic.gdx.physics.box2d.World(new Vector2(0, -10f), true);

		// 1. Add PlayerSystem BEFORE creating SceneLoader
		SceneConfiguration config = new SceneConfiguration();
		config.setWorld(physicsWorld);
		config.addSystem(new PlayerSystem(camera));

		sceneLoader = new SceneLoader(config);
		sceneLoader.loadScene("Page1", viewport);

		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

		// 2. Fetch Player entity ID and add PlayerComponent
		ItemWrapper rootWrapper = new ItemWrapper(sceneLoader.getRoot(), sceneLoader.getEngine());
		ItemWrapper playerItem = rootWrapper.getChild("Player");

		if (playerItem != null) {
			int entityId = playerItem.getEntity();
			World artemisWorld = sceneLoader.getEngine();
			artemisWorld.edit(entityId).add(new PlayerComponent());
		}

		debugRenderer = new Box2DDebugRenderer();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		viewport.apply();

		// Step the Box2D simulation forward
		physicsWorld.step(1f / 60f, 6, 2);

		// Update Artemis-ODB engine (this is what syncs body -> transform, runs PlayerSystem, etc.)
		sceneLoader.getEngine().setDelta(delta);
		sceneLoader.getEngine().process();

		// Render Box2D debug lines using sceneLoader.getWorld()
		if (debugMode && sceneLoader.getWorld() != null) {
			debugRenderer.render(sceneLoader.getWorld(), camera.combined);
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
		if (debugRenderer != null) {
			debugRenderer.dispose();
		}
		if (physicsWorld != null) {
			physicsWorld.dispose();
		}
	}
}
