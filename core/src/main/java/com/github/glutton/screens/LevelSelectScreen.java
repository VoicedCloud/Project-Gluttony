package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
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

	// Exit Button
	private TransformComponent exitTransform;
	private DimensionsComponent exitDimensions;
	private TintComponent exitTint;

	// Book & Page Layout
	private TransformComponent bookGuiTransform;
	private TransformComponent rightPageTransform;

	// Play Button
	private TransformComponent playTransform;
	private DimensionsComponent playDimensions;
	private TintComponent playTint;

	// Next Page Button (hit-tested on the NextPage composite itself, not its inner icon)
	private TransformComponent nextPageTransform;
	private DimensionsComponent nextPageDimensions;
	private TintComponent nextPageTint;

	private final Vector3 mouseVector = new Vector3();

	private boolean isValid(ItemWrapper w) {
		return w != null && w.getEntity() >= 0;
	}

	public LevelSelectScreen(GluttonGame game) {
		this.game = game;

		viewport = new StretchViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT);

		SceneConfiguration config = new SceneConfiguration();
		sceneLoader = new SceneLoader(config);
		sceneLoader.loadScene("LevelSelectScreen", viewport);

		viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		rootWrapper = new ItemWrapper(sceneLoader.getRoot(), sceneLoader.getEngine());

		// 1. Exit Button
		ItemWrapper exitBtn = rootWrapper.getChild("ExitButton");
		if (isValid(exitBtn)) {
			int entity = exitBtn.getEntity();
			exitTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
			exitDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());

			ItemWrapper exitPressed = exitBtn.getChild("Pressed");
			if (isValid(exitPressed)) {
				exitTint = ComponentRetriever.get(exitPressed.getEntity(), TintComponent.class, sceneLoader.getEngine());
			}
			if (exitTint == null) {
				exitTint = ComponentRetriever.get(entity, TintComponent.class, sceneLoader.getEngine());
			}
			if (exitTint == null) {
				exitTint = sceneLoader.getEngine().edit(entity).create(TintComponent.class);
				exitTint.color.set(0.7f, 0.7f, 0.7f, 1f);
			}
		}

		// 2. Nested Book GUI Elements
		ItemWrapper bookGui = rootWrapper.getChild("BOOKGUI");
		if (isValid(bookGui)) {
			bookGuiTransform = ComponentRetriever.get(bookGui.getEntity(), TransformComponent.class, sceneLoader.getEngine());

			ItemWrapper rightPage = bookGui.getChild("RightPage");
			if (isValid(rightPage)) {
				rightPageTransform = ComponentRetriever.get(rightPage.getEntity(), TransformComponent.class, sceneLoader.getEngine());

				// Play Button
				ItemWrapper playBtn = rightPage.getChild("Play_Button");
				if (isValid(playBtn)) {
					int entity = playBtn.getEntity();
					playTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
					playDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
					playTint = ComponentRetriever.get(entity, TintComponent.class, sceneLoader.getEngine());
					if (playTint == null) {
						playTint = sceneLoader.getEngine().edit(entity).create(TintComponent.class);
						playTint.color.set(0.58f, 0.58f, 0.58f, 1f);
					}
				}

				// Next Page Button — bounds come from the "NextPage" composite (the actual
				// button entity, 48x48), tint comes from its inner "Next_Page" icon.
				ItemWrapper nextPageComposite = rightPage.getChild("NextPage");
				if (isValid(nextPageComposite)) {
					int compositeEntity = nextPageComposite.getEntity();
					nextPageTransform = ComponentRetriever.get(compositeEntity, TransformComponent.class, sceneLoader.getEngine());
					nextPageDimensions = ComponentRetriever.get(compositeEntity, DimensionsComponent.class, sceneLoader.getEngine());

					ItemWrapper nextPageIcon = nextPageComposite.getChild("Next_Page");
					if (isValid(nextPageIcon)) {
						nextPageTint = ComponentRetriever.get(nextPageIcon.getEntity(), TintComponent.class, sceneLoader.getEngine());
					}
					if (nextPageTint == null) {
						nextPageTint = sceneLoader.getEngine().edit(compositeEntity).create(TintComponent.class);
						nextPageTint.color.set(0.58f, 0.58f, 0.58f, 1f);
					}
				}
			}
		}
	}

	@Override
	public void show() {
		if (viewport != null) {
			viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
		}
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.05f, 0.05f, 0.05f, 1f);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		viewport.apply();
		sceneLoader.getEngine().setDelta(delta);
		sceneLoader.getEngine().process();

		updateInput();
	}

	private void updateInput() {
		mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		viewport.unproject(mouseVector);
		boolean clicked = Gdx.input.justTouched();

		// Exit Button (root-level, no parent offset needed)
		if (exitTransform != null && exitDimensions != null) {
			Rectangle exitBounds = new Rectangle(
				exitTransform.x, exitTransform.y,
				exitDimensions.width * exitTransform.scaleX,
				exitDimensions.height * exitTransform.scaleY
			);
			boolean exitHovered = exitBounds.contains(mouseVector.x, mouseVector.y);

			if (exitTint != null) {
				exitTint.color.set(exitHovered ? 1.0f : 0.7f, exitHovered ? 1.0f : 0.7f, exitHovered ? 1.0f : 0.7f, 1.0f);
			}
			if (exitHovered && clicked) {
				game.showMainMenu();
			}
		}

		// Play Button (BOOKGUI -> RightPage -> Play_Button, 1 level, direct sum)
		if (playTransform != null && playDimensions != null && bookGuiTransform != null && rightPageTransform != null) {
			float x = bookGuiTransform.x + rightPageTransform.x + playTransform.x;
			float y = bookGuiTransform.y + rightPageTransform.y + playTransform.y;
			float w = playDimensions.width * playTransform.scaleX;
			float h = playDimensions.height * playTransform.scaleY;

			Rectangle playBounds = new Rectangle(x, y, w, h);
			boolean playHovered = playBounds.contains(mouseVector.x, mouseVector.y);

			if (playTint != null) {
				playTint.color.set(playHovered ? 1.0f : 0.58f, playHovered ? 1.0f : 0.58f, playHovered ? 1.0f : 0.58f, 1.0f);
			}
			if (playHovered && clicked) {
				game.showPage1();
			}
		}

		// Next Page Button (BOOKGUI -> RightPage -> NextPage composite, 1 level, direct sum)
		if (nextPageTransform != null && nextPageDimensions != null && bookGuiTransform != null && rightPageTransform != null) {
			float x = bookGuiTransform.x + rightPageTransform.x + nextPageTransform.x;
			float y = bookGuiTransform.y + rightPageTransform.y + nextPageTransform.y;
			float w = nextPageDimensions.width * nextPageTransform.scaleX;
			float h = nextPageDimensions.height * nextPageTransform.scaleY;

			Rectangle nextBounds = new Rectangle(x, y, w, h);
			boolean nextHovered = nextBounds.contains(mouseVector.x, mouseVector.y);

			if (nextPageTint != null) {
				nextPageTint.color.set(nextHovered ? 1.0f : 0.58f, nextHovered ? 1.0f : 0.58f, nextHovered ? 1.0f : 0.58f, 1.0f);
			}
			if (nextHovered && clicked) {
				// TODO: handle next page action
			}
		}
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
		if (sceneLoader != null) {
			sceneLoader = null;
		}
	}
}
