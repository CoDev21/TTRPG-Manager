package de.omegasystems.renderer;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.SizeDefiningRenderingComponent;
import de.omegasystems.core.WorldTranslationHandler;
import de.omegasystems.utility.Observer;

public class MainRenderer extends JLabel implements Observer<WorldTranslationHandler>, Renderer {

    private List<RenderingComponent> worldComponents = new ArrayList<>();
    private List<RenderingComponent> uiComponents = new ArrayList<>();

    private WorldTranslationHandler translationHandler;

    private Dimension drawingArea = new Dimension();

    public MainRenderer() {
        super();

        setFocusable(true);
        translationHandler = new TranslationHandler(this);
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2d = (Graphics2D) g;

        // Transform the graphics object for world space coordinates
        AffineTransform oldForm = g2d.getTransform();
        Point2D.Double offset = translationHandler.getOffset();
        g2d.translate(offset.x, offset.y);
        g2d.scale(translationHandler.getScale(), translationHandler.getScale());

        worldComponents.forEach(callback -> callback.draw(g2d, drawingArea));
        // Reset the transform for ui elements
        g2d.setTransform(oldForm);
        uiComponents.forEach(callback -> callback.draw(g2d, drawingArea));

    }

    @Override
    public void scheduleRedraw() {
        repaint();
        revalidate();
        if (getParent() != null)
            getParent().repaint();
    }

    @Override
    public void update(WorldTranslationHandler newVal) {
        scheduleRedraw();
    }

    @Override
    public Dimension getPreferredSize() {
        return getDrawingDimensions();
    }

    @Override
    public Dimension getDrawingDimensions() {
        return (Dimension) drawingArea.clone();
    }

    @Override
    public Dimension getMinimumSize() {
        return getPreferredSize();
    }

    @Override
    public WorldTranslationHandler getTranslationhandler() {
        return translationHandler;
    }

    @Override
    public void addWorldRenderComponent(RenderingComponent onDraw) {
        if (onDraw == null || worldComponents.contains(onDraw) || uiComponents.contains(onDraw))
            return;
        worldComponents.add(onDraw);
        onDraw.setRenderer(this);
        recalculateDrawingArea();
    }

    @Override
    public void removeWorldRenderComponent(RenderingComponent onDraw) {
        if (worldComponents.remove(onDraw))
            recalculateDrawingArea();
    }

    @Override
    public void addUIRenderComponent(RenderingComponent onDraw) {
        if (onDraw == null || worldComponents.contains(onDraw) || uiComponents.contains(onDraw))
            return;
        uiComponents.add(onDraw);
        onDraw.setRenderer(this);
        recalculateDrawingArea();
    }

    @Override
    public void removeUIRenderComponent(RenderingComponent onDraw) {
        if (uiComponents.remove(onDraw))
            recalculateDrawingArea();
    }

    @Override
    public JFrame getFrame() {
        return (JFrame) SwingUtilities.getWindowAncestor(this);
    }

    @Override
    public Dimension getScreenSize() {
        return new Dimension(this.getWidth(), this.getHeight());
    }

    private void recalculateDrawingArea() {
        drawingArea = new Dimension();
        worldComponents.forEach(component -> {
            if (!(component instanceof SizeDefiningRenderingComponent sc))
                return;

            var compDrawSize = sc.getDrawingSize();

            drawingArea = new Dimension((int) Math.max(drawingArea.getWidth(), compDrawSize.getWidth()),
                    (int) Math.max(drawingArea.getHeight(), compDrawSize.getHeight()));
        });
    }

    public <T> T getComponentImplementing(Class<T> interfaceClass) {
        for (RenderingComponent component : worldComponents) {
            if (interfaceClass.isInstance(component)) {
                return interfaceClass.cast(component);
            }
        }
        for (RenderingComponent component : uiComponents) {
            if (interfaceClass.isInstance(component)) {
                return interfaceClass.cast(component);
            }
        }
        return null;
    }

}