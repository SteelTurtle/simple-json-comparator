package org.gorillacorp.comparator.exception;

public class CustomJsonParseException extends RuntimeException {
    public CustomJsonParseException(String message, Exception exception) {
        super(message, exception);
    }
}
