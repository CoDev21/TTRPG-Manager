package de.omegasystems.dataobjects;

public enum TokenSize {
    Tiny(0.5),
    Medium(1.0),
    Large(2.0),
    Giant(4.0),
    Gargantuan(8.0),
    
    ;
    private double scale;
    TokenSize(double scale) {
        this.scale = scale;
    };

    public double getScale() {
        return scale;
    }
}
