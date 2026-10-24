
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Hound extends Enemy {

    public Hound(
            World world,
            float x, float y,
            float width, float height
    ) {
        super(world, x, y, width, height,
              120, 3, 350f, 45f, 1f);

        moveSpeed = 150f;
        jumpVelocity = 250f;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        float w = bounds.width * 0.90f;
        float h = bounds.height * 0.40f;

        float x = bounds.x + (bounds.width - w) / 2f;
        float y = bounds.y;

        // Orange-red body
        shapeRenderer.setColor(0.95f, 0.30f, 0.10f, 1f);
        shapeRenderer.rect(x, y, w, h);

        // Head at the front (right side)
        float headSize = h * 0.85f;
        shapeRenderer.setColor(1f, 0.55f, 0.15f, 1f);
        shapeRenderer.rect(
            x + w - headSize * 0.65f,
            y + h * 0.45f,
            headSize,
            headSize
        );
    }
}
