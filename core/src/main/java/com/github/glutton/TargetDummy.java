
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.physics.box2d.World;

public class TargetDummy extends Enemy {

    public TargetDummy(World world, float x, float y) {
        this(
            world,
            x, y,
            32f, 48f,
            1500, 5,
            250f, 40f, 1f
        );
    }

    public TargetDummy(
            World world,
            float x, float y,
            float width, float height,
            int health,
            int damage,
            float detectionRange,
            float attackRange,
            float attackCooldown
    ) {
        super(
            world,
            x, y,
            width, height,
            health, damage,
            detectionRange,
            attackRange,
            attackCooldown
        );
    }

    @Override
    public void render(ShapeRenderer renderer) {
        if (isDestroyed() || isBodyDisposed()) {
            return;
        }

        renderer.setColor(aggro ? Color.ORANGE : Color.RED);

        renderer.rect(
            bounds.x,
            bounds.y,
            bounds.width,
            bounds.height
        );

        float barY = bounds.y + bounds.height + 4f;

        renderer.setColor(Color.DARK_GRAY);
        renderer.rect(
            bounds.x, barY,
            bounds.width, 4f
        );

        float percentage = maxHealth > 0
            ? (float) health / maxHealth
            : 0f;

        percentage = Math.max(0f, Math.min(1f, percentage));

        renderer.setColor(Color.GREEN);
        renderer.rect(
            bounds.x, barY,
            bounds.width * percentage, 4f
        );
    }
}
