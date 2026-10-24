package com.github.glutton.weapons;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.github.glutton.player.PlayerState;

public class WeaponDebugContext {

    public final PlayerState player;
    public final BulletManager bulletManager;
    public final Vector2 muzzlePos;
    public final OrthographicCamera camera;

    public WeaponDebugContext(PlayerState player, BulletManager bulletManager, Vector2 muzzlePos, OrthographicCamera camera) {
        this.player = player;
        this.bulletManager = bulletManager;
        this.muzzlePos = muzzlePos;
        this.camera = camera;
    }
}
