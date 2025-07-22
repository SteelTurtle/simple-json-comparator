package org.gorillacorp.comparator;

import org.slf4j.Logger;

import java.io.IOException;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Main class for comparing JSON structures.
 * This class serves as the entry point for the application and orchestrates the comparison process.
 */
public class JsonStructureComparator {
    private static final Logger log = getLogger(JsonStructureComparator.class);

    public static void main(String... args) {
        if (args.length != 2) {
            log.error("Usage: java -jar JsonStructureComparator.jar <file1> <file2>");
            System.exit(1);
        }
        var file1Path = args[0];
        var file2Path = args[1];
        log.info("Comparing JSON structures: {} vs {}", file1Path, file2Path);

        try {
            var areEqual = JsonComparatorOperations.compareJson(file1Path, file2Path);
            if (areEqual) {
                log.info("✓ JSON structures are identical");
                System.exit(0);
            } else {
                log.info("✗ JSON structures are different");
                // Show detailed differences
                displayJsonComparisonReport(file1Path, file2Path);
                System.exit(1);
            }
        } catch (IOException e) {
            log.error("Error reading JSON files: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Shows detailed differences between two JSON files.
     *
     * @param file1Path Path to the first JSON file
     * @param file2Path Path to the second JSON file
     * @throws IOException if there is an error reading the files
     */
    private static void displayJsonComparisonReport(String file1Path, String file2Path) throws IOException {
        var jsonNodes = JsonComparatorOperations.loadJsonNodes(file1Path, file2Path);
        var fields1Map = JsonTraverser.extractFieldsWithValues(jsonNodes[0]);
        var fields2Map = JsonTraverser.extractFieldsWithValues(jsonNodes[1]);

        var comparisonResult = JsonComparatorOperations.compareFieldsWithValues(fields1Map, fields2Map);
        ComparisonReporter.printDetailedComparisonTable(comparisonResult);
        ComparisonReporter.printSummaryTable(fields1Map.keySet(), fields2Map.keySet(), comparisonResult);
        ComparisonReporter.printFilePaths(file1Path, file2Path);
    }
}
