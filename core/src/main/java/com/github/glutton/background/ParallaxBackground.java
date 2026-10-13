package com.github.glutton.background;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;

public class ParallaxBackground {

    private final List<ParallaxLayer> layers = new ArrayList<>();

    public ParallaxBackground() {
        // 1. Static background color at the very back (vertically clamped to avoid top lines)
        layers.add(new ParallaxLayer("sprites/backgrounds/page1/bgcolor.png", 0.0f, 1.0f, true));
        
        // 2. Reversed tree layers with progressive depth speeds (farthest to nearest)
        layers.add(new ParallaxLayer("sprites/backgrounds/page1/Tlayer1.png", 0.15f, 0.3f, false));
        layers.add(new ParallaxLayer("sprites/backgrounds/page1/Tlayer2.png", 0.30f, 0.3f, false));
        layers.add(new ParallaxLayer("sprites/backgrounds/page1/Tlayer3.png", 0.50f, 0.3f, false));
    }

    public void draw(SpriteBatch batch, float cameraX, float cameraLeftEdge, float cameraBottomEdge, float viewWidth, float viewHeight) {
        for (ParallaxLayer layer : layers) {
            layer.draw(batch, cameraX, cameraLeftEdge, cameraBottomEdge, viewWidth, viewHeight);
        }
    }

    public void dispose() {
        for (ParallaxLayer layer : layers) {
            layer.dispose();
        }
    }
}