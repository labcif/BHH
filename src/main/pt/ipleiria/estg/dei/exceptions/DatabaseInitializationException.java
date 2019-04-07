package main.pt.ipleiria.estg.dei.exceptions;

public class DatabaseInitializationException extends CriticalException {
    public DatabaseInitializationException(String message) {
        super(message, "DatabaseInitializationException");
    }
}
