package org.gorillacorp.comparator;

import org.gorillacorp.comparator.model.DetailedFieldComparisonResult;
import org.gorillacorp.comparator.model.DetailedTableRowData;
import org.slf4j.Logger;

import java.util.Set;

import static org.gorillacorp.comparator.utils.ReportFormattingConstants.*;
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
        int filePresenceWidth = calculateFilePresenceWidth(result.file1Name(), result.file2Name());

        if (log.isInfoEnabled()) {
            printDetailedTableHeader(result, filePresenceWidth);
            printDetailedTableRows(result, filePresenceWidth);
        }
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

        if (log.isInfoEnabled()) {
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

    /**
     * Truncates a value string for display, properly handling wide Unicode characters.
     *
     * @param value The value to truncate
     * @return The truncated value
     */
    private static String truncateValue(String value) {
        if (value == null) return null;
        return truncateByDisplayWidth(value, VALUE_MAX_LENGTH, VALUE_TRUNCATE_LENGTH);
    }

    /**
     * Truncates a field name for display, properly handling wide Unicode characters.
     *
     * @param fieldName      The field name to truncate
     * @param maxLength      The maximum allowed length
     * @param truncateLength The length to truncate to before adding "..."
     * @return The truncated field name
     */
    private static String truncateFieldName(String fieldName, int maxLength, int truncateLength) {
        if (fieldName == null) return "";
        return truncateByDisplayWidth(fieldName, maxLength, truncateLength);
    }

    /**
     * Truncates a string based on display width, properly handling wide Unicode characters
     * like Japanese Kanji that take up more visual space.
     *
     * @param text          The text to truncate
     * @param maxWidth      The maximum display width allowed
     * @param truncateWidth The width to truncate to before adding "..."
     * @return The truncated text
     */
    private static String truncateByDisplayWidth(String text, int maxWidth, int truncateWidth) {
        if (text == null) return null;

        int displayWidth = calculateDisplayWidth(text);
        if (displayWidth <= maxWidth) {
            return text;
        }

        // Find the position where we should truncate based on display width
        int currentWidth = 0;
        int truncatePosition = 0;

        for (int i = 0; i < text.length(); i++) {
            int charWidth = getCharacterDisplayWidth(text.charAt(i));
            if (currentWidth + charWidth > truncateWidth) {
                break;
            }
            currentWidth += charWidth;
            truncatePosition = i + 1;
        }

        return text.substring(0, truncatePosition) + "...";
    }

    /**
     * Calculates the display width of a string, accounting for wide Unicode characters.
     *
     * @param text The text to measure
     * @return The display width
     */
    private static int calculateDisplayWidth(String text) {
        if (text == null) return 0;
        return text.chars().map(ComparisonReporter::getCharacterDisplayWidth).sum();
    }

    /**
     * Returns the display width of a character. Wide characters (like CJK) take 2 spaces,
     * regular characters take 1 space.
     *
     * @param codePoint The Unicode code point
     * @return 2 for wide characters, 1 for regular characters
     */
    private static int getCharacterDisplayWidth(int codePoint) {
        // Check for wide characters (CJK ideographs, full-width characters, etc.)
        if (isWideCharacter(codePoint)) {
            return 2;
        }
        return 1;
    }


    /**
     * Determines whether a Unicode code point represents a wide character.
     * Wide characters include CJK Unified Ideographs, CJK Extensions, Hiragana, Katakana,
     * full-width characters, and certain symbols and punctuation.
     *
     * @param codePoint The Unicode code point to evaluate.
     * @return true if the code point represents a wide character; false otherwise.
     */
    private static boolean isWideCharacter(int codePoint) {
        return switch (codePoint) {
            case int n when (n >= 0x4E00 && n <= 0x9FFF) -> true;     // CJK Unified Ideographs
            case int n when (n >= 0x3400 && n <= 0x4DBF) -> true;     // CJK Extension A
            case int n when (n >= 0x20000 && n <= 0x2A6DF) -> true;   // CJK Extension B
            case int n when (n >= 0x3040 && n <= 0x309F) -> true;     // Hiragana
            case int n when (n >= 0x30A0 && n <= 0x30FF) -> true;     // Katakana
            case int n when (n >= 0xFF00 && n <= 0xFFEF) -> true;     // Full-width characters
            case int n when (n >= 0x3000 && n <= 0x303F) -> true;     // CJK Symbols and Punctuation
            default -> false;
        };
    }


    /**
     * Creates a properly padded string that accounts for wide Unicode characters.
     *
     * @param text  The text to pad
     * @param width The target display width
     * @return The padded string
     */
    private static String padString(String text, int width) {
        if (text == null) text = "";

        int displayWidth = calculateDisplayWidth(text);
        int paddingNeeded = Math.max(0, width - displayWidth);
        String padding = " ".repeat(paddingNeeded);

        return text + padding;
    }

    /**
     * Creates a formatted row for the detailed table using custom padding.
     *
     * @param rowData           The data for the row
     * @param filePresenceWidth Width for file presence columns
     * @return The formatted row string
     */
    private static String formatDetailedTableRow(DetailedTableRowData rowData,
                                                 int filePresenceWidth) {
        final var COLUMN_DELIMITER = " | ";
        return String.join(COLUMN_DELIMITER,
            padString(rowData.fieldName(), DETAILED_FIELD_NAME_WIDTH),
            padString(rowData.file1Presence(), filePresenceWidth),
            padString(rowData.file2Presence(), filePresenceWidth),
            padString(rowData.value1(), DETAILED_VALUE_WIDTH),
            padString(rowData.value2(), DETAILED_VALUE_WIDTH),
            padString(rowData.status(), DETAILED_STATUS_WIDTH),
            padString(rowData.difference(), DETAILED_DIFFERENCE_WIDTH)
        ) + " |";

    }

    /**
     * Prints the header section of the detailed comparison table.
     *
     * @param result            The detailed field comparison result
     * @param filePresenceWidth The calculated width for file presence columns
     */
    private static void printDetailedTableHeader(DetailedFieldComparisonResult result,
                                                 int filePresenceWidth) {
        int tableWidth = calculateDetailedTableWidth(filePresenceWidth);
        var truncatedFile1Name = truncateFileName(extractFileName(result.file1Name()));
        var truncatedFile2Name = truncateFileName(extractFileName(result.file2Name()));

        var headerData = DetailedTableRowData.Builder.builder()
            .fieldName("Field Name")
            .file1Presence("File 1 (" + truncatedFile1Name + ")")
            .file2Presence("File 2 (" + truncatedFile2Name + ")")
            .value1("Value in File 1")
            .value2("Value in File 2")
            .status("Status")
            .difference("Difference")
            .build();

        if (log.isInfoEnabled()) {
            log.info("=".repeat(tableWidth));
            log.info(formatDetailedTableRow(headerData, filePresenceWidth));
            log.info("-".repeat(tableWidth));
        }

    }


    /**
     * Prints the rows of a detailed comparison table, including field information,
     * status, and differences between two files. Each row is formatted with
     * truncated values and aligned according to calculated column widths.
     *
     * @param result            The detailed field comparison result containing information
     *                          about fields, their statuses, and differences.
     * @param filePresenceWidth The width to use for file presence columns in the
     *                          table layout.
     */
    private static void printDetailedTableRows(DetailedFieldComparisonResult result, int filePresenceWidth) {
        result.fieldStatusList().forEach(fieldStatus -> {
            var truncatedField = truncateFieldName(
                fieldStatus.fieldName(),
                FIELD_NAME_MAX_LENGTH,
                FIELD_NAME_TRUNCATE_LENGTH
            );
            var truncatedValue1 = truncateValue(fieldStatus.value1());
            var truncatedValue2 = truncateValue(fieldStatus.value2());

            var rowData = DetailedTableRowData.Builder.builder()
                .fieldName(truncatedField)
                .file1Presence(fieldStatus.inFile1() ? "Yes" : "No")
                .file2Presence(fieldStatus.inFile2() ? "Yes" : "No")
                .value1(truncatedValue1 != null ? truncatedValue1 : "-")
                .value2(truncatedValue2 != null ? truncatedValue2 : "-")
                .status(fieldStatus.status())
                .difference(fieldStatus.difference())
                .build();

            if (log.isInfoEnabled()) {
                log.info(formatDetailedTableRow(rowData, filePresenceWidth));
            }
        });
    }

}
