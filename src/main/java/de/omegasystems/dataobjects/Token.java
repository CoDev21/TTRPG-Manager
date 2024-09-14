package de.omegasystems.dataobjects;

import java.util.UUID;
import java.io.File;

public class Token {
    private UUID id;
    private File pictureFile;
    private String name;
    private String description;
    private TokenSize size;
    private int initiative;
    private int movement;
    private Friendlieness friendStatus;

    public UUID getID() {
        return this.id;
    }

    public String getDescription() {
        return description;
    }

    public UUID getId() {
        return id;
    }

    public int getInitiative() {
        return initiative;
    }

    public String getName() {
        return name;
    }

    public File getPictureFile() {
        return pictureFile;
    } 

    public TokenSize getSize() {
        return size;
    }

    public Friendlieness getFriendlieness() {
        return friendStatus;
    }

    public int getMovement() {
        return movement;
    }

}
