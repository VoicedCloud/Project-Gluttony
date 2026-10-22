
package com.github.glutton.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TargetDummy extends Enemy {

    public TargetDummy(float x, float y) {
        this(x, y, 32f, 48f, 150, 20, 250f, 40f, 1f);
    }

    public TargetDummy(
            float x, float y,
            float width, float height,
            int health,
            int damage,
            float detectionRange,
            float attackRange,
            float attackCooldown
    ) {
        super(
            x, y, width, height,
            health, damage,
            detectionRange, attackRange, attackCooldown
        );
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(aggro ? Color.ORANGE : Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        float barY = bounds.y + bounds.height + 4f;

        shapeRenderer.setColor(Color.DARK_GRAY);
        shapeRenderer.rect(bounds.x, barY, bounds.width, 4f);

        shapeRenderer.setColor(Color.GREEN);
        float fillWidth = (float) health / maxHealth * bounds.width;
        shapeRenderer.rect(bounds.x, barY, fillWidth, 4f);
    }
}
