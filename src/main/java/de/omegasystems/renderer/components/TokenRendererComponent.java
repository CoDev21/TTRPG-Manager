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
import java.util.Collections;
import java.util.List;

import de.omegasystems.App;
import de.omegasystems.core.Renderer;
import de.omegasystems.core.RenderingComponent;
import de.omegasystems.core.Token;
import de.omegasystems.core.TokenHandler;
import de.omegasystems.core.TokenSelectionHandler;
import de.omegasystems.core.WorldGrid;
import de.omegasystems.renderer.dialog.ChangeValueDialog.DoubleDialog;
import de.omegasystems.renderer.dialog.TokenDialog;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public class TokenRendererComponent extends MouseAdapter
        implements RenderingComponent, TokenHandler, TokenSelectionHandler, KeyListener {

    private record TokenDragElement(Token token, Point offset) {
    }

    private Observerhandler<TokenHandler> observerhandler = new Observerhandler<>();
    private Renderer renderer;

    private double highlightThickness = 1.0;
    private double tokenScale = 64.0;

    private List<Token> tokens = new ArrayList<>();
    private List<Token> highlightedTokens = new ArrayList<>();
    private List<TokenDragElement> draggedTokens = new ArrayList<>();

    private Point selectionBoxStart = new Point();
    private Point selectionBoxEnd = new Point();
    private Point originalDragPoint = new Point();
    private boolean dragCanBeInitiated = false;
    private boolean dragIsSelectionBox = false;
    private boolean isSelectionBoxActive = false;
    private boolean releaseShouldClearSelection = false;

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

            var tokenPos = token.getTopLeftPosition();
            int posX = (int) (tokenPos.x);
            int posY = (int) (tokenPos.y);

            int scaledImageSize = calculateImageSizeFor(token);
            g.drawImage(token.getImage(), posX, posY, scaledImageSize, scaledImageSize, null);

            g.setStroke(new BasicStroke((float) (highlightThickness)));
            g.setColor(highlightedTokens.contains(token) ? token.getFriendStatus().getHighlight()
                    : token.getFriendStatus().getOutline());

            int outlineOffset = (int) (highlightThickness / 4);

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
        g.setStroke(new BasicStroke((int) (1.5 / renderer.getTranslationhandler().getScale())));

        int x = Math.min(selectionBoxStart.x, selectionBoxEnd.x);
        int y = Math.min(selectionBoxStart.y, selectionBoxEnd.y);
        int width = Math.abs(selectionBoxEnd.x - selectionBoxStart.x);
        int height = Math.abs(selectionBoxEnd.y - selectionBoxStart.y);
        g.drawRect(x, y, width, height);
    }

    // Token dragging
    @Override
    public void mouseDragged(MouseEvent e) {
        // This is called once for the first drag.
        // The flag is set in the moude down event because there is no other way of
        // getting the button and we only want the drag to happen on a left click
        if (dragCanBeInitiated) {
            dragCanBeInitiated = false;
            // Initiate a selection box drawing instead of moving some tokens, if the
            // selection box is empty
            if (dragIsSelectionBox) {
                isSelectionBoxActive = true;
                if (!e.isControlDown())
                    highlightedTokens.clear();
            } else
                initiateDragging();
        }
        if (isSelectionBoxActive) {
            selectionBoxEnd = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
            notifyChange();
            return;
        }

        // Update all values according to their
        var worldGrid = renderer.getComponentImplementing(WorldGrid.class);

        var clickedWorldPos = renderer.getTranslationhandler()
                .getWorldCoordinateFormUISpace(e.getPoint());
        draggedTokens.forEach(tokenDrag ->

        updateDraggedToken(e.isControlDown(), worldGrid, tokenDrag.token(), tokenDrag.offset(), clickedWorldPos));
    }

    private void updateDraggedToken(boolean isCtrl, WorldGrid worldGrid, Token draggedToken, Point dragOffset,
            Point clickedWorldPos) {
        var translatedOffset = (Point) clickedWorldPos.clone();
        translatedOffset.translate(dragOffset.x, dragOffset.y);

        if (isCtrl && worldGrid != null) {
            double cellSize = worldGrid.getCellSize();
            Point cellOrigin = worldGrid.getContainingCellOrigin(translatedOffset);
            cellOrigin.translate((int) (cellSize / 2.0), (int) (cellSize / 2.0));
            translatedOffset = cellOrigin;
        }

        var tokenSize = calculateImageSizeFor(draggedToken);
        var maxPos = renderer.getDrawingDimensions();

        // Clamp the pos so that plaer cannot be dragge doutside the visible playarea
        translatedOffset.x = clamp(translatedOffset.x, tokenSize / 2, (int) (maxPos.getWidth() - tokenSize / 2));
        translatedOffset.y = clamp(translatedOffset.y, tokenSize / 2, (int) (maxPos.getHeight() - tokenSize / 2));

        draggedToken.setPosition(new Point2D.Double(translatedOffset.x, translatedOffset.y));
    }

    private void initiateDragging() {
        // Add all highlighted tokens to the dragged list and calculate their offset to
        // the current mouse position
        draggedTokens.clear();
        highlightedTokens.forEach(token -> {
            var tokenPos = token.getPosition();
            var dragOffset = new Point((int) (tokenPos.x - originalDragPoint.getX()),
                    (int) (tokenPos.y - originalDragPoint.getY()));
            draggedTokens.add(new TokenDragElement(token, dragOffset));
        });
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
        // Here we need to tell the drag handler that the last press was a left click or
        // not. Needed because it cannot be determined inside the drag event and we only
        // want dragging to occur for left click not right or middle
        if (e.getButton() != MouseEvent.BUTTON1) {
            dragCanBeInitiated = false;
            return;
        } else
            dragCanBeInitiated = true;
        originalDragPoint = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());

        Token token = getTokenFromPosition(e);
        if (token == null) {
            dragIsSelectionBox = true;
            releaseShouldClearSelection = true;
            selectionBoxStart = renderer.getTranslationhandler().getWorldCoordinateFormUISpace(e.getPoint());
            notifyChange();
            return;
        }
        dragIsSelectionBox = false;

        // Windows Explorer behaviour, whhen an item is clicked on:
        // If ctrl is pressed, The item selection is toggled
        // If ctrl is not pressed:
        // If the item is in the active selection, do nothing (the clearing is done on
        // mouse release => we need to set a flag for this)
        // If the item is not: Clear the selection and set the item as the only
        // highligted one

        releaseShouldClearSelection = false;

        if (e.isControlDown()) {
            if (highlightedTokens.contains(token))
                highlightedTokens.remove(token);
            else
                highlightedTokens.add(token);
        } else if (!highlightedTokens.contains(token)) {
            highlightedTokens.clear();
            highlightedTokens.add(token);
        } else
            releaseShouldClearSelection = true;
        notifyChange();

    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() != MouseEvent.BUTTON1) {
            return;
        }

        // This is part of the windows explorer selection behaviour:
        // When clicking on a selected item without starting to drag (dragCanBeInitiated
        // is true), the selection is cleared and the item under the cursor is selected
        // (if there is one)
        if (dragCanBeInitiated && releaseShouldClearSelection) {
            highlightedTokens.clear();
            Token t = getTokenFromPosition(e);
            if (t != null)
                highlightedTokens.add(t);
            return;
        }

        if (!isSelectionBoxActive)
            return;

        Rectangle selectionBox = new Rectangle(
                Math.min(selectionBoxStart.x, selectionBoxEnd.x),
                Math.min(selectionBoxStart.y, selectionBoxEnd.y),
                Math.abs(selectionBoxEnd.x - selectionBoxStart.x),
                Math.abs(selectionBoxEnd.y - selectionBoxStart.y));

        List<Token> tokensInSelectionBox = getTokensInArea(selectionBox);

        // The selection box behaviour copied from windows:
        // Without Ctrl pressed, it just adds the items to the selction
        // If ctrl is pressed, the selection tool inverts any selection under it,
        // meaning that not selected
        // tokens get highlighted (wich is what you would expect from the selection),
        // but when there are already selected items in the area, they get deselected
        //
        // It actually does not clear the selection, because that is done at the mouse
        // down of the selection (when no ctrl is pressed)

        for (Token token : tokensInSelectionBox) {
            if (highlightedTokens.contains(token) && e.isControlDown())
                highlightedTokens.remove(token);
            else
                highlightedTokens.add(token);
        }

        isSelectionBoxActive = false;
        notifyChange();
    }

    @Override
    public List<Token> getTokensInArea(Rectangle selectionBox) {
        List<Token> tokensInSelectionBox = new ArrayList<>();
        for (Token token : tokens) {
            var tokenPos = token.getTopLeftPosition();
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
        var tmpList = new ArrayList<>(tokens);
        Collections.reverse(tmpList);
        for (Token token : tmpList) {
            var tokenPos = token.getTopLeftPosition();
            // double scale = renderer.getScale();
            int tokenSize = calculateImageSizeFor(token);

            if (new Rectangle((int) tokenPos.x, (int) tokenPos.y, tokenSize, tokenSize)
                    .contains(worldPoint))
                return token;
        }
        return null;
    }

    public int calculateImageSizeFor(Token token) {
        return (int) (tokenScale * token.getSize());
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
    public double getTokenScale() {
        return tokenScale;
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

        removeTokenFromDrag(t);
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

    @Override
    public List<Token> getAllHighlightedTokens() {
        return new ArrayList<Token>(highlightedTokens);
    }

    @Override
    public void removeAllHighlightedTokens() {
        var toRemove = new ArrayList<>(highlightedTokens);
        toRemove.forEach(token -> removeToken(token));
    }

    public boolean isDragged(Token token) {
        for (TokenDragElement tokenDragElement : draggedTokens) {
            if (tokenDragElement.token().equals(token))
                return true;
        }
        return false;
    }

    public boolean removeTokenFromDrag(Token t) {
        return draggedTokens.removeIf(dragElement -> dragElement.token().equals(t));
    }
}
