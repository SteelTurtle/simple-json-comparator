package org.gorillacorp.comparator.processor;

import org.gorillacorp.comparator.exception.CustomJsonParseException;
import org.gorillacorp.comparator.model.DetailedFieldComparisonResult;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public interface StructuredScopeOperationsHandler {

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

    /**
     * Retrieves a detailed comparison result of fields between two JSON files.
     * <p>
     * This method loads JSON nodes from the given file paths, extracts their fields and values,
     * and performs a comparison to generate a detailed result. It utilizes a structured task scope
     * for concurrent execution and ensures synchronized task completion and error handling.
     *
     * @param file1Path the path to the first JSON file
     * @param file2Path the path to the second JSON file
     * @param scopeRef  the structured task scope used for concurrent execution and error supervision
     * @return a CompoundFieldsComparisonResults object containing the field mappings of both files
     * and the result of their detailed comparison
     * @throws IOException          if an I/O error occurs while loading the JSON files
     * @throws InterruptedException if the operation is interrupted during task execution
     * @throws ExecutionException   if an error occurs during task execution
     */
    static CompoundFieldsComparisonResults getGetDetailedFieldsComparisonResult(
        String file1Path,
        String file2Path,
        StructuredTaskScope.ShutdownOnFailure scopeRef
    ) throws IOException, InterruptedException, ExecutionException {
        var jsonNodes = JsonComparatorOperations.loadJsonNodes(file1Path, file2Path);
        var fields1MapSubtask = scopeRef.fork(() -> JsonTraverser.extractFieldsWithValues(jsonNodes[0]));
        var fields2MapSubTask = scopeRef.fork(() -> JsonTraverser.extractFieldsWithValues(jsonNodes[1]));
        scopeRef.join();
        scopeRef.throwIfFailed();
        var fields1Map = fields1MapSubtask.get();
        var fields2Map = fields2MapSubTask.get();
        var comparisonResult = JsonComparatorOperations.compareFieldsWithValues(
            file1Path,
            file2Path,
            fields1Map,
            fields2Map
        );
        return new CompoundFieldsComparisonResults(fields1Map, fields2Map, comparisonResult);
    }

    /**
     * Encapsulates the results of comparing fields and values between two data structures.
     * This record holds the mappings of fields for both input data structures and provides
     * a detailed comparison result.
     * <p>
     * The `fields1Map` and `fields2Map` represent the mappings of fields and their associated
     * values for the first and second data structures respectively. The `comparisonResult`
     * contains the detailed comparison of these fields, including information about common fields,
     * fields exclusive to each data structure, and discrepancies in field values.
     * <p>
     * It is primarily utilized in scenarios where two structured datasets, such as JSON files,
     * need to be compared for differences, similarities, and other analysis.
     */
    record CompoundFieldsComparisonResults(java.util.Map<String, String> fields1Map,
                                           java.util.Map<String, String> fields2Map,
                                           DetailedFieldComparisonResult comparisonResult) {
    }
}
