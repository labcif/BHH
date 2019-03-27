package main.pt.ipleiria.estg.dei.events;

public class EtlObserver implements Observer {

    private String processFase;
    private String operation;

    private static int observerIDTraker = 0;

    private int observerID;

    private Subject subject;

    public EtlObserver(Subject subject) {
        this.subject = subject;
        this.observerID = ++observerIDTraker;
        System.out.println("New Observer " + this.observerID);

        subject.register(this);
    }

    @Override
    public void update(String processFase, String operation) {
        this.processFase = processFase;
        this.operation = operation;
    }

    public void printState(){
        System.out.println("Fase " + processFase + " Operation " + operation );
    }
}
