package main.pt.ipleiria.estg.dei.events;

import java.util.ArrayList;
import java.util.List;

public class Subject {

    private List<Observer> observers = new ArrayList<Observer>();
    private String processFase;
    private String operation;

    public void register(Observer newObserver){
        observers.add(newObserver);
    }

    public void unregister(Observer deleteObserver){
        int observerIndex = observers.indexOf(deleteObserver);
        System.out.println("Observer " + (observerIndex + 1) + " deleted");
        observers.remove(deleteObserver);
    }

    public void notifyAllObservers(){
        for (Observer observer : observers) {
            observer.update(processFase, operation);
        }
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
        notifyAllObservers();
    }

    public String getProcessFase() {
        return processFase;
    }

    public void setProcessFase(String processFase) {
        this.processFase = processFase;
        notifyAllObservers();
    }
}
