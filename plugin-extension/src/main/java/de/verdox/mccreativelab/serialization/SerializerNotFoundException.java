package de.verdox.mccreativelab.serialization;

public class SerializerNotFoundException extends Exception {
    public SerializerNotFoundException() {
    }

    public SerializerNotFoundException(String message) {
        super(message);
    }

    public SerializerNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public SerializerNotFoundException(Throwable cause) {
        super(cause);
    }

    public SerializerNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
