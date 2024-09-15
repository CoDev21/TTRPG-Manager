package de.omegasystems.core;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JFrame;

public interface Renderer {

    public void addRenderCallback(DrawingComponent onDraw);

    public void removeRenderCallback(DrawingComponent onDraw);

    public interface DrawingComponent {
        public void draw(Graphics2D g, Dimension size, double scale);

    }

    public void addMouseListener(MouseListener l);

    public void addKeyListener(KeyListener l);

    public void addMouseMotionListener(MouseMotionListener l);

    public void scheduleRedraw();

    public JFrame getFrame();

    public double getScale();

    public Dimension getWorldSize();
}
