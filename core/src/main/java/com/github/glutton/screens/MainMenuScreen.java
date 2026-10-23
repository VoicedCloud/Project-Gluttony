package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.glutton.GluttonGame;
import games.rednblack.editor.renderer.SceneLoader;

public class MainMenuScreen implements Screen {

	private final GluttonGame game;
	private SceneLoader sceneLoader;
	private Stage stage;

	public MainMenuScreen(GluttonGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);

		// Initialize HyperLap2D SceneLoader and load the MainMenu scene
		sceneLoader = new SceneLoader();
		com.badlogic.gdx.scenes.scene2d.Group rootGroup = sceneLoader.loadScene("MainMenu");
		
		// Create stage using screen viewport and add the HyperLap2D root actor group
		stage = new Stage(new ScreenViewport());
		stage.addActor(rootGroup);
		Gdx.input.setInputProcessor(stage);

		game.playMenuBgm();

		// Wire up HyperLap2D UI Buttons by their itemIdentifiers
		Actor playBtn = rootGroup.findActor("PlayButton");
		if (playBtn != null) {
			addHandCursor(playBtn);
			playBtn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
					game.setScreen(new LevelSelectScreen(game));
				}
			});
		}

		Actor settingsBtn = rootGroup.findActor("SettingsButton");
		if (settingsBtn != null) {
			addHandCursor(settingsBtn);
			settingsBtn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					// TODO: Add Settings menu logic or dialog here
				}
			});
		}

		Actor accountBtn = rootGroup.findActor("AccountButton");
		if (accountBtn != null) {
			addHandCursor(accountBtn);
			accountBtn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					// TODO: Add Account/Login modal popup logic here
				}
			});
		}

		Actor exitBtn = rootGroup.findActor("ExitButton");
		if (exitBtn != null) {
			addHandCursor(exitBtn);
			exitBtn.addListener(new ClickListener() {
				@Override
				public void clicked(InputEvent event, float x, float y) {
					Gdx.app.exit();
				}
			});
		}
	}

	private void addHandCursor(Actor actor) {
		actor.addListener(new ClickListener() {
			@Override
			public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
				Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Hand);
			}

			@Override
			public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
				Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
			}
		});
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0, 0, 0, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
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
	public void dispose() {
		if (stage != null) {
			stage.dispose();
		}
	}
}