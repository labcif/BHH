package main.pt.ipleiria.estg.dei.events;

public interface Observer {
    public abstract void update(String processFase, String operation);
}
