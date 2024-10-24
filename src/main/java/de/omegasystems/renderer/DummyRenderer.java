package de.omegasystems.renderer;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Timer;

import de.omegasystems.App;

public class DummyRenderer extends JLabel {

    private Image image;
    private double scale = 1.0;
    private double targetScale = 1.0;
    private double offsetX = 0, offsetY = 0;
    private double targetOffsetX = 0, targetOffsetY = 0;
    private int lastWidth, lastHeight;
    private Point lastDragPoint;

    private Timer animationTimer;

    // Smoothing control variable
    public boolean enableSmoothing = true;

    // Sensitivity control variables
    public double scrollSensitivity = 10.0;
    public double zoomSensitivity = 1.1;

    public DummyRenderer(Image image) {
        super();
        this.image = image;

        this.addMouseWheelListener(new MouseWheelListener() {
            public void mouseWheelMoved(final MouseWheelEvent e) {
                if (e.isControlDown()) {
                    // Zoom in or out
                    int notches = -e.getWheelRotation();
                    targetScale = scale * (notches > 0 ? zoomSensitivity : 1 / zoomSensitivity);
                    targetScale = clampScale(targetScale);

                    // Calculate the cursor position relative to the image
                    Point cursor = e.getPoint();
                    double cursorX = (cursor.getX() - offsetX) / scale;
                    double cursorY = (cursor.getY() - offsetY) / scale;

                    // Adjust target offsets to zoom towards the cursor
                    targetOffsetX = cursor.getX() - cursorX * targetScale;
                    targetOffsetY = cursor.getY() - cursorY * targetScale;

                    clampOffsets();
                    return;
                }
                int scroll = -e.getUnitsToScroll();
                if (e.isShiftDown()) {
                    // Move the image sideways
                    targetOffsetX += scroll * scrollSensitivity;
                } else {
                    // Move the image up and down
                    targetOffsetY += scroll * scrollSensitivity;
                }
                clampOffsets();
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                int newWidth = getWidth();
                int newHeight = getHeight();

                if (lastWidth != 0 && lastHeight != 0) {
                    double widthRatio = (double) newWidth / lastWidth;
                    double heightRatio = (double) newHeight / lastHeight;
                    targetScale = scale * Math.min(widthRatio, heightRatio);

                    // Adjust target offsets to keep the visible area the same
                    targetOffsetX *= widthRatio;
                    targetOffsetY *= heightRatio;

                    targetScale = clampScale(targetScale);
                    clampOffsets();
                }

                lastWidth = newWidth;
                lastHeight = newHeight;
            }
        });

        this.addMouseListener(new MouseAdapter() {
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

        this.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (lastDragPoint != null) {
                    Point currentPoint = e.getPoint();
                    targetOffsetX += (currentPoint.getX() - lastDragPoint.getX());
                    targetOffsetY += (currentPoint.getY() - lastDragPoint.getY());
                    lastDragPoint = currentPoint;
                    clampOffsets();
                }
            }
        });

        animationTimer = new Timer(16, e -> {
            if (enableSmoothing) {
                // Interpolate scale and offsets
                scale = lerp(scale, targetScale, 0.1);
                offsetX = lerp(offsetX, targetOffsetX, 0.1);
                offsetY = lerp(offsetY, targetOffsetY, 0.1);
            } else {
                // Directly set scale and offsets to target values
                scale = targetScale;
                offsetX = targetOffsetX;
                offsetY = targetOffsetY;
            }
            repaint();
        });
        animationTimer.start();
    }

    private double clampScale(double newScale) {
        int imageWidth = image.getWidth(null);
        int imageHeight = image.getHeight(null);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        double minScaleX = (double) viewWidth / imageWidth;
        double minScaleY = (double) viewHeight / imageHeight;
        double minScale = Math.max(minScaleX, minScaleY);

        return Math.max(newScale, minScale);
    }

    private void clampOffsets() {
        int imageWidth = (int) (image.getWidth(null) * targetScale);
        int imageHeight = (int) (image.getHeight(null) * targetScale);
        int viewWidth = getWidth();
        int viewHeight = getHeight();

        // Clamp targetOffsetX
        if (targetOffsetX > 0) {
            targetOffsetX = 0;
        } else if (targetOffsetX < viewWidth - imageWidth) {
            targetOffsetX = viewWidth - imageWidth;
        }

        // Clamp targetOffsetY
        if (targetOffsetY > 0) {
            targetOffsetY = 0;
        } else if (targetOffsetY < viewHeight - imageHeight) {
            targetOffsetY = viewHeight - imageHeight;
        }
    }

    private double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    @Override
    public void paint(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;

        // Transform the graphics object for world space coordinates
        AffineTransform oldForm = g2d.getTransform();
        g2d.scale(scale, scale);
        g2d.translate(offsetX / scale, offsetY / scale);

        g2d.drawImage(image, 0, 0, null);

        // Reset the transform
        g2d.setTransform(oldForm);
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Dummy Renderer");
        Image image = new App(true).requestImageFromUser();
        DummyRenderer renderer = new DummyRenderer(image);
        frame.add(renderer);
        frame.setSize(800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

