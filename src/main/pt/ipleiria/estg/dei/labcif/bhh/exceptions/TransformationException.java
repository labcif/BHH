package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public class TransformationException extends NoCriticalException {
    public TransformationException(String module,String table, String message) {
        super("{" + module + "] {" + table + "}" +  message, "TransformationException");
    }
}
