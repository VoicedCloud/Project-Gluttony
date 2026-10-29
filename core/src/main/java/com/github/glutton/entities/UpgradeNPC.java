package com.github.glutton.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;

public class UpgradeNPC {

    private static final int FRAME_COUNT = 7;
    private static final float FRAME_DURATION = 0.15f;

    private static final float SPRITE_WIDTH = 64f;
    private static final float SPRITE_HEIGHT = 64f;

    private final Rectangle bounds = new Rectangle();

    private final Texture idleTexture;
    private final TextureRegion[] idleFrames;

    private float animationTimer = 0f;

    private boolean active = true;

    public UpgradeNPC(
            float x,
            float y,
            float playerWidth,
            float playerHeight
    ) {
        // Keep the NPC's existing position and collision dimensions.
        bounds.set(
            x - playerWidth / 2f,
            y - playerHeight / 2f,
            playerWidth,
            playerHeight
        );

        // Load the existing idle sprite sheet.
        idleTexture = new Texture(
    Gdx.files.internal("sprites/enemies/Idle.png")
);

        idleTexture.setFilter(
            Texture.TextureFilter.Nearest,
            Texture.TextureFilter.Nearest
        );

        // Split the sprite sheet into 7 frames.
        idleFrames = new TextureRegion[FRAME_COUNT];

        int frameWidth = idleTexture.getWidth() / FRAME_COUNT;
        int frameHeight = idleTexture.getHeight();

        for (int i = 0; i < FRAME_COUNT; i++) {
            idleFrames[i] = new TextureRegion(
                idleTexture,
                i * frameWidth,
                0,
                frameWidth,
                frameHeight
            );
        }
    }

    public void render(SpriteBatch batch, float delta) {
        if (!active) {
            return;
        }

        // Advance the idle animation.
        animationTimer += Math.min(delta, 0.1f);

        int frameIndex = (int) (animationTimer / FRAME_DURATION)
                % FRAME_COUNT;

        TextureRegion currentFrame = idleFrames[frameIndex];

        // Draw the animated NPC at its existing position.
        float drawX = bounds.x + (bounds.width - SPRITE_WIDTH) / 2f;
float drawY = bounds.y + bounds.height / 2f - 8f;
        batch.draw(
            currentFrame,
            drawX,
            drawY,
            SPRITE_WIDTH,
            SPRITE_HEIGHT
        );
    }

    public boolean isNear(float playerX, float playerY) {
        if (!active) {
            return false;
        }

        float centerX = bounds.x + bounds.width / 2f;
        float centerY = bounds.y + bounds.height / 2f;

        float dx = playerX - centerX;
        float dy = playerY - centerY;

        return dx * dx + dy * dy <= 60f * 60f;
    }

    public boolean isActive() {
        return active;
    }

    public void remove() {
        active = false;
    }

    public float getX() {
        return bounds.x + bounds.width / 2f;
    }

    public float getY() {
        return bounds.y + bounds.height / 2f;
    }

    public void dispose() {
        idleTexture.dispose();
    }
}