/*
 * =====================================================================================
 * CLASS HEADER: GifBackgroundPanel.java
 * =====================================================================================
 * WHAT THIS FILE DOES:
 * - Provides a reusable custom Swing 'JPanel' that renders an animated GIF as a full-screen 
 * or stretched background layer.
 * - Enforces pixel-perfect, crisp nearest-neighbor image scaling without blurriness.
 * - Integrates with Java Swing's 'ImageObserver' mechanism to auto-repaint GIF animation frames.
 *
 * HOW IT CONNECTS TO OTHER SYSTEMS:
 * - Used as a background container inside main menus, level setup screens, or cutscene panels.
 * - Loaded directly by NetBeans GUI components or code-driven UI layouts.
 * =====================================================================================
 */

package test1.GUI;

import javax.swing.*;
import java.awt.*;

public class GifBackgroundPanel extends JPanel {

    // =====================================================================================
    // SYSTEM VARIABLE DECLARATIONS
    // =====================================================================================
    private Image gifImage; // Reference to the loaded animated GIF image object

    // =====================================================================================
    // SYSTEM: CONSTRUCTORS & ASSET LOADING ENGINE
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Loads the animated GIF resource at construction time or updates it dynamically later.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: The constructor and setGifPath methods load animated GIF images from 
     * application resources into an ImageIcon handle."
     */
    public GifBackgroundPanel(String resourcePath) {
        loadGif(resourcePath);
    }

    /** Allows changing the GIF after construction (e.g., after initComponents()). */
    public void setGifPath(String resourcePath) {
        loadGif(resourcePath);
    }

    private void loadGif(String resourcePath) {
        java.net.URL imgURL = getClass().getResource(resourcePath);
        if (imgURL != null) {
            gifImage = new ImageIcon(imgURL).getImage();
        } else {
            gifImage = null;
            System.err.println("Could not locate GIF file at: " + resourcePath);
        }
    }

    // =====================================================================================
    // SYSTEM: CANVAS RENDERING & PIXEL-ART SCALING ENGINE
    // =====================================================================================
    /*
     * WHAT THIS BLOCK DOES:
     * Clears the background panel surface, sets nearest-neighbor interpolation to prevent blurriness, 
     * and stretches the animated GIF to fit the full dimensions of the container.
     * * HOW TO EXPLAIN IN A PRESENTATION:
     * "Explanation: PaintComponent clears the panel, enables nearest-neighbor pixel interpolation for sharp 
     * scaling, and draws the animated background stretched across the panel dimensions."
     */
    @Override
    protected void paintComponent(Graphics g) {
        // Clears panel background to prevent graphic tearing or duplicate trailing frames
        super.paintComponent(g); 

        if (gifImage != null) {
            Graphics2D g2d = (Graphics2D) g.create();

            // Enforces clean, crisp pixel art scaling (disables smooth blurry bilinear filtering)
            g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, 
                                 RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);

            // Renders stretched GIF passing 'this' (JPanel) as the ImageObserver for animated frame ticks
            g2d.drawImage(gifImage, 0, 0, getWidth(), getHeight(), this);

            g2d.dispose(); // Release graphics device context handle
        }
    }
}