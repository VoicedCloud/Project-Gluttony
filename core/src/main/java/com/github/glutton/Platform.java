package com.github.glutton;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;

public class Platform {

    public enum PlatformType {
        ONE_WAY,     // Land on top, pass through bottom/sides
        SOLID_BLOCK  // Full 4-way solid collision block
    }

    private final Rectangle bounds;
    private PlatformType type;

    public Platform(float x, float y, float width, float height, PlatformType type) {
        this.bounds = new Rectangle(x, y, width, height);
        this.type = type;
    }

    public Platform(float x, float y, float width, float height) {
        this(x, y, width, height, PlatformType.SOLID_BLOCK);
    }

    public PlatformType getType() {
        return this.type;
    }

    public void setType(PlatformType type) {
        this.type = type;
    }

    public Rectangle getBounds() {
        return bounds;
    }

    public void draw(ShapeRenderer shapeRenderer) {
        // Visual distinction between solid blocks and one-way platforms
        if (type == PlatformType.SOLID_BLOCK) {
            shapeRenderer.setColor(Color.GRAY);
        } else {
            shapeRenderer.setColor(Color.LIGHT_GRAY);
        }

        shapeRenderer.rect(bounds.x, bounds.y, bounds.width, bounds.height);
    }
}