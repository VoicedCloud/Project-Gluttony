package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;

public class MainMenuScreen implements Screen {

    private final GluttonGame game;
    private SceneLoader sceneLoader;
    private Viewport viewport;
    private OrthographicCamera camera;

    private TransformComponent playTransform;
    private DimensionsComponent playDimensions;
    private TransformComponent settingsTransform;
    private DimensionsComponent settingsDimensions;
    private TransformComponent exitTransform;
    private DimensionsComponent exitDimensions;
    private TransformComponent accountTransform;
    private DimensionsComponent accountDimensions;

    private final Vector3 mouseVector = new Vector3();

    private boolean isValid(ItemWrapper w) {
        return w != null && w.getEntity() >= 0;
    }

    public MainMenuScreen(GluttonGame game) {
        this.game = game;
    }

    @Override
    public void show() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280f, 720f, camera);

        SceneConfiguration config = new SceneConfiguration();
        sceneLoader = new SceneLoader(config);
        sceneLoader.loadScene("MainMenu", viewport);

        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        game.playMenuBgm();

        ItemWrapper root = new ItemWrapper(sceneLoader.getRoot(), sceneLoader.getEngine());

        ItemWrapper play = root.getChild("PlayButton");
        if (isValid(play)) {
            playTransform = ComponentRetriever.get(play.getEntity(), TransformComponent.class, sceneLoader.getEngine());
            playDimensions = ComponentRetriever.get(play.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
        }

        ItemWrapper settings = root.getChild("SettingsButton");
        if (isValid(settings)) {
            settingsTransform = ComponentRetriever.get(settings.getEntity(), TransformComponent.class, sceneLoader.getEngine());
            settingsDimensions = ComponentRetriever.get(settings.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
        }

        ItemWrapper exit = root.getChild("ExitButton");
        if (isValid(exit)) {
            exitTransform = ComponentRetriever.get(exit.getEntity(), TransformComponent.class, sceneLoader.getEngine());
            exitDimensions = ComponentRetriever.get(exit.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
        }

        ItemWrapper account = root.getChild("AccountButton");
        if (isValid(account)) {
            accountTransform = ComponentRetriever.get(account.getEntity(), TransformComponent.class, sceneLoader.getEngine());
            accountDimensions = ComponentRetriever.get(account.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}

    @Override
    public void hide() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
    }

    @Override
    public void render(float delta) {
        viewport.apply();
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        sceneLoader.getEngine().setDelta(delta);
        sceneLoader.getEngine().process();

        updateInput();
    }

    private void updateInput() {
        mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouseVector);
        boolean clicked = Gdx.input.justTouched();

        if (clicked && playTransform != null && playDimensions != null && getBounds(playTransform, playDimensions).contains(mouseVector.x, mouseVector.y)) {
            onPlay();
        }
        if (clicked && exitTransform != null && exitDimensions != null && getBounds(exitTransform, exitDimensions).contains(mouseVector.x, mouseVector.y)) {
            onExit();
        }
        if (clicked && settingsTransform != null && settingsDimensions != null && getBounds(settingsTransform, settingsDimensions).contains(mouseVector.x, mouseVector.y)) {
            onSettings();
        }
        if (clicked && accountTransform != null && accountDimensions != null && getBounds(accountTransform, accountDimensions).contains(mouseVector.x, mouseVector.y)) {
            onAccount();
        }
    }

    private Rectangle getBounds(TransformComponent t, DimensionsComponent d) {
        float w = d.width * t.scaleX;
        float h = d.height * t.scaleY;
        float x = t.x - (t.originX * (t.scaleX - 1));
        float y = t.y - (t.originY * (t.scaleY - 1));
        return new Rectangle(x, y, w, h);
    }

    private void onPlay() {
        game.setScreen(new LevelSelectScreen(game));
    }

    private void onExit() {
        Gdx.app.exit();
    }

    private void onSettings() {}

    private void onAccount() {}

    @Override
    public void dispose() {}
}