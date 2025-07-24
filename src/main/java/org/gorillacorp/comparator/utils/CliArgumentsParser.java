package org.gorillacorp.comparator.utils;

import org.gorillacorp.comparator.output.ComparisonCsvExporter;
import org.gorillacorp.comparator.output.ComparisonTerminalVisualizer;
import org.gorillacorp.comparator.processor.JsonComparatorOperations;
import org.slf4j.Logger;

import java.io.IOException;
import java.time.Duration;

import static org.slf4j.LoggerFactory.getLogger;

public final class CliArgumentsParser {

    public static final String USAGE =
        "Usage: java -jar JsonStructureComparator.jar <file1> <file2> [-export-to-csv <output.csv>]";
    private static final Logger log = getLogger(CliArgumentsParser.class);

    private CliArgumentsParser() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Parses command-line arguments to validate the '-export-to-csv' option
     * and invokes the CSV export process comparing two JSON files.
     * This method ensures proper formatting of the CSV output file path and handles errors appropriately.
     *
     * @param args      the array of command-line arguments
     * @param file1Path the file path of the first JSON file to compare
     * @param file2Path the file path of the second JSON file to compare
     */
    public static void parseExportToCsvOptionAndRun(String[] args, String file1Path, String file2Path) {
        if (args.length == 3) {
            log.error("Missing CSV output file path after '-export-to-csv'.");
            log.error(USAGE);
            System.exit(1);
        }

        if (!"-export-to-csv".equals(args[2])) {
            log.error("Invalid third argument '{}'. Expected '-export-to-csv'.", args[2]);
            log.error(USAGE);
            System.exit(1);
        }

        var csvOutputPath = args[3];

        // Validate that the file path ends with .csv
        if (!csvOutputPath.toLowerCase().endsWith(".csv")) {
            log.error("Invalid CSV file path '{}'. The file must end with the '.csv' extension.", csvOutputPath);
            System.exit(1);
        }

        log.info("Comparing JSON structures: {} vs. {} (exporting to CSV: {})", file1Path, file2Path, csvOutputPath);
        var startTime = System.currentTimeMillis();

        try {
            // Export to CSV instead of terminal output
            ComparisonCsvExporter.exportJsonComparisonReportToCsv(file1Path, file2Path, csvOutputPath);
            log.info("✓ Comparison report exported to CSV: {}", csvOutputPath);

            Duration duration = Duration.ofMillis(System.currentTimeMillis() - startTime);
            log.info("Export took {} seconds", duration.toSeconds());
            System.exit(0);
        } catch (IOException e) {
            log.error("Error processing JSON files: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            log.error("Unexpected error: {}", e.getMessage());
            System.exit(1);
        }
    }

    /**
     * Parses the mandatory file path options required for comparing two JSON files
     * and performs the comparison operation. The method outputs the comparison
     * result, logs differences if the structures are not identical, and reports
     * the runtime duration.
     *
     * @param file1Path the file path of the first JSON file to compare
     * @param file2Path the file path of the second JSON file to compare
     */
    public static void parseMandatoryOptionsAndRun(String file1Path, String file2Path) {
        // Original behavior for 2 arguments
        log.info("Comparing JSON structures: {} vs {}", file1Path, file2Path);
        var startTime = System.currentTimeMillis();

        try {
            var areEqual = JsonComparatorOperations.compareJson(file1Path, file2Path);
            if (!areEqual) {
                log.info("✗ JSON structures are different");
                // Show detailed differences
                ComparisonTerminalVisualizer.displayJsonComparisonReport(file1Path, file2Path);
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
}
