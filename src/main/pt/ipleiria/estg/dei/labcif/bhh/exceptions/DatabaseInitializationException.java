package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public class DatabaseInitializationException extends CriticalException {
    public DatabaseInitializationException(String message) {
        super(message, "DatabaseInitializationException");
    }
}
