package de.omegasystems.dataobjects;

public enum TokenSize {
    Tiny(0.25),
    Small(0.5),
    Medium(1.0),
    Large(2.0),
    Giant(4.0),

    ;

    public static TokenSize getClosesTokenSize(double size) {
        double closestDistance = Double.MAX_VALUE;
        TokenSize ret = null;
        for (TokenSize sizeClass : values()) {
            double dist = Math.abs(sizeClass.getScale() - size);

            if (dist >= closestDistance)
                continue;

            closestDistance = dist;
            ret = sizeClass;
        }
        return ret;
    }

    private double scale;

    TokenSize(double scale) {
        this.scale = scale;
    };

    public double getScale() {
        return scale;
    }
}
