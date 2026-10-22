package com.github.glutton.background;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import java.util.ArrayList;
import java.util.List;

public class ParallaxBackground {

	private final List<ParallaxLayer> layers = new ArrayList<>();

	public ParallaxBackground(float baselineCameraY) {
		float groundAnchor = -40f; // bottom of parallax art sits below the visible floor

		// path, scrollFactorX, scrollFactorY, scale, tileVertical, baselineCameraY, baselineBottomY
		layers.add(new ParallaxLayer("sprites/backgrounds/page1/bgcolor.png", 0.0f, 0.0f, 1.0f, false, baselineCameraY, groundAnchor));
		layers.add(new ParallaxLayer("sprites/backgrounds/page1/Tlayer1.png", 0.15f, 0.10f, 0.85f, false, baselineCameraY, groundAnchor));
		layers.add(new ParallaxLayer("sprites/backgrounds/page1/Tlayer2.png", 0.30f, 0.20f, 0.85f, false, baselineCameraY, groundAnchor));
		layers.add(new ParallaxLayer("sprites/backgrounds/page1/Tlayer3.png", 0.50f, 0.35f, 0.85f, false, baselineCameraY, groundAnchor));
	}

	public void draw(SpriteBatch batch, float cameraX, float cameraY,
		float cameraLeftEdge, float cameraBottomEdge,
		float viewWidth, float viewHeight) {
		for (ParallaxLayer layer : layers) {
			layer.draw(batch, cameraX, cameraY, cameraLeftEdge, cameraBottomEdge, viewWidth, viewHeight);
		}
	}

	public void dispose() {
		for (ParallaxLayer layer : layers) {
			layer.dispose();
		}
	}
}
