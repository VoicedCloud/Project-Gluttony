package com.github.glutton.world;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.github.glutton.player.PlayerAnimator;
import com.github.glutton.player.PlayerController;
import com.github.glutton.player.PlayerGunRenderer;
import com.github.glutton.player.PlayerState;
import com.github.glutton.weapons.BulletManager;
import com.github.glutton.weapons.ImpactManager;

public class GameplaySession {

	private final PlayerState playerState;
	private final PlayerController playerController;
	private final PlayerAnimator playerAnimator;
	private final PlayerGunRenderer gunRenderer;
	private final BulletManager bulletManager;
	private final ImpactManager impactManager;
	private final LevelCollision levelCollision;

	public GameplaySession(OrthographicCamera camera, String gunTexturePath, String bulletTexturePath, LevelCollision levelCollision) {
		playerState = new PlayerState();
		playerState.x = 0f;
		playerState.y = 300f;

		this.levelCollision = levelCollision != null ? levelCollision : new LevelCollision();
		this.impactManager = new ImpactManager();
		bulletManager = new BulletManager(bulletTexturePath, impactManager);
		gunRenderer = new PlayerGunRenderer(gunTexturePath);
		playerController = new PlayerController(camera, bulletManager, gunRenderer);
		playerAnimator = new PlayerAnimator();
	}

	public GameplaySession(OrthographicCamera camera, String gunTexturePath, String bulletTexturePath) {
		this(camera, gunTexturePath, bulletTexturePath, new LevelCollision());
	}

	public GameplaySession(OrthographicCamera camera, String gunTexturePath) {
		this(camera, gunTexturePath, "sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[CASING] Glock - P80.png", new LevelCollision());
	}

	public void update(float delta) {
		playerController.update(playerState, delta, levelCollision);
		playerAnimator.update(delta);
		bulletManager.update(delta);
	}

	public void render(SpriteBatch batch, ShapeRenderer shapeRenderer) {
		playerAnimator.draw(batch, playerState);
		gunRenderer.draw(batch, playerState);
		bulletManager.draw(batch);
	}

	public void renderMuzzleFlash(ShapeRenderer shapeRenderer) {
		gunRenderer.drawMuzzleFlash(shapeRenderer, playerState);
	}

	public void renderTrails(ShapeRenderer shapeRenderer, Matrix4 projectionMatrix) {
		bulletManager.drawTrails(shapeRenderer, projectionMatrix);
	}

	public PlayerState getPlayerState() {
		return playerState;
	}

	public PlayerGunRenderer getGunRenderer() {
		return gunRenderer;
	}

	public BulletManager getBulletManager() {
		return bulletManager;
	}

	public ImpactManager getImpactManager() {
		return impactManager;
	}

	public LevelCollision getLevelCollision() {
		return levelCollision;
	}

	public void dispose() {
		if (playerAnimator != null) {
			playerAnimator.dispose();
		}
		if (gunRenderer != null) {
			gunRenderer.dispose();
		}
		if (bulletManager != null) {
			bulletManager.dispose();
		}
	}
}
