package com.naoty.gradebook.event;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * Bus d'événements interne basé sur PropertyChangeSupport.
 * Permet aux controllers de réagir aux changements de données
 * sans connaître la source (service / repo).
 *
 * <p>Topics conventionnels : "students.changed", "subjects.changed", "grades.changed".
 */
public class EventBus {

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);

    public void subscribe(String topic, PropertyChangeListener listener) {
        support.addPropertyChangeListener(topic, listener);
    }

    public void unsubscribe(String topic, PropertyChangeListener listener) {
        support.removePropertyChangeListener(topic, listener);
    }

    public void publish(String topic, Object payload) {
        support.firePropertyChange(topic, null, payload);
    }
}
