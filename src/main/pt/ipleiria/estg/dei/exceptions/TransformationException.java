package main.pt.ipleiria.estg.dei.exceptions;

public class TransformationException extends NoCriticalException {
    public TransformationException(String module,String table, String message) {
        super("{" + module + "] {" + table + "}" +  message, "TransformationException");
    }
}
