package com.github.glutton.weapons;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

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

	private final Texture bulletTexture;
	private final TextureRegion bulletRegion;

	private static final float DEFAULT_LIFESPAN = 1.5f;
	private static final float BULLET_RADIUS = 1.5f;

	public BulletManager(String texturePath, ImpactManager impactManager) {
		this.impactManager = impactManager;
		bulletTexture = new Texture(Gdx.files.internal(texturePath));
		bulletRegion = new TextureRegion(bulletTexture);
	}

	public void spawn(float x, float y, float angleRad, float speed, int damage) {
		float vx = (float) Math.cos(angleRad) * speed;
		float vy = (float) Math.sin(angleRad) * speed;
		bullets.add(new Bullet(x, y, vx, vy, damage, DEFAULT_LIFESPAN));
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

				// Calculate alpha smoothly based on how far back the point is in the array
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
			float angleDeg = (float) Math.toDegrees(Math.atan2(b.vy, b.vx));
			float width = bulletTexture.getWidth() * 0.15f;
			float height = bulletTexture.getHeight() * 0.15f;

			batch.draw(bulletRegion,
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
		if (bulletTexture != null) {
			bulletTexture.dispose();
		}
	}
}
