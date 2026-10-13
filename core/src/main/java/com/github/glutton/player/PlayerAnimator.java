package com.github.glutton.player;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerAnimator {

	private final Animation<TextureRegion> idleAnimation;
	private final Texture texture;
	private float stateTime = 0f;

	public PlayerAnimator() {
		texture = new Texture(com.badlogic.gdx.Gdx.files.internal("sprites/player/Idle.png"));
		TextureRegion[][] tmp = TextureRegion.split(texture, 128, 128);
		TextureRegion[] frames = tmp[0]; // single row, 6 frames
		idleAnimation = new Animation<>(1f / 4f, frames); // 4fps
		idleAnimation.setPlayMode(Animation.PlayMode.LOOP);
	}

	public void update(float delta) {
		stateTime += delta;
	}

	public void draw(SpriteBatch batch, PlayerState player) {
		TextureRegion frame = idleAnimation.getKeyFrame(stateTime);

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
		texture.dispose();
	}

}
