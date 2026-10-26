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

	private TransformComponent exitTransform;
	private DimensionsComponent exitDimensions;
	private TintComponent exitHighlightTint;

	private TransformComponent bookGuiTransform;
	private TransformComponent rightPageTransform;

	private TransformComponent nextPageTransform;
	private DimensionsComponent nextPageDimensions;
	private TintComponent nextPageHighlightTint;

	private TransformComponent playTransform;
	private DimensionsComponent playDimensions;

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

		ItemWrapper exitBtn = rootWrapper.getChild("ExitButton");
		if (isValid(exitBtn)) {
			int entity = exitBtn.getEntity();
			exitTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
			exitDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
		}

		ItemWrapper exitHighlightBtn = rootWrapper.getChild("HIGHLIGHTEDExitButton");
		if (isValid(exitHighlightBtn)) {
			exitHighlightTint = ComponentRetriever.get(exitHighlightBtn.getEntity(), TintComponent.class, sceneLoader.getEngine());
			if (exitHighlightTint != null) exitHighlightTint.color.a = 0f;
		}

		ItemWrapper bookGui = rootWrapper.getChild("BOOKGUI");
		if (isValid(bookGui)) {
			bookGuiTransform = ComponentRetriever.get(bookGui.getEntity(), TransformComponent.class, sceneLoader.getEngine());
			
			ItemWrapper rightPage = bookGui.getChild("RightPage");
			if (isValid(rightPage)) {
				rightPageTransform = ComponentRetriever.get(rightPage.getEntity(), TransformComponent.class, sceneLoader.getEngine());

				ItemWrapper playBtn = rightPage.getChild("Play_Button");
				if (isValid(playBtn)) {
					int entity = playBtn.getEntity();
					playTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
					playDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
				}

				ItemWrapper nextPageComposite = rightPage.getChild("NextPage");
				if (isValid(nextPageComposite)) {
					ItemWrapper nextPageBtn = nextPageComposite.getChild("Next_Page");
					if (isValid(nextPageBtn)) {
						int entity = nextPageBtn.getEntity();
						nextPageTransform = ComponentRetriever.get(entity, TransformComponent.class, sceneLoader.getEngine());
						nextPageDimensions = ComponentRetriever.get(entity, DimensionsComponent.class, sceneLoader.getEngine());
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
		sceneLoader.getEngine().process();

		updateHoverAndInput();
	}

	private void updateHoverAndInput() {
		mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		viewport.unproject(mouseVector);

		if (exitTransform != null && exitDimensions != null) {
			Rectangle bounds = getEntityBounds(exitTransform, exitDimensions, null, null);
			boolean isHovered = bounds.contains(mouseVector.x, mouseVector.y);
			if (exitHighlightTint != null) exitHighlightTint.color.a = isHovered ? 1f : 0f;
			if (isHovered && Gdx.input.justTouched()) {
				game.showMainMenu();
			}
		}

		if (playTransform != null && playDimensions != null) {
			Rectangle bounds = getEntityBounds(playTransform, playDimensions, bookGuiTransform, rightPageTransform);
			boolean isHovered = bounds.contains(mouseVector.x, mouseVector.y);
			if (isHovered && Gdx.input.justTouched()) {
				game.showPage1();
			}
		}
	}

	private Rectangle getEntityBounds(TransformComponent transform, DimensionsComponent dimensions, TransformComponent p1, TransformComponent p2) {
		float p1X = (p1 != null) ? p1.x : 0f;
		float p1Y = (p1 != null) ? p1.y : 0f;
		float p2X = (p2 != null) ? p2.x : 0f;
		float p2Y = (p2 != null) ? p2.y : 0f;

		float x = p1X + p2X + transform.x - (transform.originX * (transform.scaleX - 1));
		float y = p1Y + p2Y + transform.y - (transform.originY * (transform.scaleY - 1));
		float width = dimensions.width * transform.scaleX;
		float height = dimensions.height * transform.scaleY;
		return new Rectangle(x, y, width, height);
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height, true);
	}

	@Override public void pause() {}
	@Override public void resume() {}
	@Override public void hide() {}

	@Override
	public void dispose() {
		if (sceneLoader != null) sceneLoader = null;
	}
}