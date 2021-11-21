package de.piobyte.dymoprint.service.print;

public class PrinterNotFoundException extends Exception {

    public PrinterNotFoundException() {
        super();
    }

    public PrinterNotFoundException(final String message) {
        super(message);
    }

    public PrinterNotFoundException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
