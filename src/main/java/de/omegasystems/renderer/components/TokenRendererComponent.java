package de.omegasystems.renderer.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import de.omegasystems.App;
import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.renderer.dialog.ChangeValueDialog.DoubleDialog;
import de.omegasystems.renderer.dialog.TokenDialog;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public class TokenRendererComponent extends MouseAdapter implements RenderingComponent, TokenHandler, KeyListener {

    private Observerhandler<TokenHandler> observerhandler = new Observerhandler<>();

    private List<Token> tokens = new ArrayList<>();
    private double highlightThickness = 1.0;
    private double tokenScale = 64.0;

    private Token draggedToken;
    private Token highlightedToken;
    private Point dragOffset;

    private Renderer renderer;

    @Override
    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
        renderer.addWorldRenderComponent(this);
        renderer.addMouseListener(this);
        renderer.addMouseMotionListener(this);
        renderer.addKeyListener(this);
    }

    public void registerUIBindings() {
        var toolbarAttributes = App.getInstance().getToolbarAttributes();

        // Create the value bindings
        toolbarAttributes.TOKEN_OUTLINE_THICKNESS.addObserver(newVal -> {
            this.highlightThickness = newVal;
            notifyChange();
        });
        toolbarAttributes.TOKEN_SIZE.addObserver(newVal -> {
            this.tokenScale = newVal;
            notifyChange();
        });

        this.highlightThickness = toolbarAttributes.TOKEN_OUTLINE_THICKNESS.getValue();
        this.tokenScale = toolbarAttributes.TOKEN_SIZE.getValue();

        // Create The button action bindings
        toolbarAttributes.TOKEN_CREATE
                .addObserver(
                        abs -> new TokenDialog(renderer.getFrame(), this));

        toolbarAttributes.TOKEN_OPEN_SIZE_DIALOG.addObserver(
                abs -> new DoubleDialog(renderer.getFrame(),
                        toolbarAttributes.TOKEN_SIZE));

        toolbarAttributes.TOKEN_OPEN_OUTLINE_THICKNESS_DIALOG.addObserver(
                abs -> new DoubleDialog(renderer.getFrame(), toolbarAttributes.TOKEN_OUTLINE_THICKNESS));
    }

    @Override
    public void draw(Graphics2D g, Dimension drawingDimensions) {

        for (Token token : tokens) {

            int posX = (int) (token.getPosition().x);
            int posY = (int) (token.getPosition().y);

            int scaledImageSize = calculateImageSizeFor(token);
            g.drawImage(token.getImage(), posX, posY, scaledImageSize, scaledImageSize, null);

            g.setStroke(new BasicStroke((float) (highlightThickness)));
            g.setColor(token.equals(highlightedToken) ? token.getFriendStatus().getHighlight()
                    : token.getFriendStatus().getOutline());

            int outlineOffset = (int) (highlightThickness / 2);

            g.drawRect(posX - outlineOffset, posY - outlineOffset, scaledImageSize + outlineOffset * 2,
                    scaledImageSize + outlineOffset * 2);

            // Draw the Tokens' name

            g.setFont(new Font("Georgia", Font.BOLD, 20));
            Font f = g.getFont();
            Rectangle2D charBounds = f.getStringBounds(token.getName(), g.getFontRenderContext());

            int stringPosX = (int) ((posX + scaledImageSize / 2.0) - (charBounds.getWidth() / 2.0));
            int stringPosY = (int) (posY + scaledImageSize + (charBounds.getHeight() / 2.0));

            g.setColor(Color.BLACK);
            g.drawString(token.getName(), stringPosX, stringPosY);

        }

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedToken == null)
            return;

        var translatedOffset = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
        translatedOffset.translate(dragOffset.x, dragOffset.y);
        var tokenSize = calculateImageSizeFor(draggedToken);
        var maxPos = renderer.getDrawingDimensions();

        // Clamp the pos so that plaer cannot be dragge doutside the visible playarea
        translatedOffset.x = Math.clamp(translatedOffset.x, 0, (int) (maxPos.getWidth() - tokenSize));
        translatedOffset.y = Math.clamp(translatedOffset.y, 0, (int) (maxPos.getHeight() - tokenSize));

        draggedToken.setPosition(new Point2D.Double(translatedOffset.x, translatedOffset.y));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getClickCount() == 2 && highlightedToken != null)
            new TokenDialog(renderer.getFrame(), highlightedToken);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        Token token = getTokenFromPosition(e);
        if (token == null) {
            highlightedToken = null;
            notifyChange();
            return;
        }

        var tokenPos = token.getPosition();
        int posX = (int) (tokenPos.x);
        int posY = (int) (tokenPos.y);

        draggedToken = token;
        highlightedToken = token;
        dragOffset = new Point(posX - e.getX(), posY - e.getY());

        // Re-add the token to the list at the back so that it gets drawn last (above
        // all the other Tokens)
        tokens.remove(token);
        tokens.add(token);
        notifyChange();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        draggedToken = null;
    }

    @Override
    public Token getTokenFromPosition(MouseEvent e) {
        Point worldPoint = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
        // Reversed the list to make it consistent with clicking the drawn hierachy
        // (last elements get drawn on top of others)
        for (Token token : tokens.reversed()) {
            var tokenPos = token.getPosition();
            // double scale = renderer.getScale();
            int tokenSize = calculateImageSizeFor(token);

            if (new Rectangle((int) tokenPos.x, (int) tokenPos.y, tokenSize, tokenSize)
                    .contains(worldPoint))
                return token;
        }
        return null;
    }

    public int calculateImageSizeFor(Token token) {
        return (int) (tokenScale * token.getSize().getScale());
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
        if (!tokens.remove(t))
            return;
        if (t.equals(highlightedToken))
            highlightedToken = null;
        if (t.equals(draggedToken))
            draggedToken = null;
        if (t.equals(highlightedToken))
            highlightedToken = null;
        notifyChange();
    }

    @Override
    public boolean hasToken(Token t) {
        return tokens.contains(t);
    }

    @Override
    public void notifyChange() {
        renderer.scheduleRedraw();
        notifyObservers(this);
    }

    @Override
    public void addObserver(Observer<TokenHandler> obs) {
        observerhandler.addObserver(obs);
    }

    @Override
    public void notifyObservers(TokenHandler value) {
        observerhandler.notifyObservers(this);
    }

    @Override
    public void removeObserver(Observer<TokenHandler> obs) {
        observerhandler.removeObserver(obs);
    }
}
