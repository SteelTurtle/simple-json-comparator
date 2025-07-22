package org.gorillacorp.comparator.exception;

import java.io.IOException;

public class CustomJsonParseException extends RuntimeException {
    public CustomJsonParseException(String message, IOException exception) {
        super(message, exception);
    }
}
