package com.github.glutton.weapons.pistol;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import java.util.LinkedList;

public class Bullet {

    private double x, y;
    private double oldX, oldY;
    private double speed = 22.0;
    private double vx, vy;
    private int damage = 25;

    // Motion trail history buffer using LibGDX Vector2
    private final LinkedList<Vector2> trail = new LinkedList<>();
    private final int maxTrailLength = 5;

    public Bullet(double startX, double startY, double angle, double weaponBulletSpeed, int weaponDamage) {
        this.x = startX;
        this.y = startY;
        this.oldX = startX;
        this.oldY = startY;

        this.speed = weaponBulletSpeed;
        this.damage = weaponDamage;

        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    public void update() {
        oldX = x;
        oldY = y;

        trail.addFirst(new Vector2((float) x, (float) y));
        if (trail.size() > maxTrailLength) {
            trail.removeLast();
        }

        x += vx;
        y += vy;
    }

    public void draw(ShapeRenderer shapeRenderer) {
        // --- RENDER MOTION TRAIL TAIL ---
        for (int i = 0; i < trail.size(); i++) {
            Vector2 pt = trail.get(i);
            float alpha = 1.0f - ((float) i / maxTrailLength);

            shapeRenderer.setColor(new Color(1.0f, 0.78f, 0.2f, alpha * 0.7f));
            shapeRenderer.rectLine(pt.x, pt.y, pt.x - (float)(vx * 0.2), pt.y - (float)(vy * 0.2), Math.max(1, 4 - i));
        }

        // --- RENDER BULLET HEAD ---
        shapeRenderer.setColor(Color.WHITE);
        shapeRenderer.rect((float) x - 2, (float) y - 2, 4, 4);
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getOldX() { return oldX; }
    public double getOldY() { return oldY; }
    public int getDamage() { return damage; }

    public void reduceDamageCapacity(int reductionAmount) {
        this.damage = Math.max(0, this.damage - reductionAmount);
    }

    // LibGDX equivalent of Line2D intersection against a target Rectangle
    public boolean checkPredictiveHitGuard(Rectangle targetBounds) {
        Vector2 start = new Vector2((float) oldX, (float) oldY);
        Vector2 end = new Vector2((float) x, (float) y);

        // Immediate raycast line intersection check
        if (Intersector.intersectSegmentRectangle(start, end, targetBounds)) {
            return true;
        }

        // High Speed Projection Guard
        if (this.speed > 25.0) {
            Vector2 scoutEnd = new Vector2((float)(x + vx), (float)(y + vy));
            if (Intersector.intersectSegmentRectangle(end, scoutEnd, targetBounds)) {
                return true;
            }
        }

        return false;
    }
}