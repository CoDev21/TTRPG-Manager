package de.omegasystems.dataobjects;

public enum TokenSize {
    TINY(0.5),
    MEDIUM(1.0),
    LARGE(2.0),
    GIANT(4.0),
    GARGANTUAN(8.0),
    
    ;
    private double scale;
    TokenSize(double scale) {
        this.scale = scale;
    };

    public double getScale() {
        return scale;
    }
}
