package main.pt.ipleiria.estg.dei.exceptions;

public class ConnectionException extends CriticalException {
    public ConnectionException(String message) {
        super(message, "ConnectionException");
    }
}
