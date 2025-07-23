package org.gorillacorp.comparator.utils;

/**
 * Utility class providing constants for formatting reports, specifically for
 * detailed and summary tables. Includes constants for column widths, truncation
 * limits, separator widths, and other formatting parameters.
 * <p>
 * This class is not meant to be instantiated as it only contains static constants
 * to be used throughout the application for consistent report formatting.
 */
public final class ReportFormattingConstants {
    public static final int SEPARATOR_WIDTH = 3; // Length of " | "
    // Detailed table constants
    public static final int DETAILED_FIELD_NAME_WIDTH = 40;
    public static final int DETAILED_FILE_PRESENCE_MIN_WIDTH = 8; // Minimum width for file columns
    public static final int DETAILED_FILE_PRESENCE_MAX_WIDTH = 25; // Maximum width for file columns
    public static final int DETAILED_VALUE_WIDTH = 20;
    public static final int DETAILED_STATUS_WIDTH = 20;
    public static final int DETAILED_DIFFERENCE_WIDTH = 25;
    // Field name truncation constants
    public static final int FIELD_NAME_MAX_LENGTH = 37;
    public static final int FIELD_NAME_TRUNCATE_LENGTH = 34;
    // Value truncation constants
    public static final int VALUE_MAX_LENGTH = 17;
    public static final int VALUE_TRUNCATE_LENGTH = 14;
    // File name truncation constants for column headers
    public static final int FILE_NAME_MAX_LENGTH = DETAILED_FILE_PRESENCE_MAX_WIDTH - 10; // Account for "File 1 ()" format
    public static final int FILE_NAME_TRUNCATE_LENGTH = FILE_NAME_MAX_LENGTH - 3; // Account for "..."
    // Summary table constants
    public static final int SUMMARY_METRIC_WIDTH = 40;
    public static final int SUMMARY_FILE_MIN_WIDTH = 15; // Minimum width for summary file columns
    public static final int SUMMARY_FILE_MAX_WIDTH = 30; // Maximum width for summary file columns
    public static final int SUMMARY_DIFFERENCE_WIDTH = 20;
    // Summary file name truncation constants
    public static final int SUMMARY_FILE_NAME_MAX_LENGTH = SUMMARY_FILE_MAX_WIDTH - 3; // Account for truncation
    public static final int SUMMARY_FILE_NAME_TRUNCATE_LENGTH = SUMMARY_FILE_NAME_MAX_LENGTH - 3; // Account for "..."

    public ReportFormattingConstants() {
        throw new IllegalStateException("Utility class");
    }

}
