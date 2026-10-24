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
import games.rednblack.editor.renderer.components.TintComponent; // Changed from ButtonComponent
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;

public class MainMenuScreen implements Screen {

    private final GluttonGame game;
    private SceneLoader sceneLoader;
    private Viewport viewport;
    private OrthographicCamera camera;

    // The parent anchor container tracking reference
    private TransformComponent buttonsStackingTransform;

    private TransformComponent playTransform;
    private DimensionsComponent playDimensions;
    private TintComponent playTint;

    private TransformComponent settingsTransform;
    private DimensionsComponent settingsDimensions;
    private TintComponent settingsTint;

    private TransformComponent exitTransform;
    private DimensionsComponent exitDimensions;
    private TintComponent exitTint;

    private TransformComponent accountTransform;
    private DimensionsComponent accountDimensions;
    private TintComponent accountTint;

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

        // 1. Fetch the parent composite item using the exact name from the visual editor
        ItemWrapper buttonsStacking = root.getChild("Buttons Stacking");

        if (isValid(buttonsStacking)) {
            // Grab the parent container's world transform component
            buttonsStackingTransform = ComponentRetriever.get(buttonsStacking.getEntity(), TransformComponent.class, sceneLoader.getEngine());

            // 2. Fetch the buttons as children of the composite container instead of root
            ItemWrapper play = buttonsStacking.getChild("PlayButton");
            if (isValid(play)) {
                playTransform = ComponentRetriever.get(play.getEntity(), TransformComponent.class, sceneLoader.getEngine());
                playDimensions = ComponentRetriever.get(play.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
                playTint = ComponentRetriever.get(play.getEntity(), TintComponent.class, sceneLoader.getEngine());
            }

            ItemWrapper settings = buttonsStacking.getChild("SettingsButton");
            if (isValid(settings)) {
                settingsTransform = ComponentRetriever.get(settings.getEntity(), TransformComponent.class, sceneLoader.getEngine());
                settingsDimensions = ComponentRetriever.get(settings.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
                settingsTint = ComponentRetriever.get(settings.getEntity(), TintComponent.class, sceneLoader.getEngine());
            }

            ItemWrapper exit = buttonsStacking.getChild("ExitButton");
            if (isValid(exit)) {
                exitTransform = ComponentRetriever.get(exit.getEntity(), TransformComponent.class, sceneLoader.getEngine());
                exitDimensions = ComponentRetriever.get(exit.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
                exitTint = ComponentRetriever.get(exit.getEntity(), TintComponent.class, sceneLoader.getEngine());
            }
        }

        // The account button is still directly in the root layout, so it remains unchanged
        ItemWrapper account = root.getChild("AccountButton");
        if (isValid(account)) {
            accountTransform = ComponentRetriever.get(account.getEntity(), TransformComponent.class, sceneLoader.getEngine());
            accountDimensions = ComponentRetriever.get(account.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
            accountTint = ComponentRetriever.get(account.getEntity(), TintComponent.class, sceneLoader.getEngine());
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

        // 1. Process PLAY button hover tint and clicks
        if (playTransform != null && playDimensions != null) {
            boolean isHovered = getBounds(playTransform, playDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
            
            if (playTint != null) {
                // Mimics your JSON definition: Full bright white when hovered, dimmed transparent gray when normal
                if (isHovered) {
                    playTint.color.set(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    playTint.color.set(0.58f, 0.58f, 0.58f, 0.58f);
                }
            }
            if (isHovered && clicked) {
                onPlay();
            }
        }

        // 2. Process EXIT button hover tint and clicks
        if (exitTransform != null && exitDimensions != null) {
            boolean isHovered = getBounds(exitTransform, exitDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
            
            if (exitTint != null) {
                if (isHovered) {
                    exitTint.color.set(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    exitTint.color.set(0.58f, 0.58f, 0.58f, 0.58f);
                }
            }
            if (isHovered && clicked) {
                onExit();
            }
        }

        // 3. Process SETTINGS button hover tint and clicks
        if (settingsTransform != null && settingsDimensions != null) {
            boolean isHovered = getBounds(settingsTransform, settingsDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
            
            if (settingsTint != null) {
                if (isHovered) {
                    settingsTint.color.set(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    settingsTint.color.set(0.58f, 0.58f, 0.58f, 0.58f);
                }
            }
            if (isHovered && clicked) {
                onSettings();
            }
        }

        // 4. Process ACCOUNT button hover tint and clicks
        if (accountTransform != null && accountDimensions != null) {
            boolean isHovered = getBounds(accountTransform, accountDimensions, null).contains(mouseVector.x, mouseVector.y);
            
            if (accountTint != null) {
                if (isHovered) {
                    accountTint.color.set(1.0f, 1.0f, 1.0f, 1.0f);
                } else {
                    accountTint.color.set(0.58f, 0.58f, 0.58f, 0.58f);
                }
            }
            if (isHovered && clicked) {
                onAccount();
            }
        }
    }

    // Updated bounds method: Takes an optional parent transform to automatically factor in container boxes
    private Rectangle getBounds(TransformComponent t, DimensionsComponent d, TransformComponent parent) {
        float w = d.width * t.scaleX;
        float h = d.height * t.scaleY;

        // If a parent container exists, add its world positions into the final result
        float offsetX = (parent != null) ? parent.x : 0;
        float offsetY = (parent != null) ? parent.y : 0;

        // Apply scale factors to the local child coordinates inside the composite
        float childX = t.x * (parent != null ? parent.scaleX : 1.0f);
        float childY = t.y * (parent != null ? parent.scaleY : 1.0f);

        float x = offsetX + childX - (t.originX * (t.scaleX - 1));
        float y = offsetY + childY - (t.originY * (t.scaleY - 1));
        return new Rectangle(x, y, w, h);
    }

    private void onPlay() {
        game.setScreen(new LevelSelectScreen(game));
    }

    private void onExit() {
        Gdx.app.exit();
    }

    private void onSettings() {
    }

    private void onAccount() {
    }

    @Override
    public void dispose() {
    }
}
