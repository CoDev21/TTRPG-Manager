package de.omegasystems.core;

import java.awt.Dimension;
import java.awt.Graphics2D;

public interface RenderingComponent {
    public void draw(Graphics2D g, Dimension size);

    /**
     * This method gets called by the renderer when this class is registered in
     * {@link Renderer#addWorldRenderComponent(RenderingComponent)}
     * 
     * @param renderer The renderer this object is registered to.
     */
    public void setRenderer(Renderer renderer);
}
