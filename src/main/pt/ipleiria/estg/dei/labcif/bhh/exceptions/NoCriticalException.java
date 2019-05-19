package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public abstract class NoCriticalException extends RuntimeException {
    public NoCriticalException(String message, String from) {
        super("[Warnings] from: [" +from + "] - Reason: " + message);
    }
}
