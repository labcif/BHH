package main.pt.ipleiria.estg.dei.events;

import org.sleuthkit.autopsy.ingest.IngestMessage;
import org.sleuthkit.autopsy.ingest.IngestServices;

import java.util.*;

public class EventManager {

    Map<String, List<Observer>> listeners = new HashMap<>();

    public EventManager(String... operations) {
        for (String operation : operations) {
            this.listeners.put(operation, new ArrayList<>());
        }
    }

    public void subscribe(String eventType, Observer listener) {
        List<Observer> users = listeners.get(eventType);
        users.add(listener);
    }

    public void unsubscribe(String eventType, Observer listener) {
        List<Observer> users = listeners.get(eventType);
        users.remove(listener);
    }

    public void notify(String eventType) {
        List<Observer> users = listeners.get(eventType);
        for (Observer listener : users) {
            listener.update(eventType);
        }
    }
}
