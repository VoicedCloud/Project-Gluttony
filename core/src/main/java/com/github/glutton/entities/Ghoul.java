package com.github.glutton.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Vector2;
import com.github.glutton.player.PlayerState;

public class Ghoul extends Enemy {

    private static final int FRAME_WIDTH = 32;
    private static final int FRAME_HEIGHT = 32;

    private static final int IDLE_ROW = 0;
    private static final int WALK_ROW = 1;
    private static final int ATTACK_ROW = 2;

    private static final int IDLE_FRAMES = 4;
    private static final int WALK_FRAMES = 8;
    private static final int ATTACK_FRAMES = 8;

    private static final float IDLE_FRAME_TIME = 0.20f;
    private static final float WALK_FRAME_TIME = 0.12f;
    private static final float ATTACK_FRAME_TIME = 0.12f;

    private final Texture spriteSheet;

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

    public Ghoul(
            World world,
            float x,
            float y,
            float width,
            float height
    ) {
        super(
            world,
            x, y,
            width, height,
            70, 5, 300f, 45f, 1f
        );

        moveSpeed = 100f;
        jumpVelocity = 300f;

        spriteSheet = new Texture(
            Gdx.files.internal(
                "sprites/enemies/Ghoul/ghoul.png"
            )
        );

        spriteSheet.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        idleFrames = loadFrames(IDLE_ROW, IDLE_FRAMES);
        walkFrames = loadFrames(WALK_ROW, WALK_FRAMES);
        attackFrames = loadFrames(ATTACK_ROW, ATTACK_FRAMES);

        Gdx.app.log("Ghoul", "Ghoul sprite sheet loaded!");
    }

    private TextureRegion[] loadFrames(
            int row,
            int frameCount
    ) {
        TextureRegion[] frames = new TextureRegion[frameCount];

        for (int i = 0; i < frameCount; i++) {
            frames[i] = new TextureRegion(
                spriteSheet,
                i * FRAME_WIDTH,
                row * FRAME_HEIGHT,
                FRAME_WIDTH,
                FRAME_HEIGHT
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

        // Face the direction the ghoul is moving.
        float velocityX = body.getLinearVelocity().x;

        if (velocityX > 1f) {
            facingRight = true;
        } else if (velocityX < -1f) {
            facingRight = false;
        }

        // Finish the current attack animation.
        if (attackAnimationPlaying) {
            animationTimer += delta;

            if (animationTimer
                    >= ATTACK_FRAME_TIME * ATTACK_FRAMES) {

                attackAnimationPlaying = false;
                animationTimer = 0f;
            } else {
                return;
            }
        }

        // Choose the correct animation.
        if (Math.abs(velocityX) > 1f) {
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

    public void triggerAttackAnimation() {
        if (isDestroyed() || isBodyDisposed()) {
            return;
        }

        attackAnimationPlaying = true;
        animationState = AnimationState.ATTACK;
        animationTimer = 0f;
    }

    public void renderSprite(
            SpriteBatch batch,
            float delta
    ) {
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

        // Flip the sprite when facing left.
        // This assumes the original sprite faces right.
        if (facingRight) {
            batch.draw(
                frame,
                drawX,
                drawY,
                drawWidth,
                drawHeight
            );
        } else {
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
    public void render(ShapeRenderer shapeRenderer) {
        // The ghoul is drawn using its sprite sheet.
    }

    public void disposeTextures() {
        spriteSheet.dispose();
    }
}   