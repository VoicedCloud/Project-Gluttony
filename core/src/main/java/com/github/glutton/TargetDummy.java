package com.github.glutton;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class TargetDummy {

    private float x, y;
    private final float width = 30f;
    private final float height = 50f;
    private final Rectangle bounds = new Rectangle();

    private int health = 50;
    private final int maxHealth = 50;

    public TargetDummy(float x, float y) {
        this.x = x;
        this.y = y;
        this.bounds.set(x, y, width, height);
    }

    public void setPosition(float x, float y) {
        this.x = x;
        this.y = y;
        this.bounds.setPosition(x, y);
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public boolean isDestroyed() { return health <= 0; }
    public int getHealth() { return health; }
    public Rectangle getBounds() { return bounds; }

    public void draw(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(x, y, width, height);

        if (health > 0) {
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(x, y + height - 6, width, 6);

            shapeRenderer.setColor(Color.GREEN);
            float healthWidth = ((float) health / maxHealth) * width;
            shapeRenderer.rect(x, y + height - 6, healthWidth, 6);
        }
    }
}