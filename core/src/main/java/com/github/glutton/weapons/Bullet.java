package com.github.glutton.weapons;

public class Bullet {
    public float x, y;
    public float vx, vy;
    public int damage;
    public float lifeRemaining; // seconds before despawning if nothing is hit

    public Bullet(float x, float y, float vx, float vy, int damage, float lifeRemaining) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.damage = damage;
        this.lifeRemaining = lifeRemaining;
    }
}