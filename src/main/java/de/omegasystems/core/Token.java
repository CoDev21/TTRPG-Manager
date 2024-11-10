package de.omegasystems.core;

import java.awt.Image;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.UUID;

import javax.imageio.ImageIO;

import de.omegasystems.App;
import de.omegasystems.dataobjects.Friendlieness;
import de.omegasystems.dataobjects.TokenData;
import de.omegasystems.dataobjects.TokenSize;

public class Token {

    private static final Image placeholderImage;
    static {
        try {
            placeholderImage = ImageIO.read(App.getInstance().loadResourceFile("img/Liron.jpg"));
        } catch (Exception e) {
            App.getInstance().openErrorDialog("Couldn't load the placeholder image for Tokens!");
            throw new IllegalStateException("Couldn't load Backup Token Image", e);
        }
    }

    public static Image getPlaceholderImage() {
        return placeholderImage;
    }

    private UUID id;
    private File pictureFile = null;
    private Image image;
    private String name = "";
    private String description = "";
    private double size = TokenSize.Large.getScale();
    private Integer initiative = 0;
    private String movement = "0";
    private Friendlieness friendStatus = Friendlieness.Neutral;

    private Point2D.Double position = new Point2D.Double();

    private TokenHandler tokenHandler;

    public Token(TokenData tokenData, TokenHandler tokenHandler) {
        this.id = UUID.randomUUID();
        this.tokenHandler = tokenHandler;
        updateAllValues(tokenData);
    }

    public void updateAllValues(TokenData tokenData) {
        this.pictureFile = tokenData.getPictureFile().getValue();
        this.name = tokenData.getName().getValue();
        this.description = tokenData.getDescription().getValue();
        this.size = tokenData.getSize().getValue();
        this.initiative = tokenData.getInitiative().getValue();
        this.movement = tokenData.getMovement().getValue();
        this.friendStatus = tokenData.getFriendStatus().getValue();
        loadImage();
        tokenHandler.notifyChange();
    }

    private void loadImage() {
        if (pictureFile == null) {
            this.image = getPlaceholderImage();
            return;
        }

        try {
            image = ImageIO.read(pictureFile);
        } catch (Exception e) {
            System.err.println(
                    "[Token] An error occcured while trying to load image '" + pictureFile.getAbsolutePath() + "'");
            e.printStackTrace();

        }
    }

    public TokenData createDataObject() {
        var data = new TokenData();
        data.getPictureFile().setValue(pictureFile);
        data.getName().setValue(name);
        data.getDescription().setValue(description);
        data.getSize().setValue(size);
        data.getInitiative().setValue(initiative);
        data.getMovement().setValue(movement);
        data.getFriendStatus().setValue(friendStatus);
        return data;
    }

    public String getDescription() {
        return description;
    }

    public Friendlieness getFriendStatus() {
        return friendStatus;
    }

    public UUID getUUId() {
        return id;
    }

    public Integer getInitiative() {
        return initiative;
    }

    public String getMovement() {
        return movement;
    }

    public String getName() {
        return name;
    }

    public File getPictureFile() {
        return pictureFile;
    }

    public double getSize() {
        return size;
    }

    // A double indicating the position of the current token between 0 and mapsize
    public Point2D.Double getPosition() {
        return (Point2D.Double) position;
    }

    public void setPosition(Point2D.Double pos) {
        if (pos == null || pos.x < 0 || pos.y < 0)
            return;
        this.position = pos;
        tokenHandler.notifyChange();
    }

    /**
     * Returns this tokens Image or a backup image, meaning it cannot be
     * null. @NonNull
     * 
     * @return Always a valid image
     */
    public Image getImage() {
        if (image != null)
            return image;
        else
            return getPlaceholderImage();
    }

    public TokenHandler getTokenHandler() {
        return tokenHandler;
    }

    @Override
    public String toString() {
        return "Token (UUID: " + getUUId() + ")";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Token other)
            return getUUId().equals(other.getUUId());
        else
            return false;
    }

    @Override
    public int hashCode() {
        return getUUId().hashCode();
    }

}
