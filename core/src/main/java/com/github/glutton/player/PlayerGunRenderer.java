package com.github.glutton.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.TimeUtils;
import com.github.glutton.weapons.WeaponProfile;
import com.github.glutton.weapons.WeaponState;
import com.badlogic.gdx.audio.Sound;
import java.util.EnumMap;
import java.util.Map;

public class PlayerGunRenderer {

	private static class WeaponAssets {
		public Texture gunTexture;
		public TextureRegion gunRegion;
		public Texture shootTexture;
		public Animation<TextureRegion> shootAnimation;
		public Texture reloadTexture;
		public Animation<TextureRegion> reloadAnimation;
	}

	private final Map<WeaponProfile, WeaponAssets> weaponAssetsMap = new EnumMap<>(WeaponProfile.class);

	private final Sound shootSound;
	private final Vector2 muzzleWorld = new Vector2();

	public PlayerGunRenderer(String defaultTexturePath) {
		for (WeaponProfile wp : WeaponProfile.values()) {
			WeaponAssets assets = new WeaponAssets();
			try {
				assets.gunTexture = new Texture(Gdx.files.internal(wp.idleTexturePath));
			} catch (Exception e) {
				assets.gunTexture = new Texture(Gdx.files.internal(defaultTexturePath));
			}
			assets.gunRegion = new TextureRegion(assets.gunTexture);

			try {
				assets.shootTexture = new Texture(Gdx.files.internal(wp.shootTexturePath));
			} catch (Exception e) {
				assets.shootTexture = new Texture(Gdx.files.internal("sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png"));
			}
			TextureRegion[][] shootTmp = TextureRegion.split(assets.shootTexture, wp.frameWidth, wp.frameHeight);
			assets.shootAnimation = new Animation<>(0.02f, shootTmp[0]);
			assets.shootAnimation.setPlayMode(Animation.PlayMode.NORMAL);

			try {
				assets.reloadTexture = new Texture(Gdx.files.internal(wp.reloadTexturePath));
			} catch (Exception e) {
				assets.reloadTexture = new Texture(Gdx.files.internal("sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png"));
			}
			TextureRegion[][] reloadTmp = TextureRegion.split(assets.reloadTexture, wp.frameWidth, wp.frameHeight);
			TextureRegion[] origReload = reloadTmp[0];

			TextureRegion[] combinedReload = new TextureRegion[origReload.length * 2];
			for (int i = 0; i < origReload.length; i++) {
				if (wp.reverseStitchOrder) {
					combinedReload[i] = origReload[origReload.length - 1 - i];
					combinedReload[origReload.length + i] = origReload[i];
				} else {
					combinedReload[i] = origReload[i];
					combinedReload[origReload.length + i] = origReload[origReload.length - 1 - i];
				}
			}

			float totalReloadDurationSeconds = wp.reloadMs / 1000f;
			float frameDuration = totalReloadDurationSeconds / combinedReload.length;
			assets.reloadAnimation = new Animation<>(frameDuration, combinedReload);
			assets.reloadAnimation.setPlayMode(Animation.PlayMode.NORMAL);

			weaponAssetsMap.put(wp, assets);

			// Load weapon-specific shoot sound
			if (wp.shootSoundPath != null && !wp.shootSoundPath.isEmpty()) {
				try {
					wp.shootSound = Gdx.audio.newSound(Gdx.files.internal(wp.shootSoundPath));
				} catch (Exception e) {
					// Fallback if audio file is missing
				}
			}

			// Load reload milestone sounds
			for (WeaponProfile.ReloadMilestone m : wp.milestones) {
				if (m.soundPath != null && !m.soundPath.isEmpty()) {
					try {
						m.sound = Gdx.audio.newSound(Gdx.files.internal(m.soundPath));
					} catch (Exception e) {
						// Fallback if missing
					}
				}
			}
		}

		shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/weapon/Snake's Authentic Gun Sounds/Full Sound/9mm/WAV/9mm Single.wav"));
	}

	public void draw(SpriteBatch batch, PlayerState player) {
		float pivotX = player.getGunPivotWorldX();
		float pivotY = player.getGunPivotWorldY();
		WeaponState ws = player.weaponState;
		WeaponProfile wp = ws.activeWeapon;
		WeaponAssets assets = weaponAssetsMap.get(wp);

		TextureRegion currentRegion = assets.gunRegion;

		if (ws.isReloading) {
			long elapsedReload = TimeUtils.timeSinceMillis(ws.reloadStartTime);
			float reloadProgress = Math.min(1f, (float) elapsedReload / wp.reloadMs);

			float animTime = reloadProgress * assets.reloadAnimation.getAnimationDuration();
			currentRegion = assets.reloadAnimation.getKeyFrame(animTime);
		} else {
			long elapsedFire = TimeUtils.timeSinceMillis(ws.lastFireTime);
			if (elapsedFire < wp.fireDelayMs) {
				float fireProgress = (float) elapsedFire / wp.fireDelayMs;
				currentRegion = assets.shootAnimation.getKeyFrame(fireProgress * assets.shootAnimation.getAnimationDuration());
			}
		}

		float angleDeg = (float) Math.toDegrees(ws.aimAngleRad);
		boolean flipY = Math.cos(ws.aimAngleRad) < 0;
		if (currentRegion.isFlipY() != flipY) {
			currentRegion.flip(false, true);
		}

		float gunWidth = wp.frameWidth * wp.gunScale;
		float gunHeight = wp.frameHeight * wp.gunScale;

		batch.draw(currentRegion,
			pivotX, pivotY - gunHeight / 2f,
			0, gunHeight / 2f,
			gunWidth, gunHeight,
			1f, 1f,
			angleDeg);
	}

	public void drawMuzzleFlash(ShapeRenderer shapeRenderer, PlayerState player) {
		WeaponState ws = player.weaponState;
		WeaponProfile wp = ws.activeWeapon;
		long elapsedFire = TimeUtils.timeSinceMillis(ws.lastFireTime);
		if (elapsedFire < 60f) {
			Vector2 muzzle = getMuzzleWorldPos(player);
			float flashX = muzzle.x;
			float flashY = muzzle.y;

			shapeRenderer.flush();

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

			float scale = wp.gunScale;

			shapeRenderer.setColor(1f, 0.9f, 0.5f, 0.25f);
			shapeRenderer.circle(flashX, flashY, 7f * scale, 12);

			shapeRenderer.setColor(1f, 1f, 0.6f, 0.5f);
			shapeRenderer.circle(flashX, flashY, 4f * scale, 10);

			shapeRenderer.setColor(1f, 1f, 1f, 0.9f);
			shapeRenderer.circle(flashX, flashY, 1.5f * scale, 8);

			shapeRenderer.flush();

			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			Gdx.gl.glDisable(GL20.GL_BLEND);
		}
	}

	public Vector2 getMuzzleWorldPos(PlayerState player) {
		float pivotX = player.getGunPivotWorldX();
		float pivotY = player.getGunPivotWorldY();
		WeaponState ws = player.weaponState;
		WeaponProfile wp = ws.activeWeapon;

		float gunWidth = wp.frameWidth * wp.gunScale;
		float gunHeight = wp.frameHeight * wp.gunScale;

		boolean flipY = Math.cos(ws.aimAngleRad) < 0;

		float localX = wp.muzzleOffsetX * wp.gunScale;
		float localY = wp.muzzleOffsetY * wp.gunScale;

		if (flipY) {
			localY = gunHeight - localY;
		}

		float relX = localX;
		float relY = localY - gunHeight / 2f;

		float angle = ws.aimAngleRad;
		float cos = (float) Math.cos(angle);
		float sin = (float) Math.sin(angle);

		float rotatedX = relX * cos - relY * sin;
		float rotatedY = relX * sin + relY * cos;

		muzzleWorld.set(pivotX + rotatedX, pivotY + rotatedY);
		return muzzleWorld;
	}

	public void playShootSound(WeaponProfile wp) {
		if (wp.shootSound != null) {
			wp.shootSound.play();
		} else if (shootSound != null) {
			shootSound.play();
		}
	}

	public void dispose() {
		for (WeaponAssets assets : weaponAssetsMap.values()) {
			if (assets.gunTexture != null) assets.gunTexture.dispose();
			if (assets.shootTexture != null) assets.shootTexture.dispose();
			if (assets.reloadTexture != null) assets.reloadTexture.dispose();
		}
		for (WeaponProfile wp : WeaponProfile.values()) {
			if (wp.shootSound != null) {
				wp.shootSound.dispose();
			}
			for (WeaponProfile.ReloadMilestone m : wp.milestones) {
				if (m.sound != null) {
					m.sound.dispose();
				}
			}
		}
		if (shootSound != null) shootSound.dispose();
	}
}