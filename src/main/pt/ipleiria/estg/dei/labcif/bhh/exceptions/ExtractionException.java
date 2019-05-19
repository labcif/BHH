package main.pt.ipleiria.estg.dei.labcif.bhh.exceptions;

public class ExtractionException extends NoCriticalException {
    public ExtractionException(String module, String table,String message) {
        super("[" + module + "] {" + table + "}" + message, "ExtractionException");
    }
}
