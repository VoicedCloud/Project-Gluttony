
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.github.glutton.player.PlayerState;
import com.github.glutton.world.PhysicsConfig;

public abstract class Enemy {

    public final Rectangle bounds;

    protected final World world;
    protected final Body body;
    protected final Fixture footFixture;

    protected int health;
    protected final int maxHealth;
    protected int attackDamage;

    protected float detectionRange;
    protected float attackRange;
    protected float attackCooldown;
    protected float attackTimer = 0f;
    protected float jumpCooldown = 0f;

    protected boolean aggro = false;
    protected boolean bodyDisposed = false;

    protected int groundContacts = 0;
    protected final Vector2 spawnPosition = new Vector2();

    public Enemy(
            World world,
            float x, float y,
            float width, float height,
            int health,
            int attackDamage,
            float detectionRange,
            float attackRange,
            float attackCooldown
    ) {
        if (world == null) {
            throw new IllegalArgumentException(
                "Enemy world cannot be null"
            );
        }

        this.world = world;
        this.bounds = new Rectangle(x, y, width, height);

        this.health = health;
        this.maxHealth = health;
        this.attackDamage = attackDamage;
        this.detectionRange = detectionRange;
        this.attackRange = attackRange;
        this.attackCooldown = attackCooldown;

        spawnPosition.set(x, y);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(
            x + width / 2f,
            y + height / 2f
        );
        bodyDef.fixedRotation = true;
        bodyDef.allowSleep = false;

        body = world.createBody(bodyDef);
        body.setUserData(this);

        PolygonShape mainShape = new PolygonShape();
        mainShape.setAsBox(
            width / 2f,
            height / 2f
        );

        FixtureDef mainFixtureDef = new FixtureDef();
        mainFixtureDef.shape = mainShape;
        mainFixtureDef.density = 1f;
        mainFixtureDef.friction = 0.2f;
        mainFixtureDef.restitution = 0f;

        body.createFixture(mainFixtureDef);
        mainShape.dispose();

        // Foot sensor for actual platform contact.
        PolygonShape footShape = new PolygonShape();
        footShape.setAsBox(
            width * 0.35f,
            1f,
            new Vector2(0f, -height / 2f + 0.5f),
            0f
        );

        FixtureDef footFixtureDef = new FixtureDef();
        footFixtureDef.shape = footShape;
        footFixtureDef.isSensor = true;

        footFixture = body.createFixture(footFixtureDef);
        footFixture.setUserData("enemyFoot");

        footShape.dispose();

        syncBounds();
    }

    public void handleGroundContactBegin(Contact contact) {
        if (isFootSensorContact(contact)
                && isSolidGround(contact)) {
            groundContacts++;
        }
    }

    public void handleGroundContactEnd(Contact contact) {
        if (isFootSensorContact(contact)
                && isSolidGround(contact)) {
            groundContacts = Math.max(
                0,
                groundContacts - 1
            );
        }
    }

    private boolean isFootSensorContact(Contact contact) {
        return contact.getFixtureA() == footFixture
            || contact.getFixtureB() == footFixture;
    }

    private boolean isSolidGround(Contact contact) {
        Fixture other =
            contact.getFixtureA() == footFixture
                ? contact.getFixtureB()
                : contact.getFixtureA();

        if (other.isSensor()) {
            return false;
        }

        Object data = other.getBody().getUserData();

        return !("player".equals(data)
            || data instanceof Enemy);
    }

    public void updateAI(float delta, PlayerState player) {
        if (isDestroyed() || player == null || bodyDisposed) {
            return;
        }

        attackTimer = Math.max(0f, attackTimer - delta);
        jumpCooldown = Math.max(0f, jumpCooldown - delta);

        syncBounds();

        Vector2 position = body.getPosition();

        float dx = player.x - position.x;
        float dy = player.y - position.y;

        float distanceSquared = dx * dx + dy * dy;

        if (distanceSquared
                <= detectionRange * detectionRange) {
            aggro = true;
        }

        Vector2 velocity = body.getLinearVelocity();

        if (!aggro) {
            body.setLinearVelocity(0f, velocity.y);
            return;
        }

        float horizontalDistance = Math.abs(dx);

        if (horizontalDistance > attackRange) {
            float playerSpeed =
                player.movementState
                    == PlayerState.MovementState.RUN
                    ? player.runSpeed
                    : player.walkSpeed;

            float enemySpeed = playerSpeed * 0.8f;

            body.setLinearVelocity(
                Math.signum(dx) * enemySpeed,
                velocity.y
            );
        } else {
            body.setLinearVelocity(0f, velocity.y);
        }

        boolean grounded = groundContacts > 0;

        boolean playerAbove =
            player.y > bounds.y + bounds.height + 5f;

        boolean playerJumping =
            player.movementState
                == PlayerState.MovementState.JUMP;

        if (grounded
                && jumpCooldown <= 0f
                && horizontalDistance
                    < detectionRange * 0.75f
                && (playerAbove || playerJumping)) {

            body.setLinearVelocity(
                body.getLinearVelocity().x,
                PhysicsConfig.JUMP_VELOCITY
            );

            groundContacts = 0;
            jumpCooldown = 1.5f;
        }

        syncBounds();
    }

    private void syncBounds() {
        Vector2 position = body.getPosition();

        bounds.setPosition(
            position.x - bounds.width / 2f,
            position.y - bounds.height / 2f
        );
    }

    public void takeDamage(int amount) {
        health = Math.max(0, health - amount);
    }

    public boolean isDestroyed() {
        return health <= 0 || bodyDisposed;
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

    public Body getBody() {
        return body;
    }

    public boolean canAttack(float playerX, float playerY) {
        if (!aggro || isDestroyed() || attackTimer > 0f) {
            return false;
        }

        Vector2 position = body.getPosition();

        float dx = playerX - position.x;
        float dy = playerY - position.y;

        if (dx * dx + dy * dy
                <= attackRange * attackRange) {
            attackTimer = attackCooldown;
            return true;
        }

        return false;
    }

    public void disposeBody() {
        if (bodyDisposed) {
            return;
        }

        bodyDisposed = true;

        if (!world.isLocked()) {
            world.destroyBody(body);
        }
    }

    public void resetForRespawn() {
        if (bodyDisposed) {
            return;
        }

        health = maxHealth;
        aggro = false;
        attackTimer = 0f;
        jumpCooldown = 0f;
        groundContacts = 0;

        body.setTransform(
            spawnPosition.x + bounds.width / 2f,
            spawnPosition.y + bounds.height / 2f,
            0f
        );

        body.setLinearVelocity(0f, 0f);
        syncBounds();
    }

    public abstract void render(ShapeRenderer shapeRenderer);
}
