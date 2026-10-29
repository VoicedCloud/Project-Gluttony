package com.github.glutton.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.github.glutton.player.PlayerState;

public class Hound extends Enemy {

    // Sprite display size
    private static final float SPRITE_WIDTH = 48f;
    private static final float SPRITE_HEIGHT = 32f;

    // Animation speeds
    private static final float IDLE_FRAME_TIME = 0.15f;
    private static final float WALK_FRAME_TIME = 0.10f;
    private static final float RUN_FRAME_TIME = 0.07f;
    private static final float JUMP_FRAME_TIME = 0.10f;

    // Animation frame counts
    private static final int IDLE_FRAME_COUNT = 6;
    private static final int WALK_FRAME_COUNT = 12;
    private static final int RUN_FRAME_COUNT = 5;
    private static final int JUMP_FRAME_COUNT = 6;

    // Sprite textures
    private final Texture idleTexture;
    private final Texture walkTexture;
    private final Texture runTexture;
    private final Texture jumpTexture;

    // Animation frames
    private final TextureRegion[] idleFrames;
    private final TextureRegion[] walkFrames;
    private final TextureRegion[] runFrames;
    private final TextureRegion[] jumpFrames;

    private float animationTimer = 0f;

    private AnimationState animationState = AnimationState.IDLE;

    private boolean facingRight = false;

    private enum AnimationState {
        IDLE,
        WALK,
        RUN,
        JUMP
    }

    public Hound(
            World world,
            float x,
            float y,
            float width,
            float height
    ) {
        super(
            world,
            x,
            y,
            width,
            height,
            120,
            3,
            350f,
            45f,
            1f
        );

        // Keep your original movement settings.
        moveSpeed = 150f;
        jumpVelocity = 250f;

       String folder = "sprites/enemies/PNG/";

        idleTexture = loadTexture(folder + "hell-hound-idle.png");
        walkTexture = loadTexture(folder + "hell-hound-walk.png");
        runTexture = loadTexture(folder + "hell-hound-run.png");
        jumpTexture = loadTexture(folder + "hell-hound-jump.png");

        // Split each sheet using its own frame count.
        idleFrames = splitStrip(idleTexture, IDLE_FRAME_COUNT);
        walkFrames = splitStrip(walkTexture, WALK_FRAME_COUNT);
        runFrames = splitStrip(runTexture, RUN_FRAME_COUNT);
        jumpFrames = splitStrip(jumpTexture, JUMP_FRAME_COUNT);

        Gdx.app.log("Hound", "Hound animations loaded!");
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));

        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        return texture;
    }

    private TextureRegion[] splitStrip(
            Texture texture,
            int frameCount
    ) {
        TextureRegion[] frames = new TextureRegion[frameCount];

        int sheetWidth = texture.getWidth();
        int sheetHeight = texture.getHeight();

        if (sheetWidth < frameCount || sheetHeight <= 0) {
            throw new IllegalArgumentException(
                "Invalid Hound sprite sheet: "
                + texture.getWidth() + "x"
                + texture.getHeight()
            );
        }

        for (int i = 0; i < frameCount; i++) {

            int startX = i * sheetWidth / frameCount;
            int endX = (i + 1) * sheetWidth / frameCount;

            frames[i] = new TextureRegion(
                texture,
                startX,
                0,
                endX - startX,
                sheetHeight
            );
        }

        return frames;
    }

    @Override
public void updateAI(
        float delta,
        PlayerState player
) {
    super.updateAI(delta, player);

    if (isDestroyed() || player == null) {
        return;
    }

    Vector2 velocity = body.getLinearVelocity();

    // Update facing direction only when the player is
    // sufficiently far away horizontally.
    float horizontalDistance = player.x - bounds.x;

    if (Math.abs(horizontalDistance) > 2f) {
        facingRight = horizontalDistance > 0f;
    }

    // Prioritize jumping when the Hound is airborne.
    if (Math.abs(velocity.y) > 10f) {
        setAnimationState(AnimationState.JUMP);
        return;
    }

    // Use a small threshold to avoid switching between
    // idle and walking due to tiny velocity changes.
    float horizontalSpeed = Math.abs(velocity.x);

    if (horizontalSpeed > moveSpeed * 0.75f) {
        setAnimationState(AnimationState.RUN);
    } else if (horizontalSpeed > 5f) {
        setAnimationState(AnimationState.WALK);
    } else {
        setAnimationState(AnimationState.IDLE);
    }
}

    private void setAnimationState(AnimationState newState) {
        if (animationState != newState) {
            animationState = newState;
            animationTimer = 0f;
        }
    }

    public void renderSprite(SpriteBatch batch, float delta) {

        animationTimer += delta;

        TextureRegion[] frames;
        float frameTime;

        switch (animationState) {

            case WALK:
                frames = walkFrames;
                frameTime = WALK_FRAME_TIME;
                break;

            case RUN:
                frames = runFrames;
                frameTime = RUN_FRAME_TIME;
                break;

            case JUMP:
                frames = jumpFrames;
                frameTime = JUMP_FRAME_TIME;
                break;

            case IDLE:
            default:
                frames = idleFrames;
                frameTime = IDLE_FRAME_TIME;
                break;
        }

        int frameIndex =
            (int) (animationTimer / frameTime) % frames.length;

        TextureRegion frame = frames[frameIndex];

        // Center the sprite above the enemy's world bounds.
        float drawX =
            bounds.x + (bounds.width - SPRITE_WIDTH) / 2f;

        float drawY = bounds.y;

        // The original sprite faces left.
        // Flip it horizontally when facing right.
        if (facingRight) {
            batch.draw(
                frame,
                drawX + SPRITE_WIDTH,
                drawY,
                -SPRITE_WIDTH,
                SPRITE_HEIGHT
            );
        } else {
            batch.draw(
                frame,
                drawX,
                drawY,
                SPRITE_WIDTH,
                SPRITE_HEIGHT
            );
        }
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // The Hound is drawn using its animated sprites.
    }

    public void disposeTextures() {
        idleTexture.dispose();
        walkTexture.dispose();
        runTexture.dispose();
        jumpTexture.dispose();
    }
}