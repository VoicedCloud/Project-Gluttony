package com.github.glutton.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class UpgradeNPC {

    private final Rectangle bounds = new Rectangle();

    private boolean active = true;

    public UpgradeNPC(
            float x,
            float y,
            float playerWidth,
            float playerHeight
    ) {
        // NPC uses the same dimensions as the player.
        bounds.set(
            x - playerWidth / 2f,
            y - playerHeight / 2f,
            playerWidth,
            playerHeight
        );
    }

    public void render(ShapeRenderer renderer) {
        if (!active) {
            return;
        }

        renderer.setColor(Color.CYAN);

        renderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        );

        // Simple visual marker above the NPC.
        renderer.setColor(Color.WHITE);

        renderer.rect(
            bounds.x + bounds.width * 0.25f,
            bounds.y + bounds.height,
            bounds.width * 0.5f,
            3f
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
}