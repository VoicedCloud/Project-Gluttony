package com.github.glutton.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.math.Vector2;
import java.util.Random;

public class Zombie extends Enemy {

    private static final int FRAME_SIZE = 128;

    private static final float IDLE_FRAME_TIME = 0.15f;
    private static final float WALK_FRAME_TIME = 0.10f;
    private static final float ATTACK_FRAME_TIME = 0.10f;
    private static final float HURT_FRAME_TIME = 0.10f;
    private static final float DEAD_FRAME_TIME = 0.14f;

    private static final String[] ZOMBIE_FOLDERS = {
        "sprites/enemies/Zombie_1/",
        "sprites/enemies/Zombie_2/",
        "sprites/enemies/Zombie_3/",
        "sprites/enemies/Zombie_4/"
    };

    private static final Random RANDOM = new Random();

    private final Texture idleTexture;
    private final Texture walkTexture;
    private final Texture attackTexture;
    private final Texture hurtTexture;
    private final Texture deadTexture;

    private final TextureRegion[][] idleFrames;
    private final TextureRegion[][] walkFrames;
    private final TextureRegion[][] attackFrames;
    private final TextureRegion[][] hurtFrames;
    private final TextureRegion[][] deadFrames;

    private float animationTimer = 0f;
    private float stateTimer = 0f;

    private AnimationState animationState = AnimationState.IDLE;

    private boolean facingRight = true;
    private boolean deathAnimationFinished = false;

    private enum AnimationState {
        IDLE,
        WALK,
        ATTACK,
        HURT,
        DEAD
    }

    public Zombie(
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
            100, 2, 250f, 40f, 1f
        );

        moveSpeed = 50f;
        jumpVelocity = 200f;

        String folder = ZOMBIE_FOLDERS[
            RANDOM.nextInt(ZOMBIE_FOLDERS.length)
        ];

        idleTexture = loadTexture(folder + "Idle.png");
        walkTexture = loadTexture(folder + "Walk.png");
        attackTexture = loadTexture(folder + "Attack.png");
        hurtTexture = loadTexture(folder + "Hurt.png");
        deadTexture = loadTexture(folder + "Dead.png");

        idleFrames = split(idleTexture);
        walkFrames = split(walkTexture);
        attackFrames = split(attackTexture);
        hurtFrames = split(hurtTexture);
        deadFrames = split(deadTexture);

        Gdx.app.log("Zombie", "Loaded variant: " + folder);
    }

    private Texture loadTexture(String path) {
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );
        return texture;
    }

    private TextureRegion[][] split(Texture texture) {
        if (texture.getHeight() != FRAME_SIZE
                || texture.getWidth() % FRAME_SIZE != 0) {
            throw new IllegalArgumentException(
                "Invalid zombie sprite sheet: "
                    + texture.getWidth() + "x"
                    + texture.getHeight()
            );
        }

        return TextureRegion.split(
            texture,
            FRAME_SIZE,
            FRAME_SIZE
        );
    }

    @Override
    public void updateAI(
            float delta,
            com.github.glutton.player.PlayerState player
    ) {
        super.updateAI(delta, player);

        if (isDestroyed()) {
            setAnimationState(AnimationState.DEAD);
            return;
        }

        if (player == null) {
            setAnimationState(AnimationState.IDLE);
            return;
        }

        Vector2 velocity = body.getLinearVelocity();

        if (Math.abs(player.x - bounds.x) > 0.01f) {
            facingRight = player.x > bounds.x;
        }

        if (animationState == AnimationState.HURT
                && stateTimer > 0f) {
            stateTimer -= delta;

            if (stateTimer <= 0f) {
                chooseMovementAnimation(velocity);
            }
            return;
        }

        if (animationState == AnimationState.ATTACK
                && stateTimer > 0f) {
            stateTimer -= delta;

            if (stateTimer <= 0f) {
                chooseMovementAnimation(velocity);
            }
            return;
        }

        chooseMovementAnimation(velocity);
    }

    private void chooseMovementAnimation(Vector2 velocity) {
        if (animationState == AnimationState.DEAD) {
            return;
        }

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

    public void triggerAttackAnimation() {
        if (!isDestroyed()) {
            setAnimationState(AnimationState.ATTACK);
            stateTimer = 0.35f;
        }
    }

    @Override
    public void takeDamage(int amount) {
        if (isDestroyed() || amount <= 0) {
            return;
        }

        super.takeDamage(amount);

        if (isDestroyed()) {
            setAnimationState(AnimationState.DEAD);
        } else {
            setAnimationState(AnimationState.HURT);
            stateTimer = 0.25f;
        }
    }

    public void renderSprite(SpriteBatch batch, float delta) {
        animationTimer += delta;

        TextureRegion[][] frames;
        float frameTime;
        boolean loop = true;

        switch (animationState) {
            case WALK:
                frames = walkFrames;
                frameTime = WALK_FRAME_TIME;
                break;

            case ATTACK:
                frames = attackFrames;
                frameTime = ATTACK_FRAME_TIME;
                loop = false;
                break;

            case HURT:
                frames = hurtFrames;
                frameTime = HURT_FRAME_TIME;
                loop = false;
                break;

            case DEAD:
                frames = deadFrames;
                frameTime = DEAD_FRAME_TIME;
                loop = false;
                break;

            case IDLE:
            default:
                frames = idleFrames;
                frameTime = IDLE_FRAME_TIME;
                break;
        }

        int frameCount = frames[0].length;
        int frame = (int) (animationTimer / frameTime);

        if (loop) {
            frame %= frameCount;
        } else {
            frame = Math.min(frame, frameCount - 1);

            if (animationState == AnimationState.DEAD
                    && frame == frameCount - 1) {
                deathAnimationFinished = true;
            }
        }

        TextureRegion region = frames[0][frame];

        float drawWidth = bounds.width;
        float drawHeight = bounds.height;

        float drawX = bounds.x;
        float drawY = bounds.y;

        batch.draw(
            region,
            facingRight ? drawX : drawX + drawWidth,
            drawY,
            facingRight ? drawWidth : -drawWidth,
            drawHeight
        );
    }

    public boolean isDeathAnimationFinished() {
        return deathAnimationFinished;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        // The zombie is rendered with its sprite sheet instead.
    }

    public void disposeTextures() {
        idleTexture.dispose();
        walkTexture.dispose();
        attackTexture.dispose();
        hurtTexture.dispose();
        deadTexture.dispose();
    }
}