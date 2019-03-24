package main.pt.ipleiria.estg.dei.exceptions;

public class GenerateReportException extends Exception {
    private String uniqueErrorID;
    private String errorMsg;

    /**
     * Instantiates a new GenerateReportException given an error code, unique error id and error message.
     * @param uniqueErrorID Unique error ID to easily identify the problem.
     * @param errorMsg .
     */
    public GenerateReportException(String uniqueErrorID, String errorMsg){
        this.uniqueErrorID = uniqueErrorID;
        this.errorMsg = errorMsg;
    }

    /**
     * @see Exception:getMessage
     */
    @Override
    public String getMessage() {
        return String.format("[%s] %s",uniqueErrorID,errorMsg);
    }

    /**
     * Gets the unique error ID.
     * @return the unique error ID.
     */
    public String getUniqueErrorID() {
        return uniqueErrorID;
    }

    /**
     * Sets the unique error ID.
     * @param uniqueErrorID New value for the unique error ID.
     */
    public void setUniqueErrorID(String uniqueErrorID) {
        this.uniqueErrorID = uniqueErrorID;
    }


    /**
     * Gets the error message.
     * @return The error message.
     */
    public String getErrorMsg() {
        return errorMsg;
    }

    /**
     * Sets the error message.
     * @param errorMsg New value for the error message.
     */
    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
