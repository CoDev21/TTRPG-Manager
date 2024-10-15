package de.omegasystems.core;

import java.awt.Dimension;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;

import javax.swing.JFrame;

public interface Renderer {

    /**
     * Adds a rendering component in world space to this renderer. <br>
     * World Space means, that the position and size of elements changes
     * automatically based on the user input, for example zooming in and out
     * 
     * @param onDraw the rendering component to be used
     */
    public void addWorldRenderComponent(RenderingComponent onDraw);

    /**
     * Adds a rendering component in ui space to this renderer.
     * UI Space means, that the position and size of elements stay fixed in
     * comparison to the zoomable map. These components are also always rendered on
     * top of world space components.
     * 
     * @param onDraw the rendering component to be used
     */
    public void addUIRenderComponent(RenderingComponent onDraw);

    public void removeWorldRenderComponent(RenderingComponent onDraw);

    public void removeUIRenderComponent(RenderingComponent onDraw);

    public void addMouseListener(MouseListener l);

    public void addKeyListener(KeyListener l);

    public void addMouseMotionListener(MouseMotionListener l);
    
    public void addMouseWheelListener(MouseWheelListener l);

    public void scheduleRedraw();

    public JFrame getFrame();

    public Dimension getDrawingDimensions();

    public WorldTranslationHandler getTranslationhandler();
}
