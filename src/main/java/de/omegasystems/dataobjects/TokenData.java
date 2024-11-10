package de.omegasystems.dataobjects;

import java.io.File;

import de.omegasystems.utility.AbstractAttributeHolder.Property;
import de.omegasystems.utility.Observable;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public class TokenData implements Observable<TokenData> {
    private Property<File> pictureFile;
    private Property<String> name;
    private Property<String> description;
    private Property<TokenSize> sizeClass;
    private Property<Double> size;
    private Property<Integer> initiative;
    private Property<String> movement;
    private Property<Friendlieness> friendStatus;

    private Observerhandler<TokenData> observerhandler = new Observerhandler<>();

    public TokenData() {
        // register itself as a property owner
        pictureFile = new Property<File>(this, null);
        name = new Property<String>(this, "Name...");
        description = new Property<String>(this, "Your Description");
        initiative = new Property<Integer>(this, 0);
        movement = new Property<String>(this, "Movement");
        friendStatus = new Property<Friendlieness>(this, Friendlieness.Neutral);

        // We need to couple these to values so that the returned token only cares about
        // its own size as a double, but the User can input a predifined input (the enum
        // TokenSize) so that the tokens can have a uniform appeareance
        sizeClass = new Property<TokenSize>(this, TokenSize.Medium);
        size = new Property<Double>(this, TokenSize.Medium.getScale());

        sizeClass.addObserver(newVal -> size.setValue(newVal.getScale()));
        size.addObserver(newVal -> sizeClass.setValue(TokenSize.getClosesTokenSize(newVal)));
    }

    public Property<String> getDescription() {
        return description;
    }

    public Property<Friendlieness> getFriendStatus() {
        return friendStatus;
    }

    public Property<Integer> getInitiative() {
        return initiative;
    }

    public Property<String> getMovement() {
        return movement;
    }

    public Property<String> getName() {
        return name;
    }

    public Property<File> getPictureFile() {
        return pictureFile;
    }

    public Property<TokenSize> getSizeClass() {
        return sizeClass;
    }

    public Property<Double> getSize() {
        return size;
    }

    @Override
    public void addObserver(Observer<TokenData> obs) {
        observerhandler.addObserver(obs);
    }

    @Override
    public void notifyObservers(TokenData value) {
        observerhandler.notifyObservers(value);
    }

    @Override
    public void removeObserver(Observer<TokenData> obs) {
        observerhandler.removeObserver(obs);
    }

}
