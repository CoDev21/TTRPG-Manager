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
import de.omegasystems.core.WorldGrid;
import de.omegasystems.renderer.dialog.ChangeValueDialog.DoubleDialog;
import de.omegasystems.renderer.dialog.TokenDialog;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public class TokenRendererComponent extends MouseAdapter implements RenderingComponent, TokenHandler, KeyListener {

    private Observerhandler<TokenHandler> observerhandler = new Observerhandler<>();

    private List<Token> tokens = new ArrayList<>();
    private double highlightThickness = 1.0;
    private double tokenScale = 64.0;

    private boolean isSelectionBoxActive = false;
    private Point selectionBoxStart = new Point();
    private Point selectionBoxEnd = new Point();

    private Token draggedToken;
    private List<Token> highlightedTokens = new ArrayList<>();
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
            g.setColor(highlightedTokens.contains(token) ? token.getFriendStatus().getHighlight()
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

        if (!isSelectionBoxActive)
            return;

        g.setColor(Color.BLACK);
        g.setStroke(new BasicStroke((int) (1 / renderer.getTranslationhandler().getScale())));

        int x = Math.min(selectionBoxStart.x, selectionBoxEnd.x);
        int y = Math.min(selectionBoxStart.y, selectionBoxEnd.y);
        int width = Math.abs(selectionBoxEnd.x - selectionBoxStart.x);
        int height = Math.abs(selectionBoxEnd.y - selectionBoxStart.y);
        g.drawRect(x, y, width, height);
    }

    // Token dragging
    @Override
    public void mouseDragged(MouseEvent e) {
        if (draggedToken == null) {
            selectionBoxEnd = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
            isSelectionBoxActive = true;
            return;
        }

        var clickedWorldPos = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
        var translatedOffset = (Point) clickedWorldPos.clone();
        translatedOffset.translate(dragOffset.x, dragOffset.y);

        var worldGrid = renderer.getComponentImplementing(WorldGrid.class);
        if (e.isControlDown() && worldGrid != null) {
            Point cellOrigin = worldGrid.getContainingCellOrigin(clickedWorldPos);
            double cellSize = worldGrid.getCellSize();
            translatedOffset = new Point(
                (int) (cellOrigin.x + (cellSize - calculateImageSizeFor(draggedToken)) / 2),
                (int) (cellOrigin.y + (cellSize - calculateImageSizeFor(draggedToken)) / 2)
            );
        }

        var tokenSize = calculateImageSizeFor(draggedToken);
        var maxPos = renderer.getDrawingDimensions();

        // Clamp the pos so that plaer cannot be dragge doutside the visible playarea
        translatedOffset.x = clamp(translatedOffset.x, 0, (int) (maxPos.getWidth() - tokenSize));
        translatedOffset.y = clamp(translatedOffset.y, 0, (int) (maxPos.getHeight() - tokenSize));

        draggedToken.setPosition(new Point2D.Double(translatedOffset.x, translatedOffset.y));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1 || e.getClickCount() != 2)
            return;

        Token clickedToken = getTokenFromPosition(e);
        if (clickedToken != null)
            new TokenDialog(renderer.getFrame(), clickedToken);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1)
            return;

        Token token = getTokenFromPosition(e);
        if (token == null) {
            highlightedTokens.clear();
            selectionBoxStart = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
            notifyChange();
            return;
        }

        var tokenPos = token.getPosition();
        int posX = (int) (tokenPos.x);
        int posY = (int) (tokenPos.y);

        var clickedPos = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());

        // Support multiple selections with control
        if (!e.isControlDown()) {
            highlightedTokens.clear();
        }
        highlightedTokens.add(token);

        draggedToken = token;
        dragOffset = new Point((int) (posX - clickedPos.getX()), (int) (posY - clickedPos.getY()));

        // Re-add the token to the list at the back so that it gets drawn last (above
        // all the other Tokens)
        tokens.remove(token);
        tokens.add(token);
        notifyChange();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        draggedToken = null;

        if (!isSelectionBoxActive)
            return;

        Rectangle selectionBox = new Rectangle(
                Math.min(selectionBoxStart.x, selectionBoxEnd.x),
                Math.min(selectionBoxStart.y, selectionBoxEnd.y),
                Math.abs(selectionBoxEnd.x - selectionBoxStart.x),
                Math.abs(selectionBoxEnd.y - selectionBoxStart.y));

        List<Token> tokensInSelectionBox = getTokensInArea(selectionBox);
        highlightedTokens.clear();
        for (Token token : tokensInSelectionBox) {
            highlightedTokens.add(token);
        }
        isSelectionBoxActive = false;

    }

    @Override
    public List<Token> getTokensInArea(Rectangle selectionBox) {
        List<Token> tokensInSelectionBox = new ArrayList<>();
        for (Token token : tokens) {
            var tokenPos = token.getPosition();
            int tokenSize = calculateImageSizeFor(token);

            if (new Rectangle((int) tokenPos.x, (int) tokenPos.y, tokenSize, tokenSize)
                    .intersects(selectionBox))
                tokensInSelectionBox.add(token);
        }
        return tokensInSelectionBox;
    }

    @Override
    public Token getTokenFromPosition(MouseEvent e) {
        Point worldPoint = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
        // Reversed the list to make it consistent with clicking the drawn hierachy
        // (last elements get drawn on top of others)
        for (Token token : tokens) {
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

        for (Token token : new ArrayList<>(highlightedTokens)) {
            removeToken(token);
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

        if (t.equals(draggedToken))
            draggedToken = null;
        highlightedTokens.remove(t);

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

    private int clamp(int value, int min, int max) {
        return value > max ? max : value < min ? min : value;
    }
}
