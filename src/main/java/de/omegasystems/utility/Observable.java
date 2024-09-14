package de.omegasystems.utility;

public interface Observable<T> {

    public void addObserver(Observer<T> obs);
    public void removeObserver(Observer<T> obs);
    public void notifyObservers(T value);
} 
