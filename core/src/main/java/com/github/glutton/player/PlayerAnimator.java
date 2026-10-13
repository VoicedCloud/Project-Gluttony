package com.github.glutton.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerAnimator {

    private final Animation<TextureRegion> idleAnimation;
    private final Animation<TextureRegion> walkAnimation;
    private final Animation<TextureRegion> runAnimation;

    private final Texture idleTexture;
    private final Texture walkTexture;
    private final Texture runTexture;

    private float stateTime = 0f;
    private PlayerState.MovementState lastState = null;

    public PlayerAnimator() {
        idleTexture = new Texture(Gdx.files.internal("sprites/player/Idle.png"));
        idleAnimation = buildAnimation(idleTexture, 1f / 4f);

        walkTexture = new Texture(Gdx.files.internal("sprites/player/Walk.png"));
        walkAnimation = buildAnimation(walkTexture, 1f / 10f);

        runTexture = new Texture(Gdx.files.internal("sprites/player/Run.png"));
        runAnimation = buildAnimation(runTexture, 1f / 10f);
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
            stateTime = 0f; // restart animation cleanly on state change
            lastState = player.movementState;
        }

        Animation<TextureRegion> active;
        switch (player.movementState) {
            case RUN:
                active = runAnimation;
                break;
            case WALK:
                active = walkAnimation;
                break;
            default:
                active = idleAnimation;
                break;
        }

        TextureRegion frame = active.getKeyFrame(stateTime);

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
    }
}
