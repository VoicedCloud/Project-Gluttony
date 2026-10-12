package com.github.glutton.levels.Diary1;

import com.github.glutton.levels.Level;
import com.github.glutton.Platform; // Update path to where Platform.java is located
import com.github.glutton.Ladder;   // Update path to where Ladder.java is located
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.Rectangle;

public class page1 extends Level {
    private TiledMap map;

    public page1() {
        super(4800, 960, 100, 550);

        // 1. Load the raw TMX file directly
        map = new TmxMapLoader().load("assets/levels/test1.tmx");

        // 2. Extract collision shapes from an Object Layer named "Platforms"
        if (map.getLayers().get("Platforms") != null) {
            for (MapObject object : map.getLayers().get("Platforms").getObjects()) {
                if (object instanceof RectangleMapObject) {
                    Rectangle rect = ((RectangleMapObject) object).getRectangle();
                    getPlatforms().add(new Platform(
                        rect.x, rect.y, rect.width, rect.height, Platform.PlatformType.SOLID_BLOCK
                    ));
                }
            }
        }
    }

    public TiledMap getMap() {
        return map;
    }
}