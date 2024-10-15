package de.omegasystems.renderer;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.WorldTranslationHandler;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public class TranslationHandler implements WorldTranslationHandler {

    // Constant k determines how fast the scaling increases or decreases.
    private static final double K = 0.1;
    private static final double scrollMultiplier = 10.0;

    /**
     * Calculates the scale based on the current scrolling value of scrollVal.
     * At x = 0, the function returns 1.
     * For x > 0, the scale increases, and for x < 0, it approaches 0.
     *
     * @param x the input value, determined by the scroll wheel.
     * @return the scale factor.
     */
    public static double getScale(double x) {
        return Math.exp(K * x);
    }

    private Observerhandler<WorldTranslationHandler> observerhandler = new Observerhandler<>();
    private Renderer renderer;

    // For values < 0 this zooms in, for > 0 it sooms out. See getScale()
    private double scale = 1.0;
    private int scrollValue = 0;

    private int vScroll = 0;
    private int hScroll = 0;

    public TranslationHandler(Renderer renderer) {
        super();

        this.renderer = renderer;

        renderer.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(final MouseWheelEvent e) {
                if (e.isControlDown()) {
                    updateScale(e.getWheelRotation());
                    return;

                }
                int scroll = e.getUnitsToScroll();
                if (e.isShiftDown()) {
                    updateHOffset(scroll);
                } else {
                    updateVOffset(scroll);
                }
            }
        });

        // TODO: Add moving around the world by using middle mouse button
        // renderer.addMouseMotionListener(new MouseAdapter() {
        // @Override
        // public void mouseMoved(MouseEvent e) {
        // if(e.getMod)
        // }
        // });

    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public Dimension getOffset() {
        return new Dimension((int) (hScroll * scale), (int) (vScroll * scale));
    }

    @Override
    public Point getWorldCoordinateFormUISpace(Point uiPoint) {
        var offset = getOffset();
        return new Point((int) ((uiPoint.x + offset.getWidth()) * scale),
                (int) ((uiPoint.y + offset.getHeight()) * scale));
    }

    private void updateHOffset(int unitsToScroll) {
        hScroll += unitsToScroll*scrollMultiplier;
        hScroll = (int) Math.clamp(hScroll, 0, renderer.getDrawingDimensions().getWidth());
        notifyObservers(null);
    }

    private void updateVOffset(int unitsToScroll) {
        vScroll += unitsToScroll * scrollMultiplier;
        vScroll = (int) Math.clamp(vScroll, 0, renderer.getDrawingDimensions().getHeight());
        notifyObservers(null);
    }

    private void updateScale(int scrolling) {
        scrollValue += scrolling;
        this.scale = getScale(scrollValue);
        notifyObservers(this);
    };

    @Override
    public void addObserver(Observer<WorldTranslationHandler> obs) {
        observerhandler.addObserver(obs);
    }

    @Override
    public void removeObserver(Observer<WorldTranslationHandler> obs) {
        observerhandler.removeObserver(obs);
    }

    @Override
    public void notifyObservers(WorldTranslationHandler value) {
        observerhandler.notifyObservers(value);

    }

}