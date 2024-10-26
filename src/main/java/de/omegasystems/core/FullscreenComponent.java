package de.omegasystems.core;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.KeyStroke;

public class FullscreenComponent implements RenderingComponent {

    private Renderer renderer;
    private boolean fullscreen;
    private JMenuBar jMenuBar;

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
        var frame = renderer.getFrame();
        var inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var actionMap = frame.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_F11, 0, false), "toggleFullscreen");

        actionMap.put("toggleFullscreen", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleFullscreeen();
            }
        });
    }

    @Override
    public void draw(Graphics2D g, Dimension size) {

    }

    public void toggleFullscreeen() {
        GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        fullscreen = !fullscreen;

        var frame = renderer.getFrame();

        if (!fullscreen) {
            // Leave fullscreen
            frame.setJMenuBar(jMenuBar);
            device.setFullScreenWindow(null);
        } else {
            // Enter fullscreen
            jMenuBar = frame.getJMenuBar();
            frame.setJMenuBar(null);
            device.setFullScreenWindow(frame);
        }
    }

}
