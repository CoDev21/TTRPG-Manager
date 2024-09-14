package de.omegasystems.utility;

import java.util.ArrayList;
import java.util.List;

public class Observerhandler<T> implements Observable<T> {

    private List<Observer<T>> observers = new ArrayList<>();
    
    public boolean hasObservers() {
        return !observers.isEmpty();
    }


    @Override
    public void addObserver(Observer<T> obs) {
        if (!observers.contains(obs))
            observers.add(obs);
    }

    @Override
    public void notifyObservers(T value) {
        observers.forEach(obs -> obs.update(value));
    }

    @Override
    public void removeObserver(Observer<T> obs) {
        observers.remove(obs);
    }

}
