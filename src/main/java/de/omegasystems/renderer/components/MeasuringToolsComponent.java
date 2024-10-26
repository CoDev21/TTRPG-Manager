package de.omegasystems.renderer.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;

public class MeasuringToolsComponent implements RenderingComponent {

    private Renderer renderer;
    private MeasurmentForm currentForm = null;

    private Point startPoint = new Point();
    private Point endPoint = new Point();

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;

        var frame = renderer.getFrame();
        var inputMap = frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        var actionMap = frame.getRootPane().getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, false), "doNothing");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ALT, 0, true), "doNothing");

        actionMap.put("doNothing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean altPressed = (e.getModifiers() & ActionEvent.ALT_MASK) != 0;
                boolean shiftPressed = (e.getModifiers() & ActionEvent.SHIFT_MASK) != 0;
                boolean ctrlPressed = (e.getModifiers() & ActionEvent.CTRL_MASK) != 0;
                System.out.println("Action performed: " + altPressed + " " + shiftPressed + " " + ctrlPressed);
                updateForm(altPressed, shiftPressed, ctrlPressed);
            }
        });

        renderer.addMouseMotionListener(new MouseAdapter() {

            @Override
            public void mouseMoved(MouseEvent e) {
                if (currentForm == null)
                    return;

                // endPoint =
                // renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
                renderer.scheduleRedraw();
            }

        });

        renderer.addKeyListener(new KeyListener() {
            @Override
            public void keyPressed(KeyEvent e) {
                boolean altPressed = e.isAltDown();
                boolean shiftPressed = e.isShiftDown();
                boolean ctrlPressed = e.isControlDown();
                updateForm(altPressed, shiftPressed, ctrlPressed);
            }

            @Override
            public void keyReleased(KeyEvent e) {
                boolean altPressed = e.isAltDown();
                boolean shiftPressed = e.isShiftDown();
                boolean ctrlPressed = e.isControlDown();
                updateForm(altPressed, shiftPressed, ctrlPressed);
            }

            @Override
            public void keyTyped(KeyEvent e) {
                boolean altPressed = e.isAltDown();
                boolean shiftPressed = e.isShiftDown();
                boolean ctrlPressed = e.isControlDown();
                updateForm(altPressed, shiftPressed, ctrlPressed);
            }
        });
    }

    private void updateForm(boolean altPressed, boolean shiftPressed, boolean ctrlPressed) {

        MeasurmentForm newForm = null;
        if (altPressed) {

            if (shiftPressed) {
                newForm = MeasurmentForm.CONE;
            } else if (ctrlPressed) {
                newForm = MeasurmentForm.CIRCLE;
            } else {
                newForm = MeasurmentForm.LINE;
            }
        }

        if (newForm == currentForm)
            return;

        if (currentForm == null) {
            Point mousePosition = MouseInfo.getPointerInfo().getLocation();
            SwingUtilities.convertPointFromScreen(mousePosition, renderer.getRenderingComponent());
            System.out.println("Mouse position: " + mousePosition);
            startPoint = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(mousePosition);
            endPoint = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(mousePosition);
        }

        currentForm = newForm;
        System.out.println("Switched to form: " + currentForm);

    }

    @Override
    public void draw(Graphics2D g, Dimension size) {
        if (currentForm == null)
            return;

        Point mousePosition = MouseInfo.getPointerInfo().getLocation();
        SwingUtilities.convertPointFromScreen(mousePosition, renderer.getRenderingComponent());
        endPoint = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(mousePosition);

        switch (currentForm) {
            case LINE -> drawLine(g);
            case CIRCLE -> drawCircle(g);
            case CONE -> drawCone(g);
        }

    }

    private void drawCircle(Graphics2D g) {
        int radius = (int) startPoint.distance(endPoint);
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke((float) (2 / renderer.getTranslationhandler().getScale())));
        g.drawOval(startPoint.x - radius, startPoint.y - radius, radius * 2, radius * 2);
        drawLine(g);

    }

    private void drawCone(Graphics2D g) {

        // Calculate the angle of the direct line
        double angle = Math.atan2(endPoint.y - startPoint.y, endPoint.x - startPoint.x);

        // Calculate the angles for the two lines at 30 degrees from the direct line
        double angle1 = angle - Math.toRadians(30);
        double angle2 = angle + Math.toRadians(30);

        // Calculate the endpoints for the two lines
        double originalLength = startPoint.distance(endPoint);
        int length = (int) (originalLength / Math.cos(Math.toRadians(30)));
        Point endPoint1 = new Point(
                (int) (startPoint.x + length * Math.cos(angle1)),
                (int) (startPoint.y + length * Math.sin(angle1)));
        Point endPoint2 = new Point(
                (int) (startPoint.x + length * Math.cos(angle2)),
                (int) (startPoint.y + length * Math.sin(angle2)));

        // Draw the two lines
        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke((float) (2 / renderer.getTranslationhandler().getScale())));
        g.drawLine(startPoint.x, startPoint.y, endPoint1.x, endPoint1.y);
        g.drawLine(startPoint.x, startPoint.y, endPoint2.x, endPoint2.y);

        // Draw the smaller line between the two endpoints
        float[] dashPattern = { 10, 10 };
        g.setStroke(new BasicStroke((float) (2 / renderer.getTranslationhandler().getScale()), BasicStroke.CAP_BUTT,
                BasicStroke.JOIN_MITER, 10, dashPattern, 0));
        g.drawLine(endPoint1.x, endPoint1.y, endPoint2.x, endPoint2.y);

        drawDistanceText(g);
    }

    private void drawLine(Graphics2D g) {
        if ((int) startPoint.distance(endPoint) <= 0)
            return;

        g.setColor(Color.GRAY);
        g.setStroke(new BasicStroke((float) (2 / renderer.getTranslationhandler().getScale())));
        g.drawLine(startPoint.x, startPoint.y, endPoint.x, endPoint.y);

        drawDistanceText(g);
    }

    private void drawDistanceText(Graphics2D g) {
        g.setColor(Color.BLACK);
        g.setFont(g.getFont().deriveFont(Font.BOLD, (float) (16 / renderer.getTranslationhandler().getScale())));

        double distance = startPoint.distance(endPoint);
        String distanceText = "" + ((int) distance);

        int textX = (startPoint.x + endPoint.x) / 2;
        int textY = (startPoint.y + endPoint.y) / 2;

        // Get the metrics of the current font
        FontMetrics metrics = g.getFontMetrics(g.getFont());
        int textWidth = metrics.stringWidth(distanceText);
        int textHeight = metrics.getHeight();

        // Adjust the text position to center it
        int adjustedTextX = textX - (textWidth / 2);
        int adjustedTextY = textY + (textHeight / 2) - metrics.getDescent();

        g.drawString(distanceText, adjustedTextX, adjustedTextY);
    }

    private enum MeasurmentForm {
        LINE, CIRCLE, CONE;

    }
}
