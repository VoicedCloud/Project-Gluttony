package com.github.glutton.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class BulletManager {

	public static class Impact {
		public final float x, y;
		public final int damage;
		public float age = 0f;

		public Impact(float x, float y, int damage) {
			this.x = x;
			this.y = y;
			this.damage = damage;
		}
	}

	private final List<Bullet> bullets = new ArrayList<>();
	private final ImpactManager impactManager;

	private final Map<WeaponProfile, Texture> bulletTextures = new EnumMap<>(WeaponProfile.class);
	private final Map<WeaponProfile, TextureRegion> bulletRegions = new EnumMap<>(WeaponProfile.class);

	private static final float DEFAULT_LIFESPAN = 1.5f;
	private static final float BULLET_RADIUS = 1.5f;

	public BulletManager(String defaultTexturePath, ImpactManager impactManager) {
		this.impactManager = impactManager;
		for (WeaponProfile wp : WeaponProfile.values()) {
			String path = wp.bulletTexturePath;
			if (!Gdx.files.internal(path).exists()) {
				path = defaultTexturePath;
			}
			Texture tex = new Texture(Gdx.files.internal(path));
			bulletTextures.put(wp, tex);
			bulletRegions.put(wp, new TextureRegion(tex));
		}
	}

	public void spawn(float x, float y, float angleRad, float speed, int damage, WeaponProfile weapon) {
		float vx = (float) Math.cos(angleRad) * speed;
		float vy = (float) Math.sin(angleRad) * speed;
		bullets.add(new Bullet(x, y, vx, vy, damage, DEFAULT_LIFESPAN, weapon));
	}

	public void update(float delta) {
		Iterator<Bullet> it = bullets.iterator();
		while (it.hasNext()) {
			Bullet b = it.next();
			b.x += b.vx * delta;
			b.y += b.vy * delta;
			b.lifeRemaining -= delta;
			b.updateTrail(delta);

			if (b.lifeRemaining <= 0f) {
				impactManager.addImpact(b.x, b.y, b.damage);
				it.remove();
			}
		}
		impactManager.update(delta);
	}

	public void drawTrails(ShapeRenderer shapeRenderer, Matrix4 projectionMatrix) {
		shapeRenderer.setProjectionMatrix(projectionMatrix);
		shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

		for (Bullet b : bullets) {
			for (int i = 0; i < b.trail.size - 1; i++) {
				Vector2 p1 = b.trail.get(i);
				Vector2 p2 = b.trail.get(i + 1);

				float alpha1 = 1f - ((float) i / b.trail.size);
				float alpha2 = 1f - ((float) (i + 1) / b.trail.size);
				float avgAlpha = (alpha1 + alpha2) / 2f;

				shapeRenderer.setColor(1f, 1f, 0.8f, avgAlpha * 0.7f);
				shapeRenderer.line(p1.x, p1.y, p2.x, p2.y);
			}
		}

		shapeRenderer.end();
	}

	public void draw(SpriteBatch batch) {
		for (Bullet b : bullets) {
			TextureRegion region = bulletRegions.get(b.weapon);
			if (region == null) {
				region = bulletRegions.get(WeaponProfile.PISTOL);
			}
			float angleDeg = (float) Math.toDegrees(Math.atan2(b.vy, b.vx));
			float width = region.getRegionWidth() * 0.15f;
			float height = region.getRegionHeight() * 0.15f;

			batch.draw(region,
				b.x - width / 2f, b.y - height / 2f,
				width / 2f, height / 2f,
				width, height,
				1f, 1f,
				angleDeg);
		}
	}

	public List<Bullet> getBullets() {
		return Collections.unmodifiableList(bullets);
	}

	public List<ImpactManager.Impact> getRecentImpacts() {
		return impactManager.getRecentImpacts();
	}

	public float getBulletRadius() {
		return BULLET_RADIUS;
	}

	public int getActiveCount() {
		return bullets.size();
	}

	public void dispose() {
		for (Texture tex : bulletTextures.values()) {
			if (tex != null) {
				tex.dispose();
			}
		}
	}
}