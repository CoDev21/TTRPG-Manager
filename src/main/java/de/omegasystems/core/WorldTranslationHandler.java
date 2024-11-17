package de.omegasystems.core;

import java.awt.Point;
import java.awt.geom.Point2D;

public interface WorldTranslationHandler {

    public double getScale();

    public Point2D.Double getOffset();

    public Point getWorldCoordinateFormUISpace(Point uiPoint);

    public void checkBounds();

}
