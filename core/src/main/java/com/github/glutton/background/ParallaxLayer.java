package com.github.glutton.background;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class ParallaxLayer {

    private final Texture texture;
    private final TextureRegion region;
    private final float scrollFactor;
    private final float scale;

    public ParallaxLayer(String path, float scrollFactor, float scale, boolean clampVertical) {
        this.texture = new Texture(Gdx.files.internal(path));
        // Repeat horizontally for seamless scrolling, clamp vertically to prevent top/bottom artifact lines
        this.texture.setWrap(TextureWrap.Repeat, clampVertical ? TextureWrap.ClampToEdge : TextureWrap.Repeat);
        this.region = new TextureRegion(texture);
        this.scrollFactor = scrollFactor;
        this.scale = scale;
    }

    public void draw(SpriteBatch batch, float cameraX, float cameraLeftEdge, float cameraBottomEdge, float viewWidth, float viewHeight) {
        float scaledWidth = texture.getWidth() * scale;

        float offsetX = (cameraX * scrollFactor) % scaledWidth;
        if (offsetX < 0) {
            offsetX += scaledWidth;
        }

        float uMin = offsetX / scaledWidth;
        float uMax = uMin + (viewWidth / scaledWidth);

        region.setRegion(uMin, 0f, uMax, 1f);

        batch.draw(region, cameraLeftEdge, cameraBottomEdge, viewWidth, viewHeight);
    }

    public void dispose() {
        texture.dispose();
    }
}