package main.pt.ipleiria.estg.dei.exceptions;

public abstract class NoCriticalException extends RuntimeException {
    public NoCriticalException(String message, String from) {
        super("[Warnings] from: [" +from + "] - Reason: " + message);
    }
}
