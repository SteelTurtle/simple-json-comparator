package org.gorillacorp.comparator;

import org.gorillacorp.comparator.model.DetailedFieldComparisonResult;
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
    private static final int DETAILED_FILE_PRESENCE_MIN_WIDTH = 8; // Minimum width for file columns
    private static final int DETAILED_FILE_PRESENCE_MAX_WIDTH = 25; // Maximum width for file columns
    private static final int DETAILED_VALUE_WIDTH = 20;
    private static final int DETAILED_STATUS_WIDTH = 15;
    private static final int DETAILED_DIFFERENCE_WIDTH = 20;

    // Field name truncation constants
    private static final int FIELD_NAME_MAX_LENGTH = 37;
    private static final int FIELD_NAME_TRUNCATE_LENGTH = 34;
    // Value truncation constants
    private static final int VALUE_MAX_LENGTH = 17;
    private static final int VALUE_TRUNCATE_LENGTH = 14;
    // File name truncation constants for column headers
    private static final int FILE_NAME_MAX_LENGTH = DETAILED_FILE_PRESENCE_MAX_WIDTH - 10; // Account for "File 1 ()" format
    private static final int FILE_NAME_TRUNCATE_LENGTH = FILE_NAME_MAX_LENGTH - 3; // Account for "..."

    // Summary table constants
    private static final int SUMMARY_METRIC_WIDTH = 40;
    private static final int SUMMARY_FILE_MIN_WIDTH = 15; // Minimum width for summary file columns
    private static final int SUMMARY_FILE_MAX_WIDTH = 30; // Maximum width for summary file columns
    private static final int SUMMARY_DIFFERENCE_WIDTH = 20;
    // Summary file name truncation constants
    private static final int SUMMARY_FILE_NAME_MAX_LENGTH = SUMMARY_FILE_MAX_WIDTH - 3; // Account for truncation
    private static final int SUMMARY_FILE_NAME_TRUNCATE_LENGTH = SUMMARY_FILE_NAME_MAX_LENGTH - 3; // Account for "..."

    private ComparisonReporter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Calculates the optimal width for file presence columns based on file names.
     *
     * @param file1Name Name of the first file
     * @param file2Name Name of the second file
     * @return The calculated width for file presence columns
     */
    private static int calculateFilePresenceWidth(String file1Name, String file2Name) {
        // Calculate the required width for "File 1 (filename)" and "File 2 (filename)" format
        var truncatedFile1 = truncateFileName(file1Name);
        var truncatedFile2 = truncateFileName(file2Name);

        int file1HeaderLength = String.format("File 1 (%s)", truncatedFile1).length();
        int file2HeaderLength = String.format("File 2 (%s)", truncatedFile2).length();

        int requiredWidth = Math.max(file1HeaderLength, file2HeaderLength);
        requiredWidth = Math.max(requiredWidth, DETAILED_FILE_PRESENCE_MIN_WIDTH); // Ensure minimum width
        requiredWidth = Math.min(requiredWidth, DETAILED_FILE_PRESENCE_MAX_WIDTH); // Respect maximum width

        return requiredWidth;
    }

    /**
     * Calculates the optimal width for summary file columns based on file names.
     *
     * @param file1Name Name of the first file
     * @param file2Name Name of the second file
     * @return The calculated width for summary file columns
     */
    private static int calculateSummaryFileWidth(String file1Name, String file2Name) {
        var truncatedFile1 = truncateSummaryFileName(file1Name);
        var truncatedFile2 = truncateSummaryFileName(file2Name);

        int requiredWidth = Math.max(truncatedFile1.length(), truncatedFile2.length());
        requiredWidth = Math.max(requiredWidth, SUMMARY_FILE_MIN_WIDTH); // Ensure minimum width
        requiredWidth = Math.min(requiredWidth, SUMMARY_FILE_MAX_WIDTH); // Respect maximum width

        return requiredWidth;
    }

    /**
     * Calculates the total table width based on column widths.
     *
     * @param filePresenceWidth The width of file presence columns
     * @return The total table width
     */
    private static int calculateDetailedTableWidth(int filePresenceWidth) {
        return DETAILED_FIELD_NAME_WIDTH +
               (filePresenceWidth * 2) +
               (DETAILED_VALUE_WIDTH * 2) +
               DETAILED_STATUS_WIDTH +
               DETAILED_DIFFERENCE_WIDTH +
               (SEPARATOR_WIDTH * 6); // 6 separators
    }

    /**
     * Calculates the total summary table width based on column widths.
     *
     * @param summaryFileWidth The width of summary file columns
     * @return The total summary table width
     */
    private static int calculateSummaryTableWidth(int summaryFileWidth) {
        return SUMMARY_METRIC_WIDTH +
               (summaryFileWidth * 2) +
               SUMMARY_DIFFERENCE_WIDTH +
               (SEPARATOR_WIDTH * 3); // 3 separators
    }

    /**
     * Truncates a file name for display in column headers.
     *
     * @param fileName The file name to truncate
     * @return The truncated file name
     */
    private static String truncateFileName(String fileName) {
        if (fileName == null) return "";
        return fileName.length() > FILE_NAME_MAX_LENGTH
            ? fileName.substring(0, FILE_NAME_TRUNCATE_LENGTH) + "..."
            : fileName;
    }

    /**
     * Truncates a file name for display in summary table headers.
     *
     * @param fileName The file name to truncate
     * @return The truncated file name
     */
    private static String truncateSummaryFileName(String fileName) {
        if (fileName == null) return "";
        return fileName.length() > SUMMARY_FILE_NAME_MAX_LENGTH
            ? fileName.substring(0, SUMMARY_FILE_NAME_TRUNCATE_LENGTH) + "..."
            : fileName;
    }

    /**
     * Extracts just the file name from a full path.
     *
     * @param filePath The full file path
     * @return Just the file name without the path
     */
    private static String extractFileName(String filePath) {
        if (filePath == null) return "";
        // Handle both Windows and Unix path separators
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * Prints a detailed comparison table of fields with values.
     *
     * @param result The detailed field comparison result
     */
    public static void printDetailedComparisonTable(DetailedFieldComparisonResult result) {
        // Calculate optimal column width for file names
        int filePresenceWidth = calculateFilePresenceWidth(result.file1Name(), result.file2Name());
        int tableWidth = calculateDetailedTableWidth(filePresenceWidth);

        // Truncate file names for headers
        var truncatedFile1Name = truncateFileName(result.file1Name());
        var truncatedFile2Name = truncateFileName(result.file2Name());

        log.info("\n{}", "=".repeat(tableWidth));
        log.info("DETAILED FIELD COMPARISON TABLE");
        log.info("=".repeat(tableWidth));
        log.info(String.format(
            "%%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds".formatted(DETAILED_FIELD_NAME_WIDTH, filePresenceWidth, filePresenceWidth, DETAILED_VALUE_WIDTH, DETAILED_VALUE_WIDTH, DETAILED_STATUS_WIDTH, DETAILED_DIFFERENCE_WIDTH),
            "Field Name",
            "File 1 (" + truncatedFile1Name + ")",
            "File 2 (" + truncatedFile2Name + ")",
            "Value in File 1", "Value in File 2", "Status", "Difference"));
        log.info("-".repeat(tableWidth));

        result.fieldStatusList()
            .forEach(fieldStatus -> {
                var truncatedField = fieldStatus.fieldName().length() > FIELD_NAME_MAX_LENGTH
                    ? fieldStatus.fieldName().substring(0, FIELD_NAME_TRUNCATE_LENGTH) + "..."
                    : fieldStatus.fieldName();
                var truncatedValue1 = truncateValue(fieldStatus.value1());
                var truncatedValue2 = truncateValue(fieldStatus.value2());
                log.info(String.format(
                    "%%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds | %%-%ds".formatted(DETAILED_FIELD_NAME_WIDTH, filePresenceWidth, filePresenceWidth, DETAILED_VALUE_WIDTH, DETAILED_VALUE_WIDTH, DETAILED_STATUS_WIDTH, DETAILED_DIFFERENCE_WIDTH),
                    truncatedField,
                    fieldStatus.inFile1() ? "Yes" : "No",
                    fieldStatus.inFile2() ? "Yes" : "No",
                    truncatedValue1 != null ? truncatedValue1 : "-",
                    truncatedValue2 != null ? truncatedValue2 : "-",
                    fieldStatus.status(),
                    fieldStatus.difference()));
            });
    }

    /**
     * Truncates a value string for display.
     *
     * @param value The value to truncate
     * @return The truncated value
     */
    private static String truncateValue(String value) {
        if (value == null) return null;
        return value.length() > VALUE_MAX_LENGTH ?
            value.substring(0, VALUE_TRUNCATE_LENGTH) + "..." :
            value;
    }

    /**
     * Prints a summary table of the comparison results.
     *
     * @param fields1 Set of fields from the first JSON file
     * @param fields2 Set of fields from the second JSON file
     * @param result  The detailed field comparison result
     */
    public static void printSummaryTable(Set<String> fields1,
                                         Set<String> fields2,
                                         DetailedFieldComparisonResult result) {
        // Calculate the optimal column width for file names in summary
        var file1Name = extractFileName(result.file1Name());
        var file2Name = extractFileName(result.file2Name());
        int summaryFileWidth = calculateSummaryFileWidth(file1Name, file2Name);
        int summaryTableWidth = calculateSummaryTableWidth(summaryFileWidth);

        // Truncate file names for headers
        var truncatedFile1Name = truncateSummaryFileName(file1Name);
        var truncatedFile2Name = truncateSummaryFileName(file2Name);

        log.info("=".repeat(summaryTableWidth));
        log.info("SUMMARY");
        log.info("=".repeat(summaryTableWidth));
        log.info(String.format(
            "%%-%ds | %%-%ds | %%-%ds | %%-%ds".formatted(SUMMARY_METRIC_WIDTH, summaryFileWidth, summaryFileWidth, SUMMARY_DIFFERENCE_WIDTH),
            "Metric", truncatedFile1Name, truncatedFile2Name, "Difference"));
        log.info("-".repeat(summaryTableWidth));

        log.info(String.format(
            "%%-%ds | %%-%dd | %%-%dd | %%-%ds".formatted(SUMMARY_METRIC_WIDTH, summaryFileWidth, summaryFileWidth, SUMMARY_DIFFERENCE_WIDTH),
            "Total fields",
            fields1.size(),
            fields2.size(),
            fields1.size() == fields2.size() ? "Same" : String.valueOf(Math.abs(fields1.size() - fields2.size()))));

        log.info(String.format(
            "%%-%ds | %%-%dd | %%-%dd | %%-%dd".formatted(SUMMARY_METRIC_WIDTH, summaryFileWidth, summaryFileWidth, SUMMARY_DIFFERENCE_WIDTH),
            "Common fields",
            result.commonFields(),
            result.commonFields(),
            0));

        log.info(String.format(
            "%%-%ds | %%-%dd | %%-%dd | %%-%ds".formatted(SUMMARY_METRIC_WIDTH, summaryFileWidth, summaryFileWidth, SUMMARY_DIFFERENCE_WIDTH),
            "Unique fields",
            result.onlyInFile1(),
            result.onlyInFile2(),
            "N/A"));

        log.info("=".repeat(summaryTableWidth));
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
