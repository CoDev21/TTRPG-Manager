package de.omegasystems.renderer.components;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;

import de.omegasystems.App;
import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.WorldGrid;

public class GridComponent implements RenderingComponent, WorldGrid {

    double gridScale = 64.0;
    double gridThickness = 2;
    double gridOffsetX = 0;
    double gridOffsetY = 0;
    boolean isGridEnabled = false;

    private Renderer renderer;

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;

        var GRID_ENABLED = App.getInstance().getToolbarAttributes().VIEW_GRID_ENABLED;
        GRID_ENABLED.addObserver(val -> {
            this.isGridEnabled = val;
            renderer.scheduleRedraw();
        });
        this.isGridEnabled = GRID_ENABLED.getValue();

        var GRID_SCALE = App.getInstance().getToolbarAttributes().VIEW_GRID_SCALE;
        GRID_SCALE.addObserver(val -> {
            this.gridScale = val;
            renderer.scheduleRedraw();
        });
        this.gridScale = GRID_SCALE.getValue();

        var GRID_THICKNESS = App.getInstance().getToolbarAttributes().VIEW_GRID_THICKNESS;
        GRID_THICKNESS.addObserver(val -> {
            this.gridThickness = val;
            renderer.scheduleRedraw();
        });
        this.gridThickness = GRID_THICKNESS.getValue();

        var GRID_OFFSET_X = App.getInstance().getToolbarAttributes().VIEW_GRID_OFFSET_X;
        GRID_OFFSET_X.addObserver(val -> {
            this.gridOffsetX = val;
            renderer.scheduleRedraw();
        });
        this.gridOffsetX = GRID_OFFSET_X.getValue();

        var GRID_OFFSET_Y = App.getInstance().getToolbarAttributes().VIEW_GRID_OFFSET_Y;
        GRID_OFFSET_Y.addObserver(val -> {
            this.gridOffsetY = val;
            renderer.scheduleRedraw();
        });
        this.gridOffsetY = GRID_OFFSET_Y.getValue();
    }

    @Override
    public void draw(Graphics2D g, Dimension size) {
        if (!isGridEnabled)
            return;
        g.setStroke(new BasicStroke((float) (gridThickness)));

        for (int x = 0; x <= size.getWidth() / gridScale; x++) {
            int xPos = (int) (x * gridScale + gridOffsetX);
            g.drawLine(xPos, 0, xPos, (int) (size.getHeight()));
        }
        for (int y = 0; y <= size.getHeight() / gridScale; y++) {
            int posY = (int) (y * gridScale + gridOffsetY);
            g.drawLine(0, posY, (int) (size.getWidth()), posY);
        }
    }

    @Override
    public Point getContainingCellOrigin(Point pos) {
        int cellX = (int) ((pos.x - gridOffsetX) / gridScale) * (int) gridScale + (int) gridOffsetX;
        int cellY = (int) ((pos.y - gridOffsetY) / gridScale) * (int) gridScale + (int) gridOffsetY;
        return new Point(cellX, cellY);
    }
}

