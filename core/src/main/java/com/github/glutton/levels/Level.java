package com.github.glutton.levels;

import com.github.glutton.Platform; // Update path to where Platform.java is located
import com.github.glutton.Ladder;   // Update path to where Ladder.java is located
import java.util.ArrayList;
import java.util.List;

public class Level {
    private final int width;
    private final int height;
    private final int spawnX;
    private final int spawnY;
    private final List<Platform> platforms = new ArrayList<>();
    private final List<Ladder> ladders = new ArrayList<>();

    public Level(int width, int height, int spawnX, int spawnY) {
        this.width = width;
        this.height = height;
        this.spawnX = spawnX;
        this.spawnY = spawnY;
    }

    public List<Platform> getPlatforms() { return platforms; }
    public List<Ladder> getLadders() { return ladders; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSpawnX() { return spawnX; }
    public int getSpawnY() { return spawnY; }
}