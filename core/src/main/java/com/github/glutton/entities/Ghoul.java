package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Ghoul extends Enemy {

    public Ghoul(
            World world,
            float x, float y,
            float width, float height
    ) {
        super(
            world,
            x, y,
            width, height,
            70, 5, 300f, 45f, 1f
        );

        moveSpeed = 100f;
        jumpVelocity = 300f;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        float w = bounds.width;
       float h = bounds.height;

        float x = bounds.x;
        float y = bounds.y;

        // Purple body
        float bodyHeight = h * 0.70f;

        shapeRenderer.setColor(0.65f, 0.25f, 0.90f, 1f);
        shapeRenderer.rect(
            x + w * 0.15f,
            y,
            w * 0.70f,
            bodyHeight
        );

        // Light purple head
        float headWidth = w * 0.70f;
        float headHeight = h - bodyHeight;

        shapeRenderer.setColor(0.85f, 0.60f, 1f, 1f);
        shapeRenderer.rect(
            x + (w - headWidth) / 2f,
            y + bodyHeight,
            headWidth,
            headHeight
        );
    }
}