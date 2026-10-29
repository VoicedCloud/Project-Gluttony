package com.github.glutton.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Cursor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.github.glutton.GluttonGame;
import com.github.glutton.SessionManager;
import com.github.glutton.AuthClient;
import games.rednblack.editor.renderer.SceneConfiguration;
import games.rednblack.editor.renderer.SceneLoader;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.JsonValue;
import games.rednblack.editor.renderer.components.DimensionsComponent;
import games.rednblack.editor.renderer.components.TransformComponent;
import games.rednblack.editor.renderer.components.TintComponent;
import games.rednblack.editor.renderer.components.label.LabelComponent;
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
	private float logoutDefaultScaleX = 0.8f, logoutDefaultScaleY = 0.8f;

	private TransformComponent signinTransform;
	private DimensionsComponent signinDimensions;
	private float signinDefaultScaleX = 0.8f, signinDefaultScaleY = 0.8f;

	// UI Text Components for Dynamic Update
	private LabelComponent usernameLabel;
	private LabelComponent useridLabel;

	// Stat Labels
	private LabelComponent compRecent, pointsRecent, monstersRecent, wavesRecent;
	private LabelComponent compHighest, pointsHighest, monstersHighest, wavesHighest;

	private final Vector3 mouseVector = new Vector3();
	private float pollTimer = 0f;
	private boolean isWaitingForBrowserLogin = false;

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

		// 3. Account Window Overlay & Nested Elements
		ItemWrapper accountWindow = mainRoot.getChild("AccountWindow");
		if (isValid(accountWindow)) {
			int windowEntity = accountWindow.getEntity();
			accountWindowTransform = ComponentRetriever.get(windowEntity, TransformComponent.class, sceneLoader.getEngine());
			accountWindowDimensions = ComponentRetriever.get(windowEntity, DimensionsComponent.class, sceneLoader.getEngine());

			ItemWrapper userDetailsOuter = accountWindow.getChild("UserDetails");
			if (isValid(userDetailsOuter)) {
				ItemWrapper userDetailsInner = userDetailsOuter.getChild("UserDetails");
				if (isValid(userDetailsInner)) {
					ItemWrapper uname = userDetailsInner.getChild("Username");
					if (isValid(uname)) {
						usernameLabel = ComponentRetriever.get(uname.getEntity(), LabelComponent.class, sceneLoader.getEngine());
					}

					ItemWrapper uid = userDetailsInner.getChild("UserID");
					if (isValid(uid)) {
						useridLabel = ComponentRetriever.get(uid.getEntity(), LabelComponent.class, sceneLoader.getEngine());
					}
				}
			}

// Traverse AccountWindow > Stats
			ItemWrapper statsContainer = accountWindow.getChild("Stats");
			if (isValid(statsContainer)) {
				// Recent Stats: Stats > StatsRecent > StatsRecent
				ItemWrapper statsRecentOuter = statsContainer.getChild("StatsRecent");
				if (isValid(statsRecentOuter)) {
					ItemWrapper statsRecentInner = statsRecentOuter.getChild("StatsRecent");
					ItemWrapper targetRecent = isValid(statsRecentInner) ? statsRecentInner : statsRecentOuter;

					compRecent = getLabel(targetRecent, "CompletionBooleanRecent");
					pointsRecent = getLabel(targetRecent, "PointsEarnedRecent");
					monstersRecent = getLabel(targetRecent, "MonstersKilledRecent");
					wavesRecent = getLabel(targetRecent, "WavesSurvivedRecent");
				}

				// Highest Stats: Stats > StatsHighest > Stats
				ItemWrapper statsHighestOuter = statsContainer.getChild("StatsHighest");
				if (isValid(statsHighestOuter)) {
					ItemWrapper statsHighestInner = statsHighestOuter.getChild("Stats");
					ItemWrapper targetHighest = isValid(statsHighestInner) ? statsHighestInner : statsHighestOuter;

					compHighest = getLabel(targetHighest, "CompletionBooleanHighest");
					pointsHighest = getLabel(targetHighest, "PointsEarnedHighest");
					monstersHighest = getLabel(targetHighest, "MonstersKilledHighest");
					wavesHighest = getLabel(targetHighest, "WavesSurvivedHighest");
				}
			}
			ItemWrapper logoutBtn = accountWindow.getChild("LogOutButton");
			if (isValid(logoutBtn)) {
				logoutTransform = ComponentRetriever.get(logoutBtn.getEntity(), TransformComponent.class, sceneLoader.getEngine());
				logoutDimensions = ComponentRetriever.get(logoutBtn.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
				if (logoutTransform != null) {
					logoutDefaultScaleX = logoutTransform.scaleX;
					logoutDefaultScaleY = logoutTransform.scaleY;
				}
			}

			ItemWrapper signinBtn = accountWindow.getChild("SignInButton");
			if (isValid(signinBtn)) {
				signinTransform = ComponentRetriever.get(signinBtn.getEntity(), TransformComponent.class, sceneLoader.getEngine());
				signinDimensions = ComponentRetriever.get(signinBtn.getEntity(), DimensionsComponent.class, sceneLoader.getEngine());
				if (signinTransform != null) {
					boolean loggedIn = SessionManager.isLoggedIn();
					signinTransform.scaleX = loggedIn ? 0.0f : signinDefaultScaleX;
					signinTransform.scaleY = loggedIn ? 0.0f : signinDefaultScaleY;
				}
			}
		}
	}

	private LabelComponent getLabel(ItemWrapper parent, String childName) {
		ItemWrapper child = parent.getChild(childName);
		if (isValid(child)) {
			LabelComponent label = ComponentRetriever.get(child.getEntity(), LabelComponent.class, sceneLoader.getEngine());
			if (label != null) {
				Gdx.app.log("UIBinding", "Successfully bound label: " + childName);
			} else {
				Gdx.app.error("UIBinding", "Node found but LabelComponent is null for: " + childName);
			}
			return label;
		}
		Gdx.app.error("UIBinding", "Failed to find child node: " + childName);
		return null;
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

		if (accountWindowTransform != null) {
			float targetScale = isAccountOpen ? 1.0f : 0.0f;
			accountWindowTransform.scaleX = targetScale;
			accountWindowTransform.scaleY = targetScale;

			if (isAccountOpen) {
				updateAccountUIData();

				if (isWaitingForBrowserLogin && !SessionManager.isLoggedIn()) {
					pollTimer += delta;
					if (pollTimer >= 1.5f) {
						pollTimer = 0f;
						checkBrowserLogin();
					}
				}
			}
		}

		sceneLoader.getEngine().setDelta(delta);
		sceneLoader.getEngine().process();

		updateInput();
	}

	private void checkBrowserLogin() {
		AuthClient.checkSession(new AuthClient.AuthListener() {
			@Override
			public void onSuccess(String message, JsonValue data) {
				long userId = data.getLong("user_id", 0);
				String username = data.getString("username", "Player");
				SessionManager.login(userId, username);
				isWaitingForBrowserLogin = false;
				Gdx.app.log("Auth", "Successfully logged in via browser! User ID: " + userId);

				AuthClient.getStats(userId, new AuthClient.AuthListener() {
					@Override
					public void onSuccess(String msg, JsonValue statsData) {
						Gdx.app.log("StatsDebug", "Raw Stats JSON: " + statsData.toString());
						SessionManager.recentStats = statsData.get("recent");
						SessionManager.highestStats = statsData.get("highest");
						updateAccountUIData();
						Gdx.app.log("Auth", "Server stats fetched successfully for UI.");
					}

					@Override
					public void onError(String err) {
						Gdx.app.error("Auth", "Failed to fetch stats: " + err);
					}
				});
			}

			@Override
			public void onError(String message) {
			}
		});
	}

	private void updateAccountUIData() {
		if (usernameLabel != null) {
			usernameLabel.setText(SessionManager.getUsername());
		}
		if (useridLabel != null) {
			useridLabel.setText(SessionManager.isLoggedIn() ? String.valueOf(SessionManager.getUserId()) : "XXXXXXXXXXXX");
		}

		boolean loggedIn = SessionManager.isLoggedIn();
		if (logoutTransform != null) {
			logoutTransform.scaleX = loggedIn ? logoutDefaultScaleX : 0.0f;
			logoutTransform.scaleY = loggedIn ? logoutDefaultScaleY : 0.0f;
		}
		if (signinTransform != null) {
			signinTransform.scaleX = loggedIn ? 0.0f : signinDefaultScaleX;
			signinTransform.scaleY = loggedIn ? 0.0f : signinDefaultScaleY;
		}

		if (loggedIn) {
			if (SessionManager.recentStats != null) {
				if (compRecent != null) {
					compRecent.setText(String.valueOf(SessionManager.recentStats.getInt("completion", 0)));
				}
				if (pointsRecent != null) {
					pointsRecent.setText(String.valueOf(SessionManager.recentStats.getInt("cash_earned", 0)));
				}
				if (monstersRecent != null) {
					monstersRecent.setText(String.valueOf(SessionManager.recentStats.getInt("monsters_killed", 0)));
				}
				if (wavesRecent != null) {
					wavesRecent.setText(String.valueOf(SessionManager.recentStats.getInt("waves_survived", 0)));
				}
			}
			if (SessionManager.highestStats != null) {
				if (compHighest != null) {
					compHighest.setText(String.valueOf(SessionManager.highestStats.getInt("completion", 0)));
				}
				if (pointsHighest != null) {
					pointsHighest.setText(String.valueOf(SessionManager.highestStats.getInt("cash_earned", 0)));
				}
				if (monstersHighest != null) {
					monstersHighest.setText(String.valueOf(SessionManager.highestStats.getInt("monsters_killed", 0)));
				}
				if (wavesHighest != null) {
					wavesHighest.setText(String.valueOf(SessionManager.highestStats.getInt("waves_survived", 0)));
				}
			}
		} else {
			if (compRecent != null) {
				compRecent.setText("0");
			}
			if (pointsRecent != null) {
				pointsRecent.setText(String.valueOf(StatsTracker.getRecentCash()));
			}
			if (monstersRecent != null) {
				monstersRecent.setText(String.valueOf(StatsTracker.getRecentMonsters()));
			}
			if (wavesRecent != null) {
				wavesRecent.setText(String.valueOf(StatsTracker.getRecentWaves()));
			}

			if (compHighest != null) {
				compHighest.setText("0");
			}
			if (pointsHighest != null) {
				pointsHighest.setText(String.valueOf(StatsTracker.getHighestCash()));
			}
			if (monstersHighest != null) {
				monstersHighest.setText(String.valueOf(StatsTracker.getHighestMonsters()));
			}
			if (wavesHighest != null) {
				wavesHighest.setText(String.valueOf(StatsTracker.getHighestWaves()));
			}
		}
	}

	private void updateInput() {
		mouseVector.set(Gdx.input.getX(), Gdx.input.getY(), 0);
		viewport.unproject(mouseVector);

		boolean clicked = Gdx.input.justTouched();

		if (isAccountOpen) {
			boolean loggedIn = SessionManager.isLoggedIn();

			if (loggedIn && logoutTransform != null && logoutDimensions != null && accountWindowTransform != null) {
				Rectangle logoutBounds = getBounds(logoutTransform, logoutDimensions, accountWindowTransform);
				if (logoutBounds.contains(mouseVector.x, mouseVector.y) && clicked) {
					SessionManager.logout();
					Gdx.app.log("Account", "Logged out successfully!");
				}
			}

			if (!loggedIn && signinTransform != null && signinDimensions != null && accountWindowTransform != null) {
				Rectangle signinBounds = getBounds(signinTransform, signinDimensions, accountWindowTransform);
				if (signinBounds.contains(mouseVector.x, mouseVector.y) && clicked) {
					Gdx.app.log("Auth", "Opening browser login page...");
					isWaitingForBrowserLogin = true;
					pollTimer = 0f;
					Gdx.net.openURI("https://careless-overdrive-valiant.ngrok-free.dev/glutton/index.html");
				}
			}

			if (clicked && accountWindowTransform != null && accountWindowDimensions != null) {
				Rectangle windowBounds = new Rectangle(
					accountWindowTransform.x, accountWindowTransform.y,
					accountWindowDimensions.width * accountWindowTransform.scaleX,
					accountWindowDimensions.height * accountWindowTransform.scaleY
				);
				if (!windowBounds.contains(mouseVector.x, mouseVector.y)) {
					isAccountOpen = false;
					isWaitingForBrowserLogin = false;
				}
			}
			return;
		}

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
				game.showDifficultySettings();
			}
		}

		if (exitTransform != null && exitDimensions != null) {
			boolean isHovered = getBounds(exitTransform, exitDimensions, buttonsStackingTransform).contains(mouseVector.x, mouseVector.y);
			if (exitTint != null) {
				exitTint.color.set(isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f);
			}
			if (isHovered && clicked) {
				Gdx.app.exit();
			}
		}

		if (accountTransform != null && accountDimensions != null) {
			boolean isHovered = getBounds(accountTransform, accountDimensions, null).contains(mouseVector.x, mouseVector.y);
			if (accountTint != null) {
				accountTint.color.set(isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f, isHovered ? 1.0f : 0.58f);
			}
			if (isHovered && clicked) {
				isAccountOpen = true;
				if (SessionManager.isLoggedIn()) {
					AuthClient.getStats(SessionManager.getUserId(), new AuthClient.AuthListener() {
						@Override
						public void onSuccess(String message, JsonValue data) {
							Gdx.app.log("StatsDebug", "Raw Stats JSON: " + data.toString());
							SessionManager.recentStats = data.get("recent");
							SessionManager.highestStats = data.get("highest");
							updateAccountUIData(); // <--- Refresh UI immediately here!
						}

						@Override
						public void onError(String message) {
							Gdx.app.error("Auth", "Failed to fetch stats: " + message);
						}
					});
				}
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
		if (sceneLoader != null) {
			sceneLoader = null;
		}
	}
}
