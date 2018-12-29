package ua.mobizon.exception;

public class MobizonException extends Exception {

    public MobizonException() {
    }

    public MobizonException(String message) {
        super(message);
    }

    public MobizonException(String message, Throwable cause) {
        super(message, cause);
    }

    public MobizonException(Throwable cause) {
        super(cause);
    }
}