package de.omegasystems.components;

import java.awt.Adjustable;
import java.awt.Image;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JScrollPane;

import de.omegasystems.core.Background;
import de.omegasystems.core.GameCore;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public class GameComponent extends JScrollPane implements GameCore {

    // Negative because intuitive zooming is inverted to normal scrolling
    private final double ZOOM_FACTOR = 0.05;
    private final int SCROLL_INCREMENT = 10;
    private Background backgroundComponent;

    private Observerhandler<GameCore> observerhandler = new Observerhandler<>();
    // For values < 0 this zooms in, for > 0 it sooms out. See getScale()
    private int scale = 0;
    private double preScale = 1.0;

    public GameComponent(Image image) {
        super(new BackgroundComponent(image, true));

        getVerticalScrollBar().setUnitIncrement(SCROLL_INCREMENT);
        getHorizontalScrollBar().setUnitIncrement(SCROLL_INCREMENT);

        // Remove the original scroll listener so that our custom listener can prevent
        // scrolling if ctrl is pressed (for zoom). It is not possible otherwise.
        removeMouseWheelListener(getMouseWheelListeners()[0]);

        addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(final MouseWheelEvent e) {
                if (e.isControlDown()) {
                    updateScale(e.getWheelRotation());
                    
                } else if (e.isShiftDown()) {
                    // Horizontal scrolling
                    Adjustable adj = getHorizontalScrollBar();
                    int scroll = e.getUnitsToScroll() * adj.getBlockIncrement();
                    adj.setValue(adj.getValue() + scroll);
                } else {
                    // Vertical scrolling
                    Adjustable adj = getVerticalScrollBar();
                    int scroll = e.getUnitsToScroll() * adj.getBlockIncrement();
                    adj.setValue(adj.getValue() + scroll);
                }
            }
        });

    }

    @Override
    public void setBackgroundImage(Image image) {
        throw new IllegalArgumentException("NOT IMPLEMENTED");
    }

    @Override
    public void setBackgroundComponent(Background newBackground) {
        if (backgroundComponent != null)
            return;
        this.backgroundComponent = newBackground;
        setPrescale(1.0f);
    }


    /**
     * This method tries to fit the calculated double to the somewhot complicated
     * scaling mechanism. If the scroll val = 1, (no zoom), this value should
     * 
     * @param scale
     */
    private void setPrescale(double newScale) {
        if (newScale <= 0)
            return;

        this.preScale = newScale;
        notifyObservers(this);
    }

    private void updateScale(int scrollVal) {
        
        this.scale += scrollVal;
        notifyObservers(this);
    }

    @Override
    public double getScale() {
        double scaledScale = scale * ZOOM_FACTOR;
        if (scale == 0)
            return preScale;
        if (scale > 0)
            return (1 / (1 + scaledScale)) * preScale;
        return (1 - scaledScale) * preScale;
    }

    @Override
    public void addObserver(Observer<GameCore> obs) {
        observerhandler.addObserver(obs);
    }

    @Override
    public void removeObserver(Observer<GameCore> obs) {
        observerhandler.removeObserver(obs);
    }

    @Override
    public void notifyObservers(GameCore value) {
        observerhandler.notifyObservers(value);

    }

}