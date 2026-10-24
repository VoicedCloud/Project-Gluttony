
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class Ghoul extends Enemy {

    public Ghoul(
            World world,
            float x, float y,
            float width, float height
    ) {
        super(world, x, y, width, height,
              70, 5, 300f, 45f, 1f);

        moveSpeed = 100f;
        jumpVelocity = 300f;
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        float w = bounds.width * 0.55f;
        float h = bounds.height * 0.65f;

        float x = bounds.x + (bounds.width - w) / 2f;
        float y = bounds.y;

        // Purple body
        shapeRenderer.setColor(0.65f, 0.25f, 0.90f, 1f);
        shapeRenderer.rect(x, y, w, h);

        // Light purple head
        float headSize = w * 0.75f;
        shapeRenderer.setColor(0.85f, 0.60f, 1f, 1f);
        shapeRenderer.rect(
            x + (w - headSize) / 2f,
            y + h,
            headSize,
            headSize
        );
    }
}
