package de.omegasystems.dataobjects;

import java.awt.Color;

public enum Friendlieness {

    Hidden(new Color(0, 0, 0, 0), new Color(0, 0, 0, 50)),
    Friend(new Color(30, 160, 30), new Color(30, 200, 30)),
    Neutral(new Color(0, 130, 150), new Color(0, 220, 220)),
    Enemy(new Color(180, 10, 10), new Color(255, 80, 10)),
    ;

    private Color outlineColor;
    private Color highlightColor;

    public Color getOutline() {
        return outlineColor;
    }

    public Color getHighlight() {
        return highlightColor;
    }

    private Friendlieness(Color outline, Color highlight) {
        this.outlineColor = outline;
        this.highlightColor = highlight;
    }

}
