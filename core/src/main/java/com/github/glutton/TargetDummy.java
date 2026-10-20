package com.github.glutton.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

public class TargetDummy extends Enemy {

    public TargetDummy(float x, float y) {
        super(x, y, 30f, 50f, 50);
    }

    @Override
    public void render(ShapeRenderer shapeRenderer) {
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);

        if (health > 0) {
            float barY = bounds.y + bounds.height + 4f;
            shapeRenderer.setColor(Color.DARK_GRAY);
            shapeRenderer.rect(bounds.x, barY, bounds.width, 4f);
            
            shapeRenderer.setColor(Color.GREEN);
            float fillWidth = (float) health / maxHealth * bounds.width;
            shapeRenderer.rect(bounds.x, barY, fillWidth, 4f);
        }
    }
}