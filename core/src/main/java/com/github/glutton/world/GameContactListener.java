
package com.github.glutton.world;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.github.glutton.entities.Enemy;
import com.github.glutton.player.PlayerController;

public class GameContactListener implements ContactListener {

    private final PlayerController playerController;

    public GameContactListener(
            PlayerController playerController
    ) {
        this.playerController = playerController;
    }

    @Override
    public void beginContact(Contact contact) {
        playerController.handleBeginContact(contact);
        notifyEnemy(contact, true);
    }

    @Override
    public void endContact(Contact contact) {
        playerController.handleEndContact(contact);
        notifyEnemy(contact, false);
    }

    private void notifyEnemy(
            Contact contact,
            boolean beginning
    ) {
        Object dataA =
            contact.getFixtureA().getBody().getUserData();

        Object dataB =
            contact.getFixtureB().getBody().getUserData();

        if (dataA instanceof Enemy) {
            Enemy enemy = (Enemy) dataA;

            if (beginning) {
                enemy.handleGroundContactBegin(contact);
            } else {
                enemy.handleGroundContactEnd(contact);
            }
        }

        if (dataB instanceof Enemy) {
            Enemy enemy = (Enemy) dataB;

            if (beginning) {
                enemy.handleGroundContactBegin(contact);
            } else {
                enemy.handleGroundContactEnd(contact);
            }
        }
    }

    @Override
    public void preSolve(
            Contact contact,
            Manifold oldManifold
    ) {
    }

    @Override
    public void postSolve(
            Contact contact,
            ContactImpulse impulse
    ) {
    }
}
