package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public class ConnectionException extends CriticalException {
    public ConnectionException(String message) {
        super(message, "ConnectionException");
    }
}
