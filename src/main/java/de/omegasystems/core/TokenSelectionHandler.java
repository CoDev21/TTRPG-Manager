package de.omegasystems.core;

import java.util.List;

public interface TokenSelectionHandler {

    public List<Token> getAllHighlightedTokens();
    public void removeAllHighlightedTokens();


}
