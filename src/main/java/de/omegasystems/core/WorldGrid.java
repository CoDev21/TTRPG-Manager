package de.omegasystems.core;

import java.awt.Point;

public interface WorldGrid {

    /**
     * Takes a position in world space and returns the top left (smallest x and y)
     * corner coordinate from the cell that contains this point.
     * 
     * @param pos A position in world space
     * @return 
     */
    public Point getContainingCellOrigin(Point pos);

}
