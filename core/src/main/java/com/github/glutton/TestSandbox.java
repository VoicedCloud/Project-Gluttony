
package com.github.glutton;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2D;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.ScreenUtils;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

import com.github.glutton.entities.Enemy;
import com.github.glutton.entities.TargetDummy;
import com.github.glutton.player.PlayerState;

public class TestSandbox extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    private SpriteBatch batch;
    private BitmapFont font;

    private World world;
    private TargetDummy targetDummy;

    private final Rectangle player =
        new Rectangle(100f, 70f, 24f, 48f);

    private final Rectangle floor =
        new Rectangle(0f, 50f, 800f, 20f);

    private final PlayerState playerState =
        new PlayerState();

    private float playerVelocityY = 0f;
    private float gravity = -1000f;
    private float jumpVelocity = 450f;
    private float playerSpeed = 250f;

    private boolean isGrounded = true;

    @Override
    public void create() {

        Box2D.init();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800f, 600f);

        shapeRenderer = new ShapeRenderer();
        batch = new SpriteBatch();
        font = new BitmapFont();

        world = new World(
            new Vector2(0f, gravity),
            true
        );

        createFloor();

        // Enemy starts on the floor, 150 units to the right
        // of the player's starting position.
        targetDummy = new TargetDummy(
            world,
            player.x + 150f,
            floor.y + floor.height,
            32f,
            48f,
            1500,
            7,
            250f,
            40f,
            1f
        );

        // Preserve enemy ground-contact detection.
        world.setContactListener(new ContactListener() {

            @Override
            public void beginContact(Contact contact) {
                targetDummy.handleGroundContactBegin(contact);
            }

            @Override
            public void endContact(Contact contact) {
                targetDummy.handleGroundContactEnd(contact);
            }

            @Override
            public void preSolve(
                    Contact contact,
                    Manifold oldManifold) {
            }

            @Override
            public void postSolve(
                    Contact contact,
                    ContactImpulse impulse) {
            }
        });

        updatePlayerState();
    }

    private void createFloor() {

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(
            floor.x + floor.width / 2f,
            floor.y + floor.height / 2f
        );

        Body floorBody = world.createBody(bodyDef);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(
            floor.width / 2f,
            floor.height / 2f
        );

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.friction = 0.2f;

        floorBody.createFixture(fixtureDef);
        shape.dispose();
    }

    @Override
    public void render() {

        float delta = Math.min(
            Gdx.graphics.getDeltaTime(),
            0.05f
        );

        ScreenUtils.clear(0.08f, 0.08f, 0.12f, 1f);

        handlePlayerMovement(delta);
        updatePlayerState();

        // Move the enemy toward the player.
        targetDummy.updateAI(delta, playerState);

        // Step Box2D after updating movement commands.
        int substeps = 8;
        float subDelta = delta / substeps;

        for (int i = 0; i < substeps; i++) {
            world.step(subDelta, 6, 2);
        }

        camera.update();

        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Floor.
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(
            floor.x,
            floor.y,
            floor.width,
            floor.height
        );

        // Player.
        shapeRenderer.setColor(Color.BLUE);
        shapeRenderer.rect(
            player.x,
            player.y,
            player.width,
            player.height
        );

        // Enemy and its health bar.
        targetDummy.render(shapeRenderer);

        // Player HP bar.
        drawPlayerHealthBar();

        shapeRenderer.end();

        drawPlayerHealthText();
    }

    private void handlePlayerMovement(float delta) {

        float moveX = 0f;

        if (Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            moveX -= playerSpeed;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            moveX += playerSpeed;
        }

        player.x += moveX * delta;

        // Keep the player inside the test area.
        player.x = Math.max(
            0f,
            Math.min(
                800f - player.width,
                player.x
            )
        );

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)
                && isGrounded) {
            playerVelocityY = jumpVelocity;
            isGrounded = false;
        }

        playerVelocityY += gravity * delta;
        player.y += playerVelocityY * delta;

        // Simple player-floor collision for this test sandbox.
        float floorTop = floor.y + floor.height;

        if (player.y <= floorTop) {
            player.y = floorTop;
            playerVelocityY = 0f;
            isGrounded = true;
        } else {
            isGrounded = false;
        }
    }

    private void updatePlayerState() {

        // PlayerState coordinates represent the player's center.
        playerState.x = player.x + player.width / 2f;
        playerState.y = player.y + player.height / 2f;

        playerState.isGrounded = isGrounded;

        if (!isGrounded) {
            playerState.movementState =
                playerVelocityY > 0f
                    ? PlayerState.MovementState.JUMP
                    : PlayerState.MovementState.FALL;
        } else {
            boolean moving =
                Gdx.input.isKeyPressed(Input.Keys.A)
                || Gdx.input.isKeyPressed(Input.Keys.D)
                || Gdx.input.isKeyPressed(Input.Keys.LEFT)
                || Gdx.input.isKeyPressed(Input.Keys.RIGHT);

            playerState.movementState = moving
                ? PlayerState.MovementState.RUN
                : PlayerState.MovementState.IDLE;
        }
    }

    private void drawPlayerHealthBar() {

        float x = camera.position.x
            - camera.viewportWidth / 2f + 20f;

        float y = camera.position.y
            + camera.viewportHeight / 2f - 45f;

        float width = 150f;
        float height = 12f;

        float percentage =
            (float) playerState.health / playerState.maxHealth;

        percentage = Math.max(
            0f,
            Math.min(1f, percentage)
        );

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(x, y, width, height);

        if (percentage > 0.5f) {
            shapeRenderer.setColor(Color.GREEN);
        } else if (percentage > 0.25f) {
            shapeRenderer.setColor(Color.ORANGE);
        } else {
            shapeRenderer.setColor(Color.RED);
        }

        shapeRenderer.rect(
            x,
            y,
            width * percentage,
            height
        );
    }

    private void drawPlayerHealthText() {

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        float x = camera.position.x
            - camera.viewportWidth / 2f + 20f;

        float y = camera.position.y
            + camera.viewportHeight / 2f - 20f;

        font.setColor(Color.WHITE);

        font.draw(
            batch,
            "HP: " + playerState.health
                + " / " + playerState.maxHealth,
            x,
            y
        );

        font.draw(
            batch,
            "A/D: Move    SPACE: Jump",
            x,
            y - 25f
        );

        batch.end();
    }

    @Override
    public void dispose() {

        if (targetDummy != null) {
            targetDummy.disposeBody();
        }

        if (world != null) {
            world.dispose();
        }

        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }

        if (batch != null) {
            batch.dispose();
        }

        if (font != null) {
            font.dispose();
        }
    }
}
