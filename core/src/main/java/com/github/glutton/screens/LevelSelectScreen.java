package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;

public class LevelSelectScreen implements Screen {

	private static final float VIRTUAL_WIDTH = 1280f;
	private static final float VIRTUAL_HEIGHT = 720f;

	private final GluttonGame game;
	private SceneLoader sceneLoader;
	private Viewport viewport;
	private ItemWrapper rootWrapper;
	private World world; // Keep track of the Box2D World

	// Exit Button Components
	private TransformComponent exitTransform;
	private DimensionsComponent exitDimensions;
	private TintComponent exitHighlightTint;

	// Next Page Button Components
	private TransformComponent nextPageTransform;
	private DimensionsComponent nextPageDimensions;
	private TintComponent nextPageHighlightTint;

	// Play Button Components
	private TransformComponent playTransform;
	private DimensionsComponent playDimensions;

	private final Vector3 mouseVector = new Vector3();

	public LevelSelectScreen(GluttonGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		game.playMenuBgm();

		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		// FIX: Instantiate Box2D World and pass it to HyperLap2D SceneConfiguration
		world = new World(new Vector2(0, -10f), true);
		SceneConfiguration config = new SceneConfiguration();
		config.setWorld(world);

		sceneLoader = new SceneLoader(config);
		sceneLoader.loadScene("LevelSelectScreen", viewport);

		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		rootWrapper = new ItemWrapper(sceneLoader.getRoot(), sceneLoader.getEngine());
		// Setup Exit Button & its Highlight
		ItemWrapper exitBtn = rootWrapper.getChild("ExitButton");
		if (exitBtn != null) {
			int entity = exitBtn.getEntity();
			exitTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
			exitDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
		}

		ItemWrapper exitHighlightBtn = rootWrapper.getChild("HIGHLIGHTEDExitButton");
		if (exitHighlightBtn != null) {
			exitHighlightTint = ComponentRetriever.get(exitHighlightBtn.getEntity(), TintComponent.class, sceneLoader.getEngine());
			if (exitHighlightTint != null) {
				exitHighlightTint.color.a = 0f; // Hide initially
			}
		}

		// Setup Next Page Button & its Highlight
		ItemWrapper nextPageBtn = rootWrapper.getChild("Next_Page");
		if (nextPageBtn != null) {
			int entity = nextPageBtn.getEntity();
			nextPageTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
			nextPageDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
		}

		ItemWrapper nextPageHighlightBtn = rootWrapper.getChild("HIGHLIGHTEDNext_Page");
		if (nextPageHighlightBtn != null) {
			nextPageHighlightTint = ComponentRetriever.get(nextPageHighlightBtn.getEntity(), TintComponent.class, sceneLoader.getEngine());
			if (nextPageHighlightTint != null) {
				nextPageHighlightTint.color.a = 0f; // Hide initially
			}
		}

		// Setup Play Button
		ItemWrapper playBtn = rootWrapper.getChild("Play_Button");
		if (playBtn != null) {
			int entity = playBtn.getEntity();
			playTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
			playDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		viewport.apply();
		sceneLoader.getEngine().process();

		updateHoverAndInput();
	}

	private void updateHoverAndInput() {
		mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		viewport.unproject(mouseVector);

		// 1. Process Exit Button
		if (exitTransform != null && exitDimensions != null) {
			Rectangle bounds = getEntityBounds(exitTransform, exitDimensions);
			boolean isHovered = bounds.contains(mouseVector.x, mouseVector.y);

			if (exitHighlightTint != null) {
				exitHighlightTint.color.a = isHovered ? 1f : 0f;
			}

			if (isHovered && Gdx.input.justTouched()) {
				game.setScreen(new MainMenuScreen(game));
			}
		}

		// 2. Process Next Page Button
		if (nextPageTransform != null && nextPageDimensions != null) {
			Rectangle bounds = getEntityBounds(nextPageTransform, nextPageDimensions);
			boolean isHovered = bounds.contains(mouseVector.x, mouseVector.y);

			if (nextPageHighlightTint != null) {
				nextPageHighlightTint.color.a = isHovered ? 1f : 0f;
			}

			if (isHovered && Gdx.input.justTouched()) {
				// TODO: Add page transition logic here
			}
		}

		// 3. Process Play Button -> Transitions to Page1 Screen
		if (playTransform != null && playDimensions != null) {
			Rectangle bounds = getEntityBounds(playTransform, playDimensions);
			boolean isHovered = bounds.contains(mouseVector.x, mouseVector.y);

			if (isHovered && Gdx.input.justTouched()) {
				game.playLevelBgm("audio/bgm/page1_theme.mp3");

				game.setScreen(new Page1Screen(game));
			}
		}
	}

	private Rectangle getEntityBounds(TransformComponent transform, DimensionsComponent dimensions) {
		float width = dimensions.width * transform.scaleX;
		float height = dimensions.height * transform.scaleY;
		float x = transform.x - (transform.originX * (transform.scaleX - 1));
		float y = transform.y - (transform.originY * (transform.scaleY - 1));
		return new Rectangle(x, y, width, height);
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
		if (sceneLoader != null) {
			sceneLoader = null;
		}
		if (world != null) {
			world.dispose();
			world = null;
		}
	}
}
