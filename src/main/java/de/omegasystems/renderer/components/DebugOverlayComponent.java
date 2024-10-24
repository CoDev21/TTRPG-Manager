package de.omegasystems.renderer.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;

public class DebugOverlayComponent implements RenderingComponent {
    private Renderer renderer;

    @Override
    public void draw(Graphics2D g, Dimension size) {
        if (renderer != null) {
            Dimension drawingDimensions = renderer.getDrawingDimensions();
            Dimension screenSize = renderer.getScreenSize();

            g.setColor(Color.WHITE);
            g.drawString("Drawing Dimensions: " + drawingDimensions.width + "x" + drawingDimensions.height, 10, 20);
            g.drawString("Screen Size: " + screenSize.width + "x" + screenSize.height, 10, 40);
        }
    }

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }
}