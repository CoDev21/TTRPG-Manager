package de.omegasystems.utility;

public interface Observer<T> {

    public void update(T newVal);

}