/*
 * =====================================================================================
 * CLASS HEADER: TargetDummy.java
 * =====================================================================================
 * WHAT THIS FILE DOES:
 * - Serves as an interactive target object for testing weapon hit detection and damage output.
 * - Manages hitpoints, health bar rendering, destruction flags, and custom graphics updates.
 *
 * HOW IT CONNECTS TO OTHER SYSTEMS:
 * - Extends 'JComponent' so it can be added directly into 'PlayerPanel.java' layout containers.
 * - Receives damage calls from raycast bullet hits evaluated inside 'PlayerPanel'.
 * - Removed from the parent container by 'PlayerPanel' once its destruction flag returns true.
 * =====================================================================================
 */

package test1;

import javax.swing.*;
import java.awt.*;

public class TargetDummy extends JComponent {

    // =====================================================================================
    // SYSTEM VARIABLE DECLARATIONS
    // =====================================================================================
    private int health = 50;            // Active hitpoints remaining
    private final int maxHealth = 50;   // Maximum baseline capacity for health bar calculations

    // =====================================================================================
    // SYSTEM: CONSTRUCTOR & INITIALIZATION ENGINE
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Sets the default width and height bounding size (30x50 pixels) for layout positioning.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: The constructor sets up the initial component dimensions to 30 pixels wide 
     * by 50 pixels high."
     */
    public TargetDummy() {
        setSize(30, 50); // Default dummy size (width, height)
    }

    // =====================================================================================
    // SYSTEM: DAMAGE APPLICATION & STATE MANAGEMENT
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Subtracts damage values from current health, clamps lower bounds to zero, and triggers 
     * a Swing UI repaint pass to update health bar visuals.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: The takeDamage method subtracts damage points from current health, prevents health 
     * from dropping below zero, and calls repaint to refresh the visual health bar."
     */
    public void takeDamage(int amount) {
        health -= amount;
        if (health < 0) health = 0;
        repaint();
    }

    /*
     * WHAT THIS BLOCK DOES:
     * Evaluates if the target has run out of hitpoints, signaling systems to drop or clean up the entity.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: IsDestroyed returns true when health hits zero, telling PlayerPanel to remove the target."
     */
    public boolean isDestroyed() {
        return health <= 0;
    }

    /*
     * WHAT THIS BLOCK DOES:
     * Accessor returning remaining health points.
     */
    public int getHealth() {
        return health;
    }

    // =====================================================================================
    // SYSTEM: CUSTOM GRAPHICS2D ENTITY & HUD RENDERING
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Handles target rendering: draws a solid red entity box, a black outer outline, and a green 
     * health overlay bar scaled proportionally to remaining hitpoints.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: PaintComponent draws the dummy body, borders, and dynamically calculates 
     * health bar width as a ratio of current health to maximum health."
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // --- RENDER DUMMY BODY (RED BOX) ---
        g2d.setColor(Color.RED);
        g2d.fillRect(0, 0, getWidth(), getHeight());

        // --- RENDER OUTLINE ---
        g2d.setColor(Color.BLACK);
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);

        // --- RENDER DYNAMIC MINI HEALTH BAR ---
        if (health > 0) {
            // Health bar background track
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(0, 0, getWidth(), 6);
            
            // Health bar fill proportional to remaining health
            g2d.setColor(Color.GREEN);
            int healthWidth = (int) Math.round((double) health / maxHealth * getWidth());
            g2d.fillRect(0, 0, healthWidth, 6);
        }

        g2d.dispose(); // Release graphics device context handle
    }
}