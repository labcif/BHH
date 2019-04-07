package main.pt.ipleiria.estg.dei.exceptions;

public class NotSupportedException extends NoCriticalException {
    public NotSupportedException(String message){
        super(message, "NotSupportedException");
    }
}
