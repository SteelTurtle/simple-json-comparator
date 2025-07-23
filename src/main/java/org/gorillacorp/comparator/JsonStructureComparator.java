package org.gorillacorp.comparator;

import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Main class for comparing JSON structures.
 * This class serves as the entry point for the application and orchestrates the comparison process.
 */
public non-sealed class JsonStructureComparator implements StructuredScopeExceptionHandler {
    private static final Logger log = getLogger(JsonStructureComparator.class);

    public static void main(String... args) {
        if (args.length != 2) {
            log.error("Usage: java -jar JsonStructureComparator.jar <file1> <file2>");
            System.exit(1);
        }
        var file1Path = args[0];
        var file2Path = args[1];
        log.info("Comparing JSON structures: {} vs {}", file1Path, file2Path);
        var startTime = System.currentTimeMillis();

        try {
            var areEqual = JsonComparatorOperations.compareJson(file1Path, file2Path);
            if (!areEqual) {
                log.info("✗ JSON structures are different");
                // Show detailed differences
                displayJsonComparisonReport(file1Path, file2Path);
            } else {
                log.info("✓ JSON structures are identical");
            }

            Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
            log.info("Comparison took {} seconds", duration.toSeconds());
            System.exit(0);
        } catch (IOException e) {
            log.error("Error reading JSON files: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            System.exit(1);
        }
    }


    /**
     * Displays a detailed comparison report of two JSON files.
     * This method compares the structure and values of the given JSON files
     * and generates a report highlighting the similarities and differences.
     *
     * @param file1Path the file path of the first JSON file
     * @param file2Path the file path of the second JSON file
     * @throws IOException if there is an issue reading the files
     */
    private static void displayJsonComparisonReport(String file1Path, String file2Path) throws IOException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var jsonNodes = JsonComparatorOperations.loadJsonNodes(file1Path, file2Path);
            var fields1MapSubtask = scope.fork(() -> JsonTraverser.extractFieldsWithValues(jsonNodes[0]));
            var fields2MapSubTask = scope.fork(() -> JsonTraverser.extractFieldsWithValues(jsonNodes[1]));
            scope.join();
            scope.throwIfFailed();
            var fields1Map = fields1MapSubtask.get();
            var fields2Map = fields2MapSubTask.get();
            var comparisonResult = JsonComparatorOperations.compareFieldsWithValues(
                file1Path,
                file2Path,
                fields1Map,
                fields2Map
            );
            ComparisonReporter.printDetailedComparisonTable(comparisonResult);
            ComparisonReporter.printSummaryTable(fields1Map.keySet(), fields2Map.keySet(), comparisonResult);
            ComparisonReporter.printFilePaths(file1Path, file2Path);
        } catch (ExecutionException | InterruptedException exception) {
            StructuredScopeExceptionHandler.handleStructuredTaskScopeException(exception);
        }
    }
}
