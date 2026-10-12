package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.github.glutton.GluttonGame;

public class MainMenuScreen implements Screen {

	private final GluttonGame game;
	private Stage stage;
	private Texture bgTexture;

	private BitmapFont titleFont;
	private BitmapFont buttonFont;
	private Texture buttonBgTexture;

	public MainMenuScreen(GluttonGame game) {
		this.game = game;
	}

	@Override
	public void show() {
		Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
		stage = new Stage(new ScreenViewport());
		Gdx.input.setInputProcessor(stage);

		game.playMenuBgm();

		if (Gdx.files.internal("UI/Menu/MenuBG.png").exists()) {
			bgTexture = new Texture(Gdx.files.internal("UI/Menu/MenuBG.png"));
		} else if (Gdx.files.internal("UI/Menu/MenuBG.jpg").exists()) {
			bgTexture = new Texture(Gdx.files.internal("UI/Menu/MenuBG.jpg"));
		}

		if (bgTexture != null) {
			Image bgImage = new Image(bgTexture);
			bgImage.setFillParent(true);
			stage.addActor(bgImage);
		}

		titleFont = loadCustomFont("UI/Fonts/SwingingWake-Regular.ttf", 90, 2);
		buttonFont = loadCustomFont("UI/Fonts/rat_typewriter.ttf", 24, 1);

		Label.LabelStyle titleStyle = new Label.LabelStyle(titleFont, new Color(158f / 255f, 43f / 255f, 5f / 255f, 1f));

		Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
		pixmap.setColor(Color.BLACK);
		pixmap.fill();
		buttonBgTexture = new Texture(pixmap);
		pixmap.dispose();

		TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
		buttonStyle.font = buttonFont;
		buttonStyle.fontColor = Color.WHITE;
		buttonStyle.up = new TextureRegionDrawable(new TextureRegion(buttonBgTexture));

		Table table = new Table();
		table.setFillParent(true);
		stage.addActor(table);

		Label titleLabel = new Label("GLUTTON", titleStyle);
		table.add(titleLabel).padBottom(50).row();

		// Play Button
		TextButton playButton = new TextButton("PLAY", buttonStyle);
		addHandCursor(playButton);
		playButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.graphics.setSystemCursor(Cursor.SystemCursor.Arrow);
				game.setScreen(new LevelSelectScreen(game));
			}
		});
		table.add(playButton).width(240).height(50).padBottom(15).row();

		// Settings Button
		TextButton settingsButton = new TextButton("SETTINGS", buttonStyle);
		addHandCursor(settingsButton);
		settingsButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
			}
		});
		table.add(settingsButton).width(240).height(50).padBottom(15).row();

		// Exit Button
		TextButton exitButton = new TextButton("EXIT", buttonStyle);
		addHandCursor(exitButton);
		exitButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				Gdx.app.exit();
			}
		});
		table.add(exitButton).width(240).height(50).padBottom(15).row();
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

	private BitmapFont loadCustomFont(String path, int size, int spaceX) {
		if (!Gdx.files.internal(path).exists()) {
			return new BitmapFont();
		}
		FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal(path));
		FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
		parameter.size = size;
		parameter.spaceX = spaceX;
		BitmapFont font = generator.generateFont(parameter);
		generator.dispose();
		return font;
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
		stage.dispose();
		if (bgTexture != null) {
			bgTexture.dispose();
		}
		if (buttonBgTexture != null) {
			buttonBgTexture.dispose();
		}
		if (titleFont != null) {
			titleFont.dispose();
		}
		if (buttonFont != null) {
			buttonFont.dispose();
		}
	}
}
