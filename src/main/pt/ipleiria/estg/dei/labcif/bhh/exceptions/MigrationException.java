package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public class MigrationException extends CriticalException {
    public MigrationException(String message) {
        super(message, "MigrationException");
    }
}
