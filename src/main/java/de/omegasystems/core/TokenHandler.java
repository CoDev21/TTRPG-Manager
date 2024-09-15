package de.omegasystems.core;

import java.util.List;

public interface TokenHandler {

    public void addToken(Token t);

    public List<Token> getAllTokens();

    public void removeToken(Token t);

    public void notifyChange();

}
