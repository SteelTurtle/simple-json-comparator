package org.gorillacorp.comparator;

import org.gorillacorp.comparator.exception.CustomJsonParseException;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

public sealed interface StructuredScopeExceptionHandler
    permits JsonComparatorOperations, JsonStructureComparator {

    /**
     * Handles exceptions that might occur during structured task scope execution.
     * This method properly handles ExecutionException and InterruptedException,
     * converting them to appropriate custom exceptions while preserving the original cause.
     *
     * @param exception the exception to handle (ExecutionException or InterruptedException)
     * @throws IOException              if the underlying cause is an IOException
     * @throws CustomJsonParseException for all other exceptions
     */
    static void handleStructuredTaskScopeException(Exception exception) throws IOException {
        switch (exception) {
            case ExecutionException executionException -> {
                var cause = executionException.getCause();
                switch (cause) {
                    case IOException ioException -> throw ioException;
                    case RuntimeException _ -> throw new CustomJsonParseException(
                        "Runtime error during JSON processing: %s".formatted(cause.getMessage()),
                        executionException
                    );
                    default -> throw new CustomJsonParseException(
                        "Unexpected error during JSON processing: %s".formatted(cause.getClass().getSimpleName()),
                        executionException
                    );
                }
            }
            case InterruptedException interruptedException -> {
                Thread.currentThread().interrupt();
                throw new CustomJsonParseException(
                    "JSON comparison was interrupted",
                    interruptedException
                );
            }
            default -> throw new CustomJsonParseException(
                "Unexpected exception type: %s".formatted(exception.getClass().getSimpleName()),
                exception
            );
        }
    }
}
