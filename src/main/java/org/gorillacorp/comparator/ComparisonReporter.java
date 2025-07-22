package org.gorillacorp.comparator;

import org.gorillacorp.comparator.model.DetailedFieldComparisonResult;
import org.gorillacorp.comparator.model.DetailedFieldStatus;
import org.slf4j.Logger;

import java.util.Set;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Utility class for formatting and displaying JSON comparison results.
 *
 * <p>This class uses a set of constants to control the formatting of tables:
 * <ul>
 *   <li>Column widths for detailed table: {@code DETAILED_FIELD_NAME_WIDTH},
 *       {@code DETAILED_FILE_PRESENCE_WIDTH}, {@code DETAILED_VALUE_WIDTH},
 *       {@code DETAILED_STATUS_WIDTH}, {@code DETAILED_DIFFERENCE_WIDTH}</li>
 *   <li>Column widths for summary table: {@code SUMMARY_METRIC_WIDTH},
 *       {@code SUMMARY_FILE_WIDTH}, {@code SUMMARY_DIFFERENCE_WIDTH}</li>
 *   <li>Truncation thresholds: {@code FIELD_NAME_MAX_LENGTH}, {@code FIELD_NAME_TRUNCATE_LENGTH},
 *       {@code VALUE_MAX_LENGTH}, {@code VALUE_TRUNCATE_LENGTH}</li>
 * </ul>
 *
 * <p>To adjust the table formatting, modify these constants. The table widths
 * ({@code DETAILED_TABLE_WIDTH} and {@code SUMMARY_TABLE_WIDTH}) are automatically
 * calculated based on the column widths and separators.
 */
public final class ComparisonReporter {

    private static final Logger log = getLogger(ComparisonReporter.class);
    private static final int SEPARATOR_WIDTH = 3; // Length of " | "
    // Detailed table constants
    private static final int DETAILED_FIELD_NAME_WIDTH = 40;
    private static final int DETAILED_FILE_PRESENCE_WIDTH = 8;
    private static final int DETAILED_VALUE_WIDTH = 20;
    private static final int DETAILED_STATUS_WIDTH = 15;
    private static final int DETAILED_DIFFERENCE_WIDTH = 20;
    // Calculate detailed table width based on column widths and separators
    private static final int DETAILED_TABLE_WIDTH =
        DETAILED_FIELD_NAME_WIDTH +
        (DETAILED_FILE_PRESENCE_WIDTH * 2) +
        (DETAILED_VALUE_WIDTH * 2) +
        DETAILED_STATUS_WIDTH +
        DETAILED_DIFFERENCE_WIDTH +
        (SEPARATOR_WIDTH * 6); // 6 separators
    // Field name truncation constants
    private static final int FIELD_NAME_MAX_LENGTH = 37;
    private static final int FIELD_NAME_TRUNCATE_LENGTH = 34;
    // Value truncation constants
    private static final int VALUE_MAX_LENGTH = 17;
    private static final int VALUE_TRUNCATE_LENGTH = 14;
    // Summary table constants
    private static final int SUMMARY_METRIC_WIDTH = 40;
    private static final int SUMMARY_FILE_WIDTH = 15;
    private static final int SUMMARY_DIFFERENCE_WIDTH = 20;
    // Calculate summary table width based on column widths and separators
    private static final int SUMMARY_TABLE_WIDTH =
        SUMMARY_METRIC_WIDTH +
        (SUMMARY_FILE_WIDTH * 2) +
        SUMMARY_DIFFERENCE_WIDTH +
        (SEPARATOR_WIDTH * 3); // 3 separators

    private ComparisonReporter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Prints a detailed comparison table of fields with values.
     *
     * @param result The detailed field comparison result
     */
    public static void printDetailedComparisonTable(DetailedFieldComparisonResult result) {
        log.info("\n{}", "=".repeat(DETAILED_TABLE_WIDTH));
        log.info("DETAILED FIELD COMPARISON TABLE");
        log.info("=".repeat(DETAILED_TABLE_WIDTH));
        log.info(String.format(
            "%-" + DETAILED_FIELD_NAME_WIDTH + "s | %-" + DETAILED_FILE_PRESENCE_WIDTH + "s | %-" +
            DETAILED_FILE_PRESENCE_WIDTH + "s | %-" + DETAILED_VALUE_WIDTH + "s | %-" +
            DETAILED_VALUE_WIDTH + "s | %-" + DETAILED_STATUS_WIDTH + "s | %-" + DETAILED_DIFFERENCE_WIDTH + "s",
            "Field Name", "File 1", "File 2", "Value in File 1", "Value in File 2", "Status", "Difference"));
        log.info("-".repeat(DETAILED_TABLE_WIDTH));

        for (DetailedFieldStatus fieldStatus : result.fieldStatusList()) {
            String truncatedField = fieldStatus.fieldName().length() > FIELD_NAME_MAX_LENGTH
                ? fieldStatus.fieldName().substring(0, FIELD_NAME_TRUNCATE_LENGTH) + "..."
                : fieldStatus.fieldName();

            String truncatedValue1 = truncateValue(fieldStatus.value1());
            String truncatedValue2 = truncateValue(fieldStatus.value2());

            log.info(String.format(
                "%-" + DETAILED_FIELD_NAME_WIDTH + "s | %-" + DETAILED_FILE_PRESENCE_WIDTH + "s | %-" +
                DETAILED_FILE_PRESENCE_WIDTH + "s | %-" + DETAILED_VALUE_WIDTH + "s | %-" +
                DETAILED_VALUE_WIDTH + "s | %-" + DETAILED_STATUS_WIDTH + "s | %-" + DETAILED_DIFFERENCE_WIDTH + "s",
                truncatedField,
                fieldStatus.inFile1() ? "Yes" : "No",
                fieldStatus.inFile2() ? "Yes" : "No",
                truncatedValue1 != null ? truncatedValue1 : "-",
                truncatedValue2 != null ? truncatedValue2 : "-",
                fieldStatus.status(),
                fieldStatus.difference()));
        }
    }

    /**
     * Truncates a value string for display.
     *
     * @param value The value to truncate
     * @return The truncated value
     */
    private static String truncateValue(String value) {
        if (value == null) return null;
        return value.length() > VALUE_MAX_LENGTH ? value.substring(0, VALUE_TRUNCATE_LENGTH) + "..." : value;
    }

    /**
     * Prints a summary table of the comparison results.
     *
     * @param fields1 Set of fields from the first JSON file
     * @param fields2 Set of fields from the second JSON file
     * @param result  The detailed field comparison result
     */
    public static void printSummaryTable(Set<String> fields1, Set<String> fields2,
                                         DetailedFieldComparisonResult result) {
        log.info("=".repeat(SUMMARY_TABLE_WIDTH));
        log.info("SUMMARY");
        log.info("=".repeat(SUMMARY_TABLE_WIDTH));
        log.info(String.format(
            "%-" + SUMMARY_METRIC_WIDTH + "s | %-" + SUMMARY_FILE_WIDTH + "s | %-" +
            SUMMARY_FILE_WIDTH + "s | %-" + SUMMARY_DIFFERENCE_WIDTH + "s",
            "Metric", "File 1", "File 2", "Difference"));
        log.info("-".repeat(SUMMARY_TABLE_WIDTH));

        log.info(String.format(
            "%-" + SUMMARY_METRIC_WIDTH + "s | %-" + SUMMARY_FILE_WIDTH + "d | %-" +
            SUMMARY_FILE_WIDTH + "d | %-" + SUMMARY_DIFFERENCE_WIDTH + "s",
            "Total fields",
            fields1.size(),
            fields2.size(),
            fields1.size() == fields2.size() ? "Same" : String.valueOf(Math.abs(fields1.size() - fields2.size()))));

        log.info(String.format(
            "%-" + SUMMARY_METRIC_WIDTH + "s | %-" + SUMMARY_FILE_WIDTH + "d | %-" +
            SUMMARY_FILE_WIDTH + "d | %-" + SUMMARY_DIFFERENCE_WIDTH + "d",
            "Common fields",
            result.commonFields(),
            result.commonFields(),
            0));

        log.info(String.format(
            "%-" + SUMMARY_METRIC_WIDTH + "s | %-" + SUMMARY_FILE_WIDTH + "d | %-" +
            SUMMARY_FILE_WIDTH + "d | %-" + SUMMARY_DIFFERENCE_WIDTH + "s",
            "Unique fields",
            result.onlyInFile1(),
            result.onlyInFile2(),
            "N/A"));

        log.info("=".repeat(SUMMARY_TABLE_WIDTH));
    }

    /**
     * Prints the file paths.
     *
     * @param file1Path Path to the first JSON file
     * @param file2Path Path to the second JSON file
     */
    public static void printFilePaths(String file1Path, String file2Path) {
        log.info("\nFILE PATHS:");
        log.info("File 1: {}", file1Path);
        log.info("File 2: {}", file2Path);
    }
}
