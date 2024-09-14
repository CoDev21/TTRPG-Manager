package de.omegasystems.core;

import java.awt.Image;

import de.omegasystems.utility.Observable;

public interface GameCore extends Observable<GameCore> {

    public double getScale();

    public void setBackgroundImage(Image image);

    /**
     * DO NOT USE THIS FOR SETTING THE BACKGROUND
     * Use {@link #setBackgroundImage(Image)} instead!
     * @param backgroundComponent
     */
    @Deprecated
    public void setBackgroundComponent(Background backgroundComponent);

}
