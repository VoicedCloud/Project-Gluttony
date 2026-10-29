package com.github.glutton.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;
import com.github.glutton.player.PlayerState;

public class TargetDummy extends Enemy {

    private static final int IDLE_FRAME_COUNT = 5;
    private static final int WALK_FRAME_COUNT = 7;
    private static final int ATTACK_FRAME_COUNT = 4;

    private static final float IDLE_FRAME_TIME = 0.20f;
    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float ATTACK_FRAME_TIME = 0.12f;

    private final Texture idleTexture;
    private final Texture walkTexture;
    private final Texture attackTexture;

    private final TextureRegion[] idleFrames;
    private final TextureRegion[] walkFrames;
    private final TextureRegion[] attackFrames;

    private float animationTimer = 0f;

    private AnimationState animationState = AnimationState.IDLE;

    private boolean facingRight = false;
    private boolean attackAnimationPlaying = false;

    private enum AnimationState {
        IDLE,
        WALK,
        ATTACK
    }

    public TargetDummy(World world, float x, float y) {
        this(
            world,
            x, y,
            32f, 48f,
            1500, 5,
            250f, 40f, 1f
        );
    }

    public TargetDummy(
            World world,
            float x,
            float y,
            float width,
            float height,
            int health,
            int damage,
            float detectionRange,
            float attackRange,
            float attackCooldown
    ) {
        super(
            world,
            x, y,
            width, height,
            health, damage,
            detectionRange,
            attackRange,
            attackCooldown
        );

        String folder = "sprites/enemies/DegoBoss/";

        idleTexture = loadTexture(folder + "Windihstand2.png");
        walkTexture = loadTexture(folder + "walkdego.png");
        attackTexture = loadTexture(folder + "attackdego.png");

        idleFrames = splitStrip(idleTexture, IDLE_FRAME_COUNT);
        walkFrames = splitStrip(walkTexture, WALK_FRAME_COUNT);
        attackFrames = splitStrip(attackTexture, ATTACK_FRAME_COUNT);

        Gdx.app.log("Boss", "Dego boss animations loaded!");
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
        if (texture.getWidth() % frameCount != 0) {
            throw new IllegalArgumentException(
                "Boss sprite sheet width " + texture.getWidth()
                + " is not evenly divisible by " + frameCount
                + ". Check the frame count for this sprite sheet."
            );
        }

        int frameWidth = texture.getWidth() / frameCount;
        int frameHeight = texture.getHeight();

        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(
                texture,
                i * frameWidth,
                0,
                frameWidth,
                frameHeight
            );
        }

        return frames;
    }

    @Override
    public void updateAI(float delta, PlayerState player) {
        super.updateAI(delta, player);

        if (isDestroyed() || isBodyDisposed() || player == null) {
            return;
        }

       // Face the direction the boss is moving.
// Always face toward the player.
float bossCenterX = bounds.x + bounds.width / 2f;
float playerCenterX = player.x + player.width / 2f;

facingRight = playerCenterX >= bossCenterX;

        // Finish the current attack animation before switching states.
        if (attackAnimationPlaying) {
            animationTimer += delta;

            if (animationTimer >= ATTACK_FRAME_TIME
                    * ATTACK_FRAME_COUNT) {
                attackAnimationPlaying = false;
                animationTimer = 0f;
            } else {
                return;
            }
        }

        Vector2 velocity = body.getLinearVelocity();

        if (Math.abs(velocity.x) > 1f) {
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

    public void playAttackAnimation() {
        if (isDestroyed() || isBodyDisposed()) {
            return;
        }

        attackAnimationPlaying = true;
        animationState = AnimationState.ATTACK;
        animationTimer = 0f;
    }

    public void renderSprite(SpriteBatch batch, float delta) {
        if (isDestroyed() || isBodyDisposed()) {
            return;
        }

        if (!attackAnimationPlaying) {
            animationTimer += delta;
        }

        TextureRegion[] frames;
        float frameTime;

        if (attackAnimationPlaying) {
            frames = attackFrames;
            frameTime = ATTACK_FRAME_TIME;
        } else if (animationState == AnimationState.WALK) {
            frames = walkFrames;
            frameTime = WALK_FRAME_TIME;
        } else {
            frames = idleFrames;
            frameTime = IDLE_FRAME_TIME;
        }

        int frameIndex = (int) (animationTimer / frameTime);

        if (attackAnimationPlaying) {
            frameIndex = Math.min(
                frameIndex,
                frames.length - 1
            );
        } else {
            frameIndex %= frames.length;
        }

        TextureRegion frame = frames[frameIndex];

        float drawX = bounds.x;
        float drawY = bounds.y;

        float drawWidth = bounds.width;
        float drawHeight = bounds.height;

        if (facingRight) {
    // Draw normally when facing right.
    batch.draw(
        frame,
        drawX,
        drawY,
        drawWidth,
        drawHeight
    );
} else {
    // Flip horizontally when facing left.
    batch.draw(
        frame,
        drawX + drawWidth,
        drawY,
        -drawWidth,
        drawHeight
    );
}
    }

    @Override
    public void render(ShapeRenderer renderer) {
        if (isDestroyed() || isBodyDisposed()) {
            return;
        }

        // Keep the boss health bar.
        float barY = bounds.y + bounds.height + 4f;

        renderer.setColor(com.badlogic.gdx.graphics.Color.DARK_GRAY);

        renderer.rect(
            bounds.x,
            barY,
            bounds.width,
            4f
        );

        float percentage = maxHealth > 0
            ? (float) health / maxHealth
            : 0f;

        percentage = Math.max(0f, Math.min(1f, percentage));

        renderer.setColor(com.badlogic.gdx.graphics.Color.GREEN);

        renderer.rect(
            bounds.x,
            barY,
            bounds.width * percentage,
            4f
        );
    }

    public void disposeTextures() {
        idleTexture.dispose();
        walkTexture.dispose();
        attackTexture.dispose();
    }
}