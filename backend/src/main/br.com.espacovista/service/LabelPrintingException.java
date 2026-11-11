package service;

/** Base runtime error for a failed label printing operation. */
public class LabelPrintingException extends RuntimeException {
    public LabelPrintingException(String message, Throwable cause) {
        super(message, cause);
    }
}
