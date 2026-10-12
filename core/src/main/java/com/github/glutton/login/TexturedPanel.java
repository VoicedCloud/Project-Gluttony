package test1.login; 

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class TexturedPanel extends JPanel {
    private TexturePaint texturePattern;

    public TexturedPanel() {
        loadTexture();
    }

    private void loadTexture() {
        try {

            java.net.URL imgURL = getClass().getResource("background.png");
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                

                Rectangle rect = new Rectangle(0, 0, img.getWidth(), img.getHeight());

                this.texturePattern = new TexturePaint(img, rect);
            } else {
                createFallbackTexture();
            }
        } catch (IOException e) {
            createFallbackTexture();
        }
    }

    private void createFallbackTexture() {
        BufferedImage img = new BufferedImage(20, 20, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.LIGHT_GRAY);
        g.fillRect(0, 0, 20, 20);
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, 10, 10);
        g.fillRect(10, 10, 10, 10);
        g.dispose();
        this.texturePattern = new TexturePaint(img, new Rectangle(0, 0, 20, 20));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (texturePattern != null) {
            Graphics2D g2d = (Graphics2D) g;
            
            g2d.setPaint(texturePattern);
            
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
