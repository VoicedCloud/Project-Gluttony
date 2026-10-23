package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public abstract class Enemy {

    public final Rectangle bounds;

    protected int health;
    protected final int maxHealth;

    protected int attackDamage;
    protected float detectionRange;
    protected float attackRange;
    protected float attackCooldown;
    protected float attackTimer = 0f;
    protected float moveSpeed = 90f;

    protected boolean aggro = false;
    protected final Vector2 spawnPosition = new Vector2();

    public Enemy(
            float x, float y,
            float width, float height,
            int health,
            int attackDamage,
            float detectionRange,
            float attackRange,
            float attackCooldown
    ) {
        bounds = new Rectangle(x, y, width, height);

        this.health = health;
        this.maxHealth = health;
        this.attackDamage = attackDamage;
        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;

        spawnPosition.set(x, y);
    }

    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) {
            health = 0;
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public int getAttackDamage() {
        return attackDamage;
    }

    public boolean isAggro() {
        return aggro;
    }

    public void updateAggro(float delta, float playerX, float playerY) {
        if (isDestroyed()) {
            return;
        }

        attackTimer = Math.max(0f, attackTimer - delta);

        float enemyX = bounds.x + bounds.width / 2f;
        float enemyY = bounds.y + bounds.height / 2f;

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;
        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared <= detectionRange * detectionRange) {
            aggro = true;
        }

        if (!aggro) {
            return;
        }

        // Chase the player while outside attack range.
        if (distanceSquared > attackRange * attackRange) {
            float distance = (float) Math.sqrt(distanceSquared);

            if (distance > 0f) {
                float moveX = dx / distance * moveSpeed * delta;
                float moveY = dy / distance * moveSpeed * delta;

                bounds.x += moveX;
                bounds.y += moveY;
            }
        }
    }

    public boolean canAttack(float playerX, float playerY) {
        if (!aggro || isDestroyed() || attackTimer > 0f) {
            return false;
        }

        float enemyX = bounds.x + bounds.width / 2f;
        float enemyY = bounds.y + bounds.height / 2f;

        float dx = playerX - enemyX;
        float dy = playerY - enemyY;

        if (dx * dx + dy * dy <= attackRange * attackRange) {
            attackTimer = attackCooldown;
            return true;
        }

        return false;
    }

    public void resetForRespawn() {
        health = maxHealth;
        bounds.setPosition(spawnPosition);
        aggro = false;
        attackTimer = 0f;
    }

    public abstract void render(ShapeRenderer shapeRenderer);
}