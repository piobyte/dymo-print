package de.piobyte.dymoprint.service.print;

public class InvalidParameterException extends Exception {

    public InvalidParameterException() {
        super();
    }

    public InvalidParameterException(final String message) {
        super(message);
    }

    public InvalidParameterException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
