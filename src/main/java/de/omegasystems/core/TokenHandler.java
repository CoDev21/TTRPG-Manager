package de.omegasystems.core;

import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.util.List;

import de.omegasystems.utility.Observable;

public interface TokenHandler extends Observable<TokenHandler> {

    public double getTokenScale();

    public void addToken(Token t);

    public List<Token> getAllTokens();

    public void removeToken(Token t);

    public void notifyChange();

    public boolean hasToken(Token t);

    /**
     * This method can return a token based on a MouseEvent triggered on the
     * rendering component.<br>
     * It returns the most visible token (not overdrawn by others at the mouse
     * point).
     * 
     * @param e The Event causing the hover
     * @return The token under the Mouse or null if none where found
     */
    public Token getTokenFromPosition(MouseEvent e);

    /**
     * Retrieves a list of tokens that are within a specified rectangular selection
     * area.
     * The selection area is defined in World Space coordinates and needs to be
     * translated
     * using a WorldTranslationHandler to match the coordinate system of the tokens.
     *
     * @param selectionBox the rectangular area in World Space coordinates to
     *                     check for tokens.
     * @return a list of tokens that are within the specified selection area.
     */
    public List<Token> getTokensInArea(Rectangle r);

    public int calculateImageSizeFor(Token t);

}
