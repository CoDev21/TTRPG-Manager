package de.omegasystems.core;

import java.awt.event.MouseEvent;
import java.util.List;

import de.omegasystems.utility.Observable;

public interface TokenHandler extends Observable<TokenHandler> {

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

    public int calculateImageSizeFor(Token t);

}
