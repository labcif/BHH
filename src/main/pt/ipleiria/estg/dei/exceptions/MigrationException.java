package main.pt.ipleiria.estg.dei.exceptions;

public class MigrationException extends CriticalException {
    public MigrationException(String message) {
        super(message, "MigrationException");
    }
}
