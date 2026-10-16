package com.github.glutton.player;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerAnimator {

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> runAnimation;

    private final TextureRegion[] jumpFrames;
    private final TextureRegion[] fallFrames;

    private final Texture idleTexture;
    private final Texture walkTexture;
    private final Texture runTexture;
    private final Texture jumpTexture;
    private final Texture fallTexture;

    private float stateTime = 0f;
    private PlayerState.MovementState lastState = null;

    public PlayerAnimator() {
        idleTexture = new Texture(Gdx.files.internal("sprites/player/Idle.png"));
        idleAnimation = buildAnimation(idleTexture, 1f / 4f);

        walkTexture = new Texture(Gdx.files.internal("sprites/player/Walk.png"));
        walkAnimation = buildAnimation(walkTexture, 1f / 10f);

        runTexture = new Texture(Gdx.files.internal("sprites/player/Run.png"));
        runAnimation = buildAnimation(runTexture, 1f / 10f);

        // Load 4-frame 512x128 jump and fall sheets (128x128 per frame)
        jumpTexture = new Texture(Gdx.files.internal("sprites/player/JumpW.png"));
        jumpFrames = TextureRegion.split(jumpTexture, 128, 128)[0];

        fallTexture = new Texture(Gdx.files.internal("sprites/player/Attack.png"));
        fallFrames = TextureRegion.split(fallTexture, 128, 128)[0];
    }

    private Animation<TextureRegion> buildAnimation(Texture texture, float frameDuration) {
        TextureRegion[][] tmp = TextureRegion.split(texture, 128, 128);
        TextureRegion[] frames = tmp[0];
        Animation<TextureRegion> anim = new Animation<>(frameDuration, frames);
        anim.setPlayMode(Animation.PlayMode.LOOP);
        return anim;
    }

    public void update(float delta) {
        stateTime += delta;
    }

    public void draw(SpriteBatch batch, PlayerState player) {
        if (player.movementState != lastState) {
            stateTime = 0f;
            lastState = player.movementState;
        }

        TextureRegion frame;
        switch (player.movementState) {
            case RUN:
                frame = runAnimation.getKeyFrame(stateTime);
                break;
            case WALK:
                frame = walkAnimation.getKeyFrame(stateTime);
                break;
            case JUMP:
                // Map progress from jumpStartY to maxJumpHeight (0.0 to 1.0)
                float jumpProgress = 0f;
                if (player.maxJumpHeight > 0f) {
                    jumpProgress = Math.min(1f, Math.max(0f, (player.y - player.jumpStartY) / player.maxJumpHeight));
                }
                int jumpIndex = Math.min(3, (int) (jumpProgress * 4f));
                frame = jumpFrames[jumpIndex];
                break;
            case FALL:
                // Map progress from peakY downwards based on fall distance
                float fallDistance = player.peakY - player.y;
                float fallProgress = 0f;
                if (player.maxJumpHeight > 0f) {
                    fallProgress = Math.min(1f, Math.max(0f, fallDistance / player.maxJumpHeight));
                }
                int fallIndex = Math.min(3, (int) (fallProgress * 4f));
                frame = fallFrames[fallIndex];
                break;
            default:
                frame = idleAnimation.getKeyFrame(stateTime);
                break;
        }

        float hitboxLeft = player.x - player.width / 2f;
        float hitboxBottom = player.y - player.height / 2f;

        float drawX = hitboxLeft - player.spriteOffsetX;
        float drawY = hitboxBottom - player.spriteOffsetY;

        boolean flip = !player.facingRight;
        if (flip != frame.isFlipX()) {
            frame.flip(true, false);
        }

        batch.draw(frame, drawX, drawY, player.spriteFrameSize, player.spriteFrameSize);
    }

    public void dispose() {
        idleTexture.dispose();
        walkTexture.dispose();
        runTexture.dispose();
        jumpTexture.dispose();
        fallTexture.dispose();
    }
}