package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public abstract class CriticalException extends Exception {
    CriticalException(String message, String from) {
        super("[Critical Exception] from: [" +from + "] - Reason: " + message);
    }
}
