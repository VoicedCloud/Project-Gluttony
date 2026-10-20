package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public abstract class Enemy {
    public final Rectangle bounds;
    protected int health;
    protected final int maxHealth;

    public Enemy(float x, float y, float width, float height, int health) {
        this.bounds = new Rectangle(x, y, width, height);
        this.health = health;
        this.maxHealth = health;
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public abstract void render(ShapeRenderer shapeRenderer);
}