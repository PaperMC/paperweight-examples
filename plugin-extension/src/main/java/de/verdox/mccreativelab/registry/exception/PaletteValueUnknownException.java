package de.verdox.mccreativelab.registry.exception;

public class PaletteValueUnknownException extends Exception{
    public PaletteValueUnknownException() {
    }

    public PaletteValueUnknownException(String message) {
        super(message);
    }

    public PaletteValueUnknownException(String message, Throwable cause) {
        super(message, cause);
    }

    public PaletteValueUnknownException(Throwable cause) {
        super(cause);
    }

    public PaletteValueUnknownException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
