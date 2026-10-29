package com.github.glutton.world;

// Box2D classes used to detect and handle collisions
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

// Classes used by the game
import com.github.glutton.entities.Enemy;
import com.github.glutton.player.PlayerController;

public class GameContactListener implements ContactListener {

    // Controls the player's movement and collision behavior
    private final PlayerController playerController;

    // Constructor: receives the PlayerController when this listener is created
    public GameContactListener(
            PlayerController playerController
    ) {
        this.playerController = playerController;
    }

    // Called when two Box2D fixtures begin touching each other
    @Override
    public void beginContact(Contact contact) {

        // Notify the player controller that a collision has started
        playerController.handleBeginContact(contact);

        // Notify any enemy involved in the collision
        // true means the contact is beginning
        notifyEnemy(contact, true);
    }

    // Called when two Box2D fixtures stop touching each other
    @Override
    public void endContact(Contact contact) {

        // Notify the player controller that a collision has ended
        playerController.handleEndContact(contact);

        // Notify any enemy involved in the collision
        // false means the contact is ending
        notifyEnemy(contact, false);
    }

    // Checks both bodies involved in a collision and notifies
    // an enemy if one of the bodies belongs to an Enemy object.
    private void notifyEnemy(
            Contact contact,
            boolean beginning
    ) {

        // Get the custom user data attached to the first body
        Object dataA =
            contact.getFixtureA().getBody().getUserData();

        // Get the custom user data attached to the second body
        Object dataB =
            contact.getFixtureB().getBody().getUserData();

        // Check whether the first body belongs to an enemy
        if (dataA instanceof Enemy) {

            // Convert the body data into an Enemy object
            Enemy enemy = (Enemy) dataA;

            // Tell the enemy that contact has started or ended
            if (beginning) {
                enemy.handleGroundContactBegin(contact);
            } else {
                enemy.handleGroundContactEnd(contact);
            }
        }

        // Check whether the second body belongs to an enemy
        if (dataB instanceof Enemy) {

            // Convert the body data into an Enemy object
            Enemy enemy = (Enemy) dataB;

            // Tell the enemy that contact has started or ended
            if (beginning) {
                enemy.handleGroundContactBegin(contact);
            } else {
                enemy.handleGroundContactEnd(contact);
            }
        }
    }

    // Called before Box2D calculates the collision response.
    // Can be used to change collision behavior, but is currently empty.
    @Override
    public void preSolve(
            Contact contact,
            Manifold oldManifold
    ) {
        // No additional collision processing is needed here yet.
    }

    // Called after Box2D calculates the collision response.
    // Can be used to process collision forces or impulses.
    // Currently, no additional processing is performed.
    @Override
    public void postSolve(
            Contact contact,
            ContactImpulse impulse
    ) {
        // No additional collision processing is needed here yet.
    }
}