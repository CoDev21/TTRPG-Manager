package de.omegasystems.dataobjects;

import java.util.Objects;

import de.omegasystems.utility.Observable;
import de.omegasystems.utility.Observer;
import de.omegasystems.utility.Observerhandler;

public abstract class AbstractAttributeHolder {

    public static class Property<T> implements Observable<T> {
        private Observerhandler<T> handler = new Observerhandler<>();
        private T val;

        protected Property(T defaultVal) {
            this.val = defaultVal;
        }

        public void setValue(T newVal) {
            if (Objects.equals(val, newVal))
                return;

            val = newVal;
            notifyObservers(val);
        }

        public T getValue() {
            return val;
        }

        @Override
        public void addObserver(Observer<T> obs) {
            handler.addObserver(obs);
        }

        @Override
        public void notifyObservers(T val) {
            handler.notifyObservers(val);
        }

        @Override
        public void removeObserver(Observer<T> obs) {
            handler.removeObserver(obs);
        }
    }

    public static class Action<T> implements Observable<T> {
        private Observerhandler<T> handler = new Observerhandler<>();

        private Property<Boolean> canTriggerAction = new Property<>(true);

        protected Action() {
        }

        public Property<Boolean> canTriggerAction() {
            return canTriggerAction;
        }

        public void triggerAction(T val) {
            notifyObservers(val);
        }

        @Override
        public void addObserver(Observer<T> obs) {
            handler.addObserver(obs);
        }

        @Override
        public void notifyObservers(T val) {
            if (canTriggerAction.getValue())
                handler.notifyObservers(val);
        }

        @Override
        public void removeObserver(Observer<T> obs) {
            handler.removeObserver(obs);
        }

    }
}
