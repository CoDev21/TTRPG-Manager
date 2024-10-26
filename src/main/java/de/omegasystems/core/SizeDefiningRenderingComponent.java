package de.omegasystems.core;

import java.awt.Dimension;

/**
 * This class can be used by a renderer to calulate the required drawing
 * area.<br>
 * <br>
 * Although implementation specific, the intended use for the renderer is the
 * following:<br>
 * <br>
 * - Loop through every component and check if it is a
 * SizeDefiningRenderingComponent<br>
 * <br>
 * - Compare the current drawing size with the interface's reported size and
 * adjust accordingly (stretch the rendering dimensions)
 */
public interface SizeDefiningRenderingComponent extends RenderingComponent {

    public Dimension getDrawingSize();

}
