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
import com.github.glutton.weapons.WeaponState;
import com.badlogic.gdx.audio.Sound;

public class PlayerGunRenderer {

	private final Texture gunTexture;
	private final TextureRegion gunRegion;
	private final Sound shootSound;

	private final Texture shootTexture;
	private final Animation<TextureRegion> shootAnimation;

	private final Texture reloadTexture;
	private final Animation<TextureRegion> reloadAnimation;

	private final Sound slideLockSound;
	private final Sound reload1Sound;
	private final Sound reload2Sound;
	private final Sound slideReleaseSound;

	private final Vector2 muzzleWorld = new Vector2();

	public PlayerGunRenderer(String texturePath) {
		gunTexture = new Texture(Gdx.files.internal(texturePath));
		gunRegion = new TextureRegion(gunTexture);

		shootSound = Gdx.audio.newSound(Gdx.files.internal("audio/weapon/Snake's Authentic Gun Sounds/Full Sound/9mm/WAV/9mm Single.wav"));

		// Load 12-frame shoot animation sheet (64x48 per frame)
		shootTexture = new Texture(Gdx.files.internal("sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[SHOOT WITH CASING AND MUZZLE FLASH] Glock - P80.png"));
		TextureRegion[][] shootTmp = TextureRegion.split(shootTexture, 64, 48);
		shootAnimation = new Animation<>(0.02f, shootTmp[0]);
		shootAnimation.setPlayMode(Animation.PlayMode.NORMAL);

		// Load 16-frame reload animation sheet (64x48 per frame)
		reloadTexture = new Texture(Gdx.files.internal("sprites/weapons/02 - Sprite sheets/Glock - P80 [64x48]/[RELOAD] Glock - P80.png"));
		TextureRegion[][] reloadTmp = TextureRegion.split(reloadTexture, 64, 48);
		reloadAnimation = new Animation<>(0.075f, reloadTmp[0]);
		reloadAnimation.setPlayMode(Animation.PlayMode.NORMAL);

		slideLockSound = Gdx.audio.newSound(Gdx.files.internal("audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Slide Lock.wav"));
		reload1Sound = Gdx.audio.newSound(Gdx.files.internal("audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Reload 1.wav"));
		reload2Sound = Gdx.audio.newSound(Gdx.files.internal("audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Reload 2.wav"));
		slideReleaseSound = Gdx.audio.newSound(Gdx.files.internal("audio/weapon/Snake's Authentic Gun Sounds/& More/Pistol/WAV/9mm Pistol Slide Release.wav"));
	}

	public void draw(SpriteBatch batch, PlayerState player) {
		float pivotX = player.getGunPivotWorldX();
		float pivotY = player.getGunPivotWorldY();
		WeaponState ws = player.weaponState;

		TextureRegion currentRegion = gunRegion;

		if (ws.isReloading) {
			long elapsedReload = TimeUtils.timeSinceMillis(ws.reloadStartTime);
			float reloadProgress = Math.min(1f, (float) elapsedReload / ws.activeWeapon.reloadMs);

			float totalAnimDuration = reloadAnimation.getAnimationDuration();
			float animTime;
			if (reloadProgress <= 0.5f) {
				animTime = (reloadProgress / 0.5f) * totalAnimDuration;
			} else {
				animTime = (2f - (reloadProgress / 0.5f)) * totalAnimDuration;
			}
			currentRegion = reloadAnimation.getKeyFrame(Math.max(0f, Math.min(animTime, totalAnimDuration)));
		} else {
			long elapsedFire = TimeUtils.timeSinceMillis(ws.lastFireTime);
			if (elapsedFire < ws.activeWeapon.fireDelayMs) {
				float fireProgress = (float) elapsedFire / ws.activeWeapon.fireDelayMs;
				currentRegion = shootAnimation.getKeyFrame(fireProgress * shootAnimation.getAnimationDuration());
			}
		}

		float angleDeg = (float) Math.toDegrees(ws.aimAngleRad);
		boolean flipY = Math.cos(ws.aimAngleRad) < 0;
		if (currentRegion.isFlipY() != flipY) {
			currentRegion.flip(false, true);
		}

		float gunWidth = 64f * player.gunScale;
		float gunHeight = 48f * player.gunScale;

		batch.draw(currentRegion,
			pivotX, pivotY - gunHeight / 2f,
			0, gunHeight / 2f,
			gunWidth, gunHeight,
			1f, 1f,
			angleDeg);
	}

	public void drawMuzzleFlash(ShapeRenderer shapeRenderer, PlayerState player) {
		WeaponState ws = player.weaponState;
		long elapsedFire = TimeUtils.timeSinceMillis(ws.lastFireTime);
		if (elapsedFire < 60f) {
			// Use exact same bullet spawn coordinate reference
			Vector2 muzzle = getMuzzleWorldPos(player);
			float flashX = muzzle.x;
			float flashY = muzzle.y;

			shapeRenderer.flush();

			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE);

			float scale = player.gunScale;

			// 1. Outer glow halo (adjustable opacity via alpha channel)
			shapeRenderer.setColor(1f, 0.9f, 0.5f, 0.25f);
			shapeRenderer.circle(flashX, flashY, 7f * scale, 12);

			// 2. Mid glow ring
			shapeRenderer.setColor(1f, 1f, 0.6f, 0.5f);
			shapeRenderer.circle(flashX, flashY, 4f * scale, 10);

			// 3. Inner core
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

		float gunWidth = 64f * player.gunScale;
		float gunHeight = 48f * player.gunScale;

		boolean flipY = Math.cos(ws.aimAngleRad) < 0;

		float localX = player.muzzleOffsetX * player.gunScale;
		float localY = player.muzzleOffsetY * player.gunScale;

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

	public void playShootSound() {
		shootSound.play();
	}

	public void playSlideLock() {
		slideLockSound.play();
	}

	public void playReload1() {
		reload1Sound.play();
	}

	public void playReload2() {
		reload2Sound.play();
	}

	public void playSlideRelease() {
		slideReleaseSound.play();
	}

	public void dispose() {
		gunTexture.dispose();
		shootTexture.dispose();
		reloadTexture.dispose();
		shootSound.dispose();
		slideLockSound.dispose();
		reload1Sound.dispose();
		reload2Sound.dispose();
		slideReleaseSound.dispose();
	}
}
