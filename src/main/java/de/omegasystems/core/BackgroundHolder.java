package de.omegasystems.core;

import java.awt.Image;

import de.omegasystems.utility.Observable;

public interface BackgroundHolder extends Observable<BackgroundHolder> {

    public double getScale();

    public void setBackgroundImage(Image image);

    public Renderer getRenderer();

}
