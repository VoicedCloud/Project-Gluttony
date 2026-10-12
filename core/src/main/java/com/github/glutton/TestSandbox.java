package com.github.glutton;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.ScreenUtils;

public class TestSandbox extends ApplicationAdapter {

    private OrthographicCamera camera;
    private ShapeRenderer shapeRenderer;
    
    // Primitive hitboxes for standalone sandbox testing
    private Rectangle player;
    private Rectangle targetDummy;
    private Rectangle floor;

    // Dummy state tracking
    private int dummyHealth = 100;
    private boolean isDummyDestroyed = false;

    // Sandbox Physics Variables
    private float velocityY = 0;
    private final float gravity = -1000f;
    private final float jumpVelocity = 450f;
    private final float moveSpeed = 250f;
    private boolean isGrounded = false;

    @Override
    public void create() {
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 600);
        shapeRenderer = new ShapeRenderer();

        // Spawn player hitbox (width: 24, height: 48)
        player = new Rectangle(100, 150, 24, 48);

        // Spawn dummy hitbox
        targetDummy = new Rectangle(500, 70, 32, 64);

        // Ground floor platform
        floor = new Rectangle(50, 50, 700, 20);
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // --- HORIZONTAL MOVEMENT ---
        float nextX = player.x;
        if (Gdx.input.isKeyPressed(Input.Keys.A) || Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            nextX -= moveSpeed * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D) || Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            nextX += moveSpeed * delta;
        }

        // --- JUMPING & GRAVITY ---
        if ((Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.W)) && isGrounded) {
            velocityY = jumpVelocity;
            isGrounded = false;
        }

        velocityY += gravity * delta;
        float nextY = player.y + velocityY * delta;

        // Apply new position
        player.setPosition(nextX, nextY);

        // --- SIMPLE FLOOR COLLISION ---
        if (player.overlaps(floor)) {
            player.setPosition(nextX, floor.y + floor.height);
            velocityY = 0;
            isGrounded = true;
        }

        // --- DUMMY HIT TEST (Press 'F' to attack) ---
        if (Gdx.input.isKeyJustPressed(Input.Keys.F) && !isDummyDestroyed) {
            if (player.overlaps(targetDummy)) {
                dummyHealth -= 10;
                if (dummyHealth <= 0) {
                    isDummyDestroyed = true;
                }
            }
        }

        // --- RENDER SCREEN ---
        ScreenUtils.clear(0.12f, 0.12f, 0.15f, 1f);

        camera.update();
        shapeRenderer.setProjectionMatrix(camera.combined);

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Draw Floor
        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(floor.x, floor.y, floor.width, floor.height);

        // Draw Player (Cyan Box)
        shapeRenderer.setColor(Color.CYAN);
        shapeRenderer.rect(player.x, player.y, player.width, player.height);

        // Draw Target Dummy (Red Box)
        if (!isDummyDestroyed) {
            shapeRenderer.setColor(Color.RED);
            shapeRenderer.rect(targetDummy.x, targetDummy.y, targetDummy.width, targetDummy.height);
        }

        shapeRenderer.end();
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}