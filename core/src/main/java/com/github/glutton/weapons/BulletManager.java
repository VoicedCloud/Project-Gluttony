package com.github.glutton.weapons;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import com.badlogic.gdx.math.Matrix4;

public class BulletManager {

    private final List<Bullet> bullets = new ArrayList<>();
    private static final float DEFAULT_LIFESPAN = 1.5f; // seconds
    private static final float BULLET_RADIUS = 1.5f; // placeholder visual size, world units

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
            if (b.lifeRemaining <= 0f) {
                it.remove();
            }
        }
    }

    public void draw(ShapeRenderer shapeRenderer, Matrix4 projectionMatrix) {
        shapeRenderer.setProjectionMatrix(projectionMatrix);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(Color.YELLOW);
        for (Bullet b : bullets) {
            shapeRenderer.circle(b.x, b.y, BULLET_RADIUS, 8);
        }
        shapeRenderer.end();
    }

    public int getActiveCount() {
        return bullets.size();
    }
}
