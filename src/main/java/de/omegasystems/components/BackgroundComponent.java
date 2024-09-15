package de.omegasystems.components;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import de.omegasystems.App;
import de.omegasystems.core.BackgroundHolder;
import de.omegasystems.core.Renderer;
import de.omegasystems.utility.Observer;

public class BackgroundComponent extends JLabel implements Observer<BackgroundHolder>, Renderer {

    private List<DrawingComponent> drawCallbacks = new ArrayList<>();

    Image image;
    double scale = 1.0;
    double gridScale = 64.0;
    double gridThickness = 2;
    double gridOffsetX = 0;
    double gridOffsetY = 0;
    boolean isGridEnabled = false;

    public BackgroundComponent(Image image) {
        super(new ImageIcon(image));
        this.image = image;

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

        var GRID_OFFSET_X = App.getInstance().getToolbarAttributes().VIEW_GRID_OFFSET_X;
        GRID_OFFSET_X.addObserver(val -> {
            this.gridOffsetX = val;
            repaint();
        });
        this.gridOffsetX = GRID_OFFSET_X.getValue();

        var GRID_OFFSET_Y = App.getInstance().getToolbarAttributes().VIEW_GRID_OFFSET_Y;
        GRID_OFFSET_Y.addObserver(val -> {
            this.gridOffsetY = val;
            repaint();
        });
        this.gridOffsetY = GRID_OFFSET_Y.getValue();

    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);

        int scaledImageWidth = (int) (imageWidth * scale);
        int scaledImageHeight = (int) (imageHeight * scale);
        // Draw the actual background image
        g.drawImage(image, 0, 0, scaledImageWidth, scaledImageHeight, null);

        if (isGridEnabled) {
            g2d.setStroke(new BasicStroke((float) (gridThickness * scale)));

            for (int x = 0; x < imageWidth / gridScale; x++) {
                int xPos = (int) (x * gridScale * scale + gridOffsetX);
                g.drawLine(xPos, 0, xPos, (int) (imageHeight * scale));
            }
            for (int y = 0; y < imageHeight / gridScale; y++) {
                int posY = (int) (y * gridScale * scale + gridOffsetY);
                g.drawLine(0, posY, (int) (imageWidth * scale), posY);
            }
        }

        notifyDrawCallbacks(g2d, getPreferredSize());

    }

    @Override
    public void scheduleRedraw() {
        repaint();
        revalidate();
        if (getParent() != null)
            getParent().repaint();
    }

    @Override
    public void update(BackgroundHolder newVal) {
        if (newVal.getScale() != this.getScale())
            setScale(newVal.getScale());
    }

    @Override
    public Dimension getPreferredSize() {
        return getWorldSize();
    }

    @Override
    public Dimension getWorldSize() {
        return new Dimension((int) (image.getWidth(null) * scale), (int) (image.getHeight(null) * scale));
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public void addRenderCallback(DrawingComponent onDraw) {
        drawCallbacks.add(onDraw);
    }

    @Override
    public void removeRenderCallback(DrawingComponent onDraw) {
        drawCallbacks.remove(onDraw);
    }

    @Override
    public JFrame getFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    private void notifyDrawCallbacks(Graphics2D graphics2d, Dimension size) {
        drawCallbacks.forEach(callback -> callback.draw(graphics2d, size, getScale()));
    }

    public void setScale(double newScale) {
        this.scale = newScale;
        scheduleRedraw();
    }

    @Override
    public double getScale() {
        return scale;
    }

}