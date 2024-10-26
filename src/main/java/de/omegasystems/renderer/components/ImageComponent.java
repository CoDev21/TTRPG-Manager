package de.omegasystems.renderer.components;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.SizeDefiningRenderingComponent;

public class ImageComponent implements SizeDefiningRenderingComponent {

    private Image image;
    private Renderer renderer;

    public ImageComponent(Image image) {
        if (image == null)
            throw new IllegalArgumentException("[" + this.getClass().getCanonicalName()
                    + "] Image was null during initialization");

        this.image = image;
    }

    @Override
    public void draw(Graphics2D g, Dimension size) {
        g.drawImage(image, 0, 0, null);
    }

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Dimension getDrawingSize() {
        if (image == null)
            return new Dimension();

        return new Dimension(image.getWidth(null), image.getHeight(null));
    }

    public void setImage(Image image) {
        if (image == null)
            return;

        this.image = image;

        if (renderer != null)
            renderer.scheduleRedraw();
    }

}
