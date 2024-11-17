package de.omegasystems.renderer;

import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;

import javax.swing.Timer;

import de.omegasystems.App;
import de.omegasystems.core.Renderer;
import de.omegasystems.core.WorldTranslationHandler;

public class TranslationHandler implements WorldTranslationHandler {

    private Renderer renderer;
    private double scale = 1.0;
    private double targetScale = 1.0;
    private Point2D.Double offset = new Point2D.Double(0, 0);
    private Point2D.Double targetOffset = new Point2D.Double(0, 0);
    private Point lastDragPoint;
    private Timer animationTimer;

    // Sensitivity control variables
    public double scrollSensitivity = 10.0;
    public double zoomSensitivity = 1.3;

    // Animation control variable
    private boolean enableAnimations = true;

    public TranslationHandler(Renderer renderer) {
        this.renderer = renderer;

        App.getInstance().getToolbarAttributes().VIEW_ANIMATION_SMOOTH_SCROLLING_ENABLED.addObserver(arg -> {
            enableAnimations = arg;
        });
        enableAnimations = App.getInstance().getToolbarAttributes().VIEW_ANIMATION_SMOOTH_SCROLLING_ENABLED.getValue();

        renderer.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(final MouseWheelEvent e) {
                if (e.isControlDown()) {
                    // Zoom in or out
                    int notches = -e.getWheelRotation();
                    targetScale = scale * (notches > 0 ? zoomSensitivity : 1 / zoomSensitivity);
                    targetScale = clampScale(targetScale);

                    // Calculate the cursor position relative to the image
                    Point cursor = e.getPoint();
                    double cursorX = (cursor.getX() - offset.x) / scale;
                    double cursorY = (cursor.getY() - offset.y) / scale;

                    // Adjust target offsets to zoom towards the cursor
                    targetOffset.x = cursor.getX() - cursorX * targetScale;
                    targetOffset.y = cursor.getY() - cursorY * targetScale;

                    clampOffsets();

                    if (!enableAnimations) {
                        scale = targetScale;
                        offset.setLocation(targetOffset);
                        renderer.scheduleRedraw();
                    }
                    return;
                }
                int scroll = -e.getUnitsToScroll();
                if (e.isShiftDown()) {
                    // Move the image sideways
                    targetOffset.x += scroll * scrollSensitivity;
                } else {
                    // Move the image up and down
                    targetOffset.y += scroll * scrollSensitivity;
                }
                clampOffsets();

                if (!enableAnimations) {
                    offset.setLocation(targetOffset);
                    renderer.scheduleRedraw();
                }
            }
        });

        renderer.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    lastDragPoint = e.getPoint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON2) {
                    lastDragPoint = null;
                }
            }
        });

        renderer.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint != null) {
                    Point currentPoint = e.getPoint();
                    targetOffset.x += (currentPoint.getX() - lastDragPoint.getX());
                    targetOffset.y += (currentPoint.getY() - lastDragPoint.getY());
                    lastDragPoint = currentPoint;
                    clampOffsets();

                    if (!enableAnimations) {
                        offset.setLocation(targetOffset);
                        renderer.scheduleRedraw();
                    }
                }
            }
        });

        renderer.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                update();
            }
        });

        animationTimer = new Timer(16, e -> update());
        animationTimer.start();
    }

    @Override
    public void checkBounds() {
        this.scale = clampScale(getScale());
    }

    public void update() {
        if (enableAnimations) {
            // Interpolate scale and offsets
            scale = lerp(scale, targetScale, 0.1);
            offset.x = lerp(offset.x, targetOffset.x, 0.1);
            offset.y = lerp(offset.y, targetOffset.y, 0.1);
        } else {
            // Apply target values directly
            scale = targetScale;
            offset.setLocation(targetOffset);
        }
        renderer.scheduleRedraw();
    }

    private double clampScale(double newScale) {
        var screenDim = renderer.getScreenSize();

        int viewWidth = (int) screenDim.getWidth();
        int viewHeight = (int) screenDim.getHeight();

        double minScaleX = (double) viewWidth / renderer.getDrawingDimensions().getWidth();
        double minScaleY = (double) viewHeight / renderer.getDrawingDimensions().getHeight();
        double minScale = Math.max(minScaleX, minScaleY);

        return Math.max(newScale, minScale);
    }

    private void clampOffsets() {
        var drawingDim = renderer.getDrawingDimensions();
        var screenDim = renderer.getScreenSize();

        int viewWidth = (int) screenDim.getWidth();
        int viewHeight = (int) screenDim.getHeight();

        int imageWidth = (int) (drawingDim.getWidth() * targetScale);
        int imageHeight = (int) (drawingDim.getHeight() * targetScale);

        // Clamp targetOffsetX
        if (targetOffset.x > 0) {
            targetOffset.x = 0;
        } else if (targetOffset.x < viewWidth - imageWidth) {
            targetOffset.x = viewWidth - imageWidth;
        }

        // Clamp targetOffsetY
        if (targetOffset.y > 0) {
            targetOffset.y = 0;
        } else if (targetOffset.y < viewHeight - imageHeight) {
            targetOffset.y = viewHeight - imageHeight;
        }
    }

    private double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    @Override
    public double getScale() {
        return scale;
    }

    @Override
    public Point2D.Double getOffset() {
        return offset;
    }

    @Override
    public Point getWorldCoordinateFormUISpace(Point uiPoint) {
        double worldX = (uiPoint.getX() - offset.x) / scale;
        double worldY = (uiPoint.getY() - offset.y) / scale;
        return new Point((int) worldX, (int) worldY);
    }
}
