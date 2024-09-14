package de.omegasystems.components;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import de.omegasystems.App;
import de.omegasystems.core.Background;
import de.omegasystems.core.GameCore;
import de.omegasystems.utility.Observer;

public class BackgroundComponent extends JLabel implements HierarchyListener, Observer<GameCore>, Background {

    Image image;
    double scale = 1.0;
    double gridScale = 64.0;
    double gridThickness = 2;
    boolean isGridEnabled = false;

    public BackgroundComponent(Image image, boolean reCalculateScale) {
        super(new ImageIcon(image));
        this.image = image;

        // We need this because there is no other way of the BackgroundComponent knowing
        // its child
        addHierarchyListener(this);

        var GRID_ENABLED = App.getInstance().getToolbarAttributes().VIEW_GRID_ENABLED;
        GRID_ENABLED.addObserver(val -> {
            this.isGridEnabled = val;
            repaint();
        });
        this.isGridEnabled = GRID_ENABLED.getValue();

        var GRID_SCALE = App.getInstance().getToolbarAttributes().VIEW_GRID_SCALE;
        GRID_SCALE.addObserver(val -> {
            this.gridScale = val;
            repaint();
        });
        this.gridScale = GRID_SCALE.getValue();

        var GRID_THICKNESS = App.getInstance().getToolbarAttributes().VIEW_GRID_THICKNESS;
        GRID_THICKNESS.addObserver(val -> {
            this.gridThickness = val;
            repaint();
        });
        this.gridThickness = GRID_THICKNESS.getValue();

    }

    // This is fine because here is the only place where the
    // setBackgroundComponent() method should be
    // called. It is deprecated in the first place to warn developers to not use it
    @SuppressWarnings("deprecation")
    @Override
    public void hierarchyChanged(HierarchyEvent e) {

        if (e.getChanged() instanceof GameCore gc) {
            gc.addObserver(this);

            gc.setBackgroundComponent(this);
        }
    }

    @Override
    public void update(GameCore newVal) {
        if (newVal.getScale() != this.getScale())
            setScale(newVal.getScale());
    }

    @Override
    public void paint(Graphics g) {
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);


        int scaledImageWidth = (int) (imageWidth * scale);
        int scaledImageHeight = (int) (imageHeight * scale);
        // Draw the background
        g.drawImage(image, 0, 0, scaledImageWidth, scaledImageHeight, null);

        // Draw the grid
        if (!isGridEnabled)
            return;

        Graphics2D g2d = (Graphics2D) g;
        g2d.setStroke(new BasicStroke((float) (gridThickness*scale)));

        for (int x = 0; x < imageWidth / gridScale; x++) {
            int xPos = (int) (x * gridScale * scale);
            g.drawLine(xPos, 0, xPos, (int) (imageHeight * scale));
        }
        for (int y = 0; y < imageHeight / gridScale; y++) {
            int posY = (int) (y * gridScale * scale);
            g.drawLine(0, posY, (int) (imageWidth * scale), posY);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (image.getWidth(null) * scale), (int) (image.getHeight(null) * scale));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    public void setScale(double newScale) {
        this.scale = newScale;
        repaint();
        revalidate();
        if (getParent() != null)
            getParent().repaint();
    }

    public double getScale() {
        return scale;
    }

}