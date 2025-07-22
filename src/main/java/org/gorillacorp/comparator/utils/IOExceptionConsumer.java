package org.gorillacorp.comparator.utils;

import org.gorillacorp.comparator.exception.CustomJsonParseException;

import java.io.IOException;
import java.util.function.Consumer;


/**
 * A functional interface similar to {@link Consumer}, but that allows throwing {@link IOException}.
 * <p>
 * The primary use of this interface is to enable lambda or method references that throw
 * {@link IOException} within contexts that expect a {@link Consumer}.
 *
 * @param <T> the type of the input to the operation
 */
@FunctionalInterface
public interface IOExceptionConsumer<T> {
    /**
     * Wraps an {@link IOExceptionConsumer} into a standard {@link Consumer} by handling the checked
     * {@link IOException} and rethrowing it as an unchecked {@link RuntimeException}.
     *
     * @param <T>      the type of the input to the operation
     * @param consumer the {@link IOExceptionConsumer} to be wrapped into a {@link Consumer}
     * @return a {@link Consumer} that performs the operation of the given {@link IOExceptionConsumer}
     * and handles {@link IOException} as a {@link RuntimeException}
     */
    static <T> Consumer<T> wrapIOException(IOExceptionConsumer<T> consumer) {
        return item -> {
            try {
                consumer.accept(item);
            } catch (IOException e) {
                throw new CustomJsonParseException("IOException during JSON traversal", e);
            }
        };
    }

    void accept(T t) throws IOException;

}
