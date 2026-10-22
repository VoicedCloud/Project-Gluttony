package com.github.glutton.world;

import com.badlogic.gdx.physics.box2d.*;
import com.github.glutton.player.PlayerController;

public class GameContactListener implements ContactListener {
    private final PlayerController playerController;

    public GameContactListener(PlayerController playerController) {
        this.playerController = playerController;
    }

    @Override
    public void beginContact(Contact contact) {
        playerController.handleBeginContact(contact);
    }

    @Override
    public void endContact(Contact contact) {
        playerController.handleEndContact(contact);
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {}

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {}
}