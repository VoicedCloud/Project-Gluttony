package test1.login;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MossPanel extends JLabel {
    private BufferedImage mossImage;
    
    // Properties that reveal as checkboxes in NetBeans
    private boolean mirrorHorizontal = false;
    private boolean mirrorVertical = false;

    public MossPanel() {
        setOpaque(false);
        setPreferredSize(new Dimension(200, 200)); 
        loadMoss();
    }

    public boolean isMirrorHorizontal() { return mirrorHorizontal; }
    public void setMirrorHorizontal(boolean mirrorHorizontal) {
        this.mirrorHorizontal = mirrorHorizontal;
        repaint(); 
    }

    public boolean isMirrorVertical() { return mirrorVertical; }
    public void setMirrorVertical(boolean mirrorVertical) {
        this.mirrorVertical = mirrorVertical;
        repaint();
    }

    private void loadMoss() {
        try {
            java.net.URL mossURL = getClass().getResource("moss.png");
            if (mossURL != null) {
                this.mossImage = ImageIO.read(mossURL);
            }
        } catch (IOException e) {
            System.err.println("Error loading moss: " + e.getMessage());
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (mossImage == null) return;
        Graphics2D g2d = (Graphics2D) g;
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        int imgW = mossImage.getWidth();
        int imgH = mossImage.getHeight();
        int labelW = getWidth();
        int labelH = getHeight();

        // 1. Calculate how much we need to scale to fit width vs height
        double scaleX = (double) labelW / imgW;
        double scaleY = (double) labelH / imgH;
        
        // 2. Use the SMALLER scale factor to ensure the entire image fits WITHOUT clipping
        double fitScale = Math.min(scaleX, scaleY);

        // 3. Compute final width and height based on the safe scale factor
        int finalW = (int) (imgW * fitScale);
        int finalH = (int) (imgH * fitScale);

        // 4. Center the fitted image inside your panel box
        int x = (labelW - finalW) / 2;
        int y = (labelH - finalH) / 2;

        // 5. Source coordinate swapping for clean mirroring
        int srcX1 = 0;
        int srcY1 = 0;
        int srcX2 = imgW;
        int srcY2 = imgH;

        if (mirrorHorizontal) {
            srcX1 = imgW;
            srcX2 = 0;
        }

        if (mirrorVertical) {
            srcY1 = imgH;
            srcY2 = 0;
        }

        // 6. Draw the image contained perfectly within the layout limits
        g2d.drawImage(mossImage, 
                      x, y, x + finalW, y + finalH, 
                      srcX1, srcY1, srcX2, srcY2, 
                      null);

        super.paintComponent(g); 
    }
}
