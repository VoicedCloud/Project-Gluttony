package com.github.glutton.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {

	private final Texture texture;
	private final TextureRegion region;
	private final float scrollFactorX;
	private final float scrollFactorY;
	private final float scale;
	private final boolean tileVertical;
	private final float baselineCameraY;
	private final float baselineBottomY;

	public ParallaxLayer(String path, float scrollFactorX, float scrollFactorY, float scale,
		boolean tileVertical, float baselineCameraY, float baselineBottomY) {
		this.texture = new Texture(Gdx.files.internal(path));
		this.texture.setWrap(
			TextureWrap.Repeat,
			tileVertical ? TextureWrap.Repeat : TextureWrap.ClampToEdge
		);
		this.region = new TextureRegion(texture);
		this.scrollFactorX = scrollFactorX;
		this.scrollFactorY = scrollFactorY;
		this.scale = scale;
		this.tileVertical = tileVertical;
		this.baselineCameraY = baselineCameraY;
		this.baselineBottomY = baselineBottomY;
	}

	public void draw(SpriteBatch batch, float cameraX, float cameraY,
		float cameraLeftEdge, float cameraBottomEdge,
		float viewWidth, float viewHeight) {

		float scaledWidth = texture.getWidth() * scale;
		float scaledHeight = texture.getHeight() * scale;

		float offsetX = (cameraX * scrollFactorX) % scaledWidth;
		if (offsetX < 0) {
			offsetX += scaledWidth;
		}

		float uMin = offsetX / scaledWidth;
		float uMax = uMin + (viewWidth / scaledWidth);

		float vMin, vMax;
		float drawY, drawHeight;

		if (tileVertical) {
			float offsetY = (cameraY * scrollFactorY) % scaledHeight;
			if (offsetY < 0) {
				offsetY += scaledHeight;
			}

			vMin = offsetY / scaledHeight;
			vMax = vMin + (viewHeight / scaledHeight);

			drawY = cameraBottomEdge;
			drawHeight = viewHeight;
		} else {
			vMin = 0f;
			vMax = 1f;

			float sinkOffset = Math.max(0f, (cameraY - baselineCameraY) * scrollFactorY);
			drawY = cameraBottomEdge - sinkOffset;
			drawHeight = scaledHeight;
		}

		region.setRegion(uMin, vMin, uMax, vMax);
		batch.draw(region, cameraLeftEdge, drawY, viewWidth, drawHeight);
	}

	public void dispose() {
		texture.dispose();
	}
}
