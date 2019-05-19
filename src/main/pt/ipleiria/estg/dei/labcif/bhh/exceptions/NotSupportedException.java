package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public class NotSupportedException extends NoCriticalException {
    public NotSupportedException(String message){
        super(message, "NotSupportedException");
    }
}
