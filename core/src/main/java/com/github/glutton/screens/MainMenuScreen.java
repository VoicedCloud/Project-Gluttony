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
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.utils.ComponentRetriever;
import games.rednblack.editor.renderer.utils.ItemWrapper;

public class MainMenuScreen implements Screen {

    private final GluttonGame game;
    private Viewport viewport;
    private OrthographicCamera camera;
    private SceneLoader sceneLoader;

    // Main Menu Buttons
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

    // Account Window & Elements
    private boolean isAccountOpen = false;
    private TransformComponent accountWindowTransform;
    private DimensionsComponent accountWindowDimensions;

    private TransformComponent logoutTransform;
    private DimensionsComponent logoutDimensions;
    private TintComponent logoutTint;

    private final Vector3 mouseVector = new Vector3();

    private boolean isValid(ItemWrapper w) {
        return w != null && w.getEntity() >= 0;
    }

    public MainMenuScreen(GluttonGame game) {
        this.game = game;

        camera = new OrthographicCamera();
        viewport = new FitViewport(1280f, 720f, camera);
        viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        SceneConfiguration config = new SceneConfiguration();
        sceneLoader = new SceneLoader(config);
        sceneLoader.loadScene("MainMenu", viewport);

        ItemWrapper mainRoot = new ItemWrapper(sceneLoader.getRoot(), sceneLoader.getEngine());
        
        // 1. Buttons Stacking
        ItemWrapper buttonsStacking = mainRoot.getChild("Buttons Stacking");
        if (isValid(buttonsStacking)) {
            buttonsStackingTransform = ComponentRetriever.get(buttonsStacking.getEntity(), TransformComponent.class, sceneLoader.getEngine());

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

        // 2. Account Button
        ItemWrapper account = mainRoot.getChild("AccountButton");
        if (isValid(account)) {
            accountTransform = ComponentRetriever.get(account.getEntity(), TransformComponent.class, sceneLoader.getEngine());
            accountDimensions = ComponentRetriever.get(account.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
            accountTint = ComponentRetriever.get(account.getEntity(), TintComponent.class, sceneLoader.getEngine());
        }

        // 3. Account Window Overlay & LogOut Button
        ItemWrapper accountWindow = mainRoot.getChild("AccountWindow");
        if (isValid(accountWindow)) {
            int windowEntity = accountWindow.getEntity();
            accountWindowTransform = ComponentRetriever.get(windowEntity, TransformComponent.class, sceneLoader.getEngine());
            accountWindowDimensions = ComponentRetriever.get(windowEntity, DimensionsComponent.class, sceneLoader.getEngine());

            ItemWrapper logoutBtn = accountWindow.getChild("LogOutButton");
            if (isValid(logoutBtn)) {
                int logoutEntity = logoutBtn.getEntity();
                logoutTransform = ComponentRetriever.get(logoutEntity, TransformComponent.class, sceneLoader.getEngine());
                logoutDimensions = ComponentRetriever.get(logoutEntity, DimensionsComponent.class, sceneLoader.getEngine());
                
                // Traverse through Composite Item to reach LogoutBG
                ItemWrapper compositeItem = logoutBtn.getChild("Composite Item");
                ItemWrapper logoutBg = isValid(compositeItem) ? compositeItem.getChild("LogoutBG") : null;
                if (!isValid(logoutBg)) {
                    logoutBg = logoutBtn.getChild("LogoutBG");
                }
                
                if (isValid(logoutBg)) {
                    logoutTint = ComponentRetriever.get(logoutBg.getEntity(), TintComponent.class, sceneLoader.getEngine());
                }
                if (logoutTint == null) {
                    logoutTint = ComponentRetriever.get(logoutEntity, TintComponent.class, sceneLoader.getEngine());
                }
                if (logoutTint == null) {
                    logoutTint = sceneLoader.getEngine().edit(logoutEntity).create(TintComponent.class);
                    logoutTint.color.set(0.58f, 0.58f, 0.58f, 1f);
                }
            }
        }
    }

    @Override
    public void show() {
        Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
        if (viewport != null) {
            viewport.update(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
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

        if (accountWindowTransform != null) {
            float targetScale = isAccountOpen ? 1.0f : 0.0f;
            accountWindowTransform.scaleX = targetScale;
            accountWindowTransform.scaleY = targetScale;
        }

        sceneLoader.getEngine().setDelta(delta);
        sceneLoader.getEngine().process();

        updateInput();
    }

    private void updateInput() {
        mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
        viewport.unproject(mouseVector);
        
        boolean clicked = Gdx.input.justTouched();

        if (isAccountOpen) {
            // --- ACCOUNT WINDOW OPEN: Handle overlay interactions & block background ---
            if (logoutTransform != null && logoutDimensions != null && accountWindowTransform != null) {
                Rectangle logoutBounds = getBounds(logoutTransform, logoutDimensions, accountWindowTransform);
                boolean logoutHovered = logoutBounds.contains(mouseVector.x, mouseVector.y);

                if (logoutTint != null) {
                    logoutTint.color.set(
                        logoutHovered ? 1.0f : 0.58f, 
                        logoutHovered ? 0.3f : 0.58f, 
                        logoutHovered ? 0.3f : 0.58f, 
                        1.0f
                    );
                }
                if (logoutHovered && clicked) {
                    Gdx.app.log("Account", "Logged out successfully!");
                    isAccountOpen = false;
                }
            }

            // Click outside AccountWindow bounds to close
            if (clicked && accountWindowTransform != null && accountWindowDimensions != null) {
                Rectangle windowBounds = new Rectangle(
                    accountWindowTransform.x, accountWindowTransform.y,
                    accountWindowDimensions.width * accountWindowTransform.scaleX,
                    accountWindowDimensions.height * accountWindowTransform.scaleY
                );
                if (!windowBounds.contains(mouseVector.x, mouseVector.y)) {
                    isAccountOpen = false;
                }
            }
            return;
        }

        // --- NORMAL MAIN MENU INPUT ---
        if (playTransform != null && playDimensions != null) {
            boolean isHovered = getBounds(playTransform, playDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
            if (playTint != null) {
                playTint.color.set(isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f);
            }
            if (isHovered && clicked) {
                game.showLevelSelect();
            }
        }

        if (settingsTransform != null && settingsDimensions != null) {
            boolean isHovered = getBounds(settingsTransform, settingsDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
            if (settingsTint != null) {
                settingsTint.color.set(isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f);
            }
            if (isHovered && clicked) {
                // TODO: Hook up settings screen when ready
            }
        }

        if (exitTransform != null && exitDimensions != null) {
            boolean isHovered = getBounds(exitTransform, exitDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
            if (exitTint != null) {
                exitTint.color.set(isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f);
            }
            if (isHovered && clicked) Gdx.app.exit();
        }

        if (accountTransform != null && accountDimensions != null) {
            boolean isHovered = getBounds(accountTransform, accountDimensions, null).contains(mouseVector.x, mouseVector.y);
            if (accountTint != null) {
                accountTint.color.set(isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f);
            }
            if (isHovered && clicked) {
                isAccountOpen = true;
            }
        }
    }

    private Rectangle getBounds(TransformComponent t, DimensionsComponent d, TransformComponent parent) {
        float w = d.width * t.scaleX;
        float h = d.height * t.scaleY;
        float offsetX = (parent != null) ? parent.x : 0;
        float offsetY = (parent != null) ? parent.y : 0;
        float childX = t.x * (parent != null ? parent.scaleX : 1.0f);
        float childY = t.y * (parent != null ? parent.scaleY : 1.0f);
        float x = offsetX + childX - (t.originX * (t.scaleX - 1));
        float y = offsetY + childY - (t.originY * (t.scaleY - 1));
        return new Rectangle(x, y, w, h);
    }

    @Override
    public void dispose() {
        if (sceneLoader != null) sceneLoader = null;
    }
}