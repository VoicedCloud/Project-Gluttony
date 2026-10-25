package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Zombie extends Enemy {

    public Zombie(
            World world,
            float x, float y,
            float width, float height
    ) {
        super(
            world,
            x, y,
            width, height,
            100, 2, 250f, 40f, 1f
        );

        moveSpeed = 50f;
        jumpVelocity = 200f;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        float w = bounds.width;
        float h = bounds.height;

        float x = bounds.x;
        float y = bounds.y;

        // Green body
        float bodyHeight = h * 0.70f;

        shapeRenderer.setColor(0.20f, 0.75f, 0.25f, 1f);
        shapeRenderer.rect(
            x + w * 0.20f,
            y,
            w * 0.60f,
            bodyHeight
        );

        // Dark green head
        float headWidth = w * 0.70f;
        float headHeight = h - bodyHeight;

        shapeRenderer.setColor(0.08f, 0.42f, 0.12f, 1f);
        shapeRenderer.rect(
            x + (w - headWidth) / 2f,
            y + bodyHeight,
            headWidth,
            headHeight
        );
    }
}