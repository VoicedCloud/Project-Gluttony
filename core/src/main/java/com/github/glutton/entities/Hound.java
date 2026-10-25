package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Hound extends Enemy {

    public Hound(
            World world,
            float x, float y,
            float width, float height
    ) {
        super(
            world,
            x, y,
            width, height,
            120, 3, 350f, 45f, 1f
        );

        moveSpeed = 150f;
        jumpVelocity = 250f;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        float w = bounds.width;
        float h = bounds.height * 0.45f;

        float x = bounds.x;
        float y = bounds.y;

        // Orange-red body
        float bodyHeight = h * 0.65f;

        shapeRenderer.setColor(0.95f, 0.30f, 0.10f, 1f);
        shapeRenderer.rect(
            x,
            y,
            w,
            bodyHeight
        );

        // Head at the front
        float headSize = h * 0.80f;

        shapeRenderer.setColor(1f, 0.55f, 0.15f, 1f);
        shapeRenderer.rect(
            x + w - headSize * 0.80f,
            y + bodyHeight,
            headSize,
            headSize
        );

        // Front and rear legs
        float legWidth = w * 0.15f;
        float legHeight = h * 0.35f;

        shapeRenderer.setColor(0.65f, 0.15f, 0.05f, 1f);

        shapeRenderer.rect(
            x + w * 0.15f,
            y,
            legWidth,
            legHeight
        );

        shapeRenderer.rect(
            x + w * 0.70f,
            y,
            legWidth,
            legHeight
        );
    }
}