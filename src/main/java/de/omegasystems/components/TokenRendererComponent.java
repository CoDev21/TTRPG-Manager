package de.omegasystems.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import de.omegasystems.App;
import de.omegasystems.components.dialog.ChangeValueDialog;
import de.omegasystems.components.dialog.ChangeValueDialog.DoubleDialog;
import de.omegasystems.components.dialog.TokenDialog;
import de.omegasystems.core.Renderer;
import de.omegasystems.core.Renderer.DrawingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;

public class TokenRendererComponent extends MouseAdapter implements DrawingComponent, TokenHandler, KeyListener {

    private List<Token> tokens = new ArrayList<>();
    private Color highlightedColor = Color.RED;
    private double highlightThickness = 1.0;
    private double tokenScale = 64.0;

    private Token draggedToken;
    private Token highlightedToken;
    private Point dragOffset;

    private Renderer renderer;

    public TokenRendererComponent(Renderer renderer) {
        this.renderer = renderer;
        renderer.addRenderCallback(this);
        renderer.addMouseListener(this);
        renderer.addMouseMotionListener(this);
        renderer.addKeyListener(this);
    }

    public void registerUIBindings() {
        var toolbarAttributes = App.getInstance().getToolbarAttributes();

        // Create the value bindings
        toolbarAttributes.TOKEN_OUTLINE_COLOR.addObserver(newVal -> {
            this.highlightedColor = newVal;
            notifyChange();
        });
        toolbarAttributes.TOKEN_OUTLINE_THICKNESS.addObserver(newVal -> {
            this.highlightThickness = newVal;
            notifyChange();
        });
        toolbarAttributes.TOKEN_SIZE.addObserver(newVal -> {
            this.tokenScale = newVal;
            notifyChange();
        });

        this.highlightedColor = toolbarAttributes.TOKEN_OUTLINE_COLOR.getValue();
        this.highlightThickness = toolbarAttributes.TOKEN_OUTLINE_THICKNESS.getValue();
        this.tokenScale = toolbarAttributes.TOKEN_SIZE.getValue();

        // Create The button action bindings
        toolbarAttributes.TOKEN_CREATE
                .addObserver(
                        abs -> new TokenDialog(renderer.getFrame(), this));

        toolbarAttributes.TOKEN_OPEN_SIZE_DIALOG.addObserver(
                abs -> new DoubleDialog(renderer.getFrame(),
                        toolbarAttributes.TOKEN_SIZE));

        toolbarAttributes.TOKEN_OPEN_OUTLINE_COLOR_DIALOG.addObserver(
                abs -> ChangeValueDialog.createColorDialog(renderer.getFrame(), toolbarAttributes.TOKEN_OUTLINE_COLOR));
        toolbarAttributes.TOKEN_OPEN_OUTLINE_THICKNESS_DIALOG.addObserver(
                abs -> new DoubleDialog(renderer.getFrame(), toolbarAttributes.TOKEN_OUTLINE_THICKNESS));
    }

    @Override
    public void draw(Graphics2D g, Dimension size, double scale) {
        for (Token token : tokens) {

            int posX = (int) (token.getPosition().x * scale);
            int posY = (int) (token.getPosition().y * scale);

            int scaledImageSize = calculateImageSize(token);
            g.drawImage(token.getImage(), posX, posY, scaledImageSize, scaledImageSize, null);

            // Draw the outline around a highlighted token
            if (!token.equals(highlightedToken))
                continue;

            g.setStroke(new BasicStroke((float) (highlightThickness * scale)));
            g.setColor(highlightedColor);

            int highlightOffset = (int) (highlightThickness * scale / 2);

            g.drawRect(posX - highlightOffset, posY - highlightOffset, scaledImageSize + highlightOffset * 2,
                    scaledImageSize + highlightOffset * 2);

        }
    }

    private int calculateImageSize(Token token) {
        return (int) (renderer.getScale() * tokenScale * token.getSize().getScale());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() != KeyEvent.VK_DELETE) {
            return;
        }

        if (highlightedToken != null) {
            removeToken(highlightedToken);
            highlightedToken = null;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedToken == null)
            return;

        var translatedOffset = e.getPoint();
        translatedOffset.translate(dragOffset.x, dragOffset.y);
        var tokenSize = calculateImageSize(draggedToken);
        var maxPos = renderer.getWorldSize();
        var scale = renderer.getScale();

        // Check that the item would land inside the playable area
        if (translatedOffset.x < 0 || translatedOffset.y < 0 || translatedOffset.x + tokenSize > maxPos.getWidth()
                || translatedOffset.y > maxPos.getHeight())
            return;

        draggedToken.setPosition(new Point2D.Double(translatedOffset.x / scale, translatedOffset.y / scale));
    }

    @Override
    public void mousePressed(MouseEvent e) {

        boolean found = false;
        for (Token token : tokens) {
            var tokenPos = token.getPosition();
            double scale = renderer.getScale();
            int tokenSize = calculateImageSize(token);
            int posX = (int) (tokenPos.x * scale);
            int posY = (int) (tokenPos.y * scale);

            if (new Rectangle(posX, posY, tokenSize, tokenSize)
                    .contains(e.getPoint())) {
                draggedToken = token;
                highlightedToken = token;
                dragOffset = new Point(posX - e.getX(), posY - e.getY());
                notifyChange();
                found = true;
                break;
            }

        }

        if (!found) {
            highlightedToken = null;
            notifyChange();
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggedToken = null;
    }

    @Override
    public void addToken(Token t) {
        if (t == null | tokens.contains(t))
            return;
        tokens.add(t);
        notifyChange();
    }

    @Override
    public java.util.List<Token> getAllTokens() {
        return new ArrayList<>(tokens);
    }

    @Override
    public void removeToken(Token t) {
        if (t == null)
            return;
        tokens.remove(t);
        if (t.equals(highlightedToken))
            highlightedToken = null;
        if (t.equals(draggedToken))
            draggedToken = null;
        notifyChange();
    }

    @Override
    public void notifyChange() {
        renderer.scheduleRedraw();
    }
}
