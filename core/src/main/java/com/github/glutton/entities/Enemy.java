package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.github.glutton.player.PlayerState;

public abstract class Enemy {

    public final Rectangle bounds;

    protected int health;
    protected final int maxHealth;

    protected int attackDamage;
    protected float detectionRange;
    protected float attackRange;
    protected float attackCooldown;
    protected float attackTimer = 0f;
    protected float moveSpeed = 100f;

    protected float velocityY = 0f;
    protected boolean isGrounded = true;
    protected float gravity = -900f;
    protected float jumpVelocity = 350f;
    protected float jumpCooldown = 0f;

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

    public void updateAI(float delta, PlayerState player) {
        if (isDestroyed()) {
            return;
        }

        attackTimer = Math.max(0f, attackTimer - delta);
        jumpCooldown = Math.max(0f, jumpCooldown - delta);

        float enemyCenterX = bounds.x + bounds.width / 2f;
        float enemyCenterY = bounds.y + bounds.height / 2f;

        float dx = player.x - enemyCenterX;
        float dy = player.y - enemyCenterY;
        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared <= detectionRange * detectionRange) {
            aggro = true;
        }

        if (!aggro) {
            return;
        }

        if (distanceSquared > attackRange * attackRange) {
            float moveX = 0f;
            if (dx > 0) {
                moveX = moveSpeed * delta;
            } else if (dx < 0) {
                moveX = -moveSpeed * delta;
            }
            bounds.x += moveX;
        }

        velocityY += gravity * delta;
        bounds.y += velocityY * delta;

        if (bounds.y <= 150f) {
            bounds.y = 150f;
            velocityY = 0f;
            isGrounded = true;
        } else {
            isGrounded = false;
        }

        if (isGrounded && jumpCooldown <= 0f) {
            boolean playerIsJumpingOrHigher = player.movementState == PlayerState.MovementState.JUMP || player.y > enemyCenterY + 30f;
            if (playerIsJumpingOrHigher && distanceSquared < (detectionRange * 0.75f) * (detectionRange * 0.75f)) {
                velocityY = jumpVelocity;
                isGrounded = false;
                jumpCooldown = 1.5f;
            }
        }
    }

    public void updateAggro(float delta, float playerX, float playerY) {
    }

    public boolean canAttack(float playerX, float playerY) {
        if (!aggro || isDestroyed() || attackTimer > 0f) {
            return false;
        }

        float enemyCenterX = bounds.x + bounds.width / 2f;
        float enemyCenterY = bounds.y + bounds.height / 2f;

        float dx = playerX - enemyCenterX;
        float dy = playerY - enemyCenterY;

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
        velocityY = 0f;
        isGrounded = true;
    }

    public abstract void render(ShapeRenderer shapeRenderer);
}