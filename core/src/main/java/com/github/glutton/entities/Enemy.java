
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
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

    protected float moveSpeed = 50f;
    protected float velocityY = 0f;
    protected boolean isGrounded = false;

    protected float gravity = -900f;
    protected float jumpVelocity = 350f;
    protected float jumpCooldown = 0f;

    protected boolean aggro = false;

    protected final Vector2 spawnPosition = new Vector2();

    protected final World world;
    protected Body body;
    protected boolean bodyDisposed = false;
    protected int groundContacts = 0;

    public Enemy(
            World world,
            float x,
            float y,
            float width,
            float height,
            int health,
            int attackDamage,
            float detectionRange,
            float attackRange,
            float attackCooldown
    ) {
        if (world == null) {
            throw new IllegalArgumentException(
                "Enemy World cannot be null."
            );
        }

        this.world = world;
        this.bounds = new Rectangle(x, y, width, height);

        this.health = Math.max(0, health);
        this.maxHealth = Math.max(0, health);
        this.attackDamage = Math.max(0, attackDamage);

        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
        this.attackCooldown = Math.max(0f, attackCooldown);

        spawnPosition.set(x, y);

        createBody();
    }

    private void createBody() {
        BodyDef bodyDef = new BodyDef();

        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            bounds.x + bounds.width / 2f,
            bounds.y + bounds.height / 2f
        );
        bodyDef.fixedRotation = true;
        bodyDef.allowSleep = false;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            bounds.width / 2f,
            bounds.height / 2f
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.density = 1f;
        fixtureDef.friction = 0.2f;

        body.createFixture(fixtureDef);
        shape.dispose();

        body.setLinearVelocity(0f, 0f);
    }

    public void syncBounds() {
        if (body == null || bodyDisposed) {
            return;
        }

        Vector2 position = body.getPosition();

        bounds.setPosition(
            position.x - bounds.width / 2f,
            position.y - bounds.height / 2f
        );
    }

    private void syncBodyToBounds() {
        if (body == null || bodyDisposed || world.isLocked()) {
            return;
        }

        body.setTransform(
            bounds.x + bounds.width / 2f,
            bounds.y + bounds.height / 2f,
            0f
        );
    }

    public void disposeBody() {
        if (bodyDisposed || world.isLocked()) {
            return;
        }

        if (body != null) {
            body.setUserData(null);
            world.destroyBody(body);
            body = null;
        }

        bodyDisposed = true;
    }

    public boolean isBodyDisposed() {
        return bodyDisposed;
    }

    public void handleGroundContactBegin(Contact contact) {
        if (bodyDisposed || contact == null) {
            return;
        }

        groundContacts++;
        isGrounded = true;
    }

    public void handleGroundContactEnd(Contact contact) {
        if (bodyDisposed || contact == null) {
            return;
        }

        groundContacts = Math.max(0, groundContacts - 1);
        isGrounded = groundContacts > 0;
    }

    public void takeDamage(int amount) {
        if (amount > 0 && !isDestroyed()) {
            health = Math.max(0, health - amount);
        }
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    // Regular enemies use their configured damage.
    public int getAttackDamage() {
        return attackDamage;
    }

    public boolean isAggro() {
        return aggro;
    }

    public void updateAI(float delta, PlayerState player) {
        if (isDestroyed()
                || player == null
                || bodyDisposed
                || delta <= 0f) {
            return;
        }

        syncBounds();

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
            bounds.x += Math.signum(dx) * moveSpeed * delta;
        }

        // Preserve the existing jump behavior.
        if (isGrounded && jumpCooldown <= 0f) {
            boolean playerIsHigher =
                player.movementState == PlayerState.MovementState.JUMP
                || player.y > enemyCenterY + 30f;

            float jumpDetectionRange = detectionRange * 0.75f;

            if (playerIsHigher
                    && distanceSquared
                    < jumpDetectionRange * jumpDetectionRange) {
                bounds.y += jumpVelocity * delta;
                jumpCooldown = 1.5f;
            }
        }

        syncBodyToBounds();
    }

    public void updateAggro(
            float delta,
            float playerX,
            float playerY
    ) {
        // Retained for compatibility with existing code.
    }

    public boolean canAttack(float playerX, float playerY) {
        if (!aggro
                || isDestroyed()
                || bodyDisposed
                || attackTimer > 0f) {
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
        if (bodyDisposed || world.isLocked()) {
            return;
        }

        health = maxHealth;
        bounds.setPosition(spawnPosition);

        aggro = false;
        attackTimer = 0f;
        velocityY = 0f;
        jumpCooldown = 0f;

        groundContacts = 0;
        isGrounded = false;

        syncBodyToBounds();

        if (body != null) {
            body.setLinearVelocity(0f, 0f);
        }
    }

    public abstract void render(ShapeRenderer shapeRenderer);
}
