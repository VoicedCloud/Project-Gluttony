
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Zombie extends Enemy {

    public Zombie(
            World world,
            float x, float y,
            float width, float height
    ) {
        super(world, x, y, width, height,
              100, 2, 250f, 40f, 1f);

        moveSpeed = 50f;
        jumpVelocity = 200f;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        float w = bounds.width * 0.65f;
        float h = bounds.height * 0.75f;

        float x = bounds.x + (bounds.width - w) / 2f;
        float y = bounds.y;

        // Green body
        shapeRenderer.setColor(0.20f, 0.75f, 0.25f, 1f);
        shapeRenderer.rect(x, y, w, h);

        // Dark green head
        float headSize = w * 0.65f;
        shapeRenderer.setColor(0.08f, 0.42f, 0.12f, 1f);
        shapeRenderer.rect(
            x + (w - headSize) / 2f,
            y + h,
            headSize,
            headSize
        );
    }
}
