package com.github.glutton;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Ladder {

    private final Rectangle bounds;

    public Ladder(float x, float y, float width, float height) {
        this.bounds = new Rectangle(x, y, width, height);
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void draw(ShapeRenderer shapeRenderer) {
        // Ladder side rails (Wooden Brown)
        shapeRenderer.setColor(new Color(139f / 255f, 69f / 255f, 19f / 255f, 1f));
        shapeRenderer.rect(bounds.x, bounds.y, 6, bounds.height);
        shapeRenderer.rect(bounds.x + bounds.width - 6, bounds.y, 6, bounds.height);

        // Ladder rungs (Orange)
        shapeRenderer.setColor(Color.ORANGE);
        for (float i = 10; i < bounds.height; i += 22) {
            shapeRenderer.rect(bounds.x + 4, bounds.y + i, bounds.width - 8, 4);
        }
    }
}