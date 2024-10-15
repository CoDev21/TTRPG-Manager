package de.omegasystems.core;

import java.awt.Dimension;
import java.awt.Point;

import de.omegasystems.utility.Observable;

public interface WorldTranslationHandler extends Observable<WorldTranslationHandler> {

    public double getScale();

    public Dimension getOffset();

    public Point getWorldCoordinateFormUISpace(Point uiPoint);

}
