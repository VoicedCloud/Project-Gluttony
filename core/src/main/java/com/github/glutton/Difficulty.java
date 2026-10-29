package com.github.glutton;

public enum Difficulty {

    EASY(0.5f),
    NORMAL(1.0f),
    HARD(1.5f);

    private final float mobMultiplier;

    Difficulty(float mobMultiplier) {
        this.mobMultiplier = mobMultiplier;
    }

    public float getMobMultiplier() {
        return mobMultiplier;
    }
}