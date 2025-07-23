package org.gorillacorp.comparator.model;

/**
 * Represents the various states of a field during the comparison of two JSON structures or files.
 * It is used to identify the presence and equality of fields in the compared structures.
 */
public enum FieldState {
    /**
     * Indicates that the field is present in both JSON files and has identical values in each.
     * This state is used during the comparison of two JSON structures or files.
     */
    COMMON_SAME,
    /**
     * Indicates that the field is present in both JSON files but has different values in each.
     * This state is used during the comparison of two JSON structures or files to identify discrepancies
     * between the same field across two files.
     */
    COMMON_DIFFERENT,
    /**
     * Indicates that the field exists only in the first JSON file during the comparison of two JSON structures.
     * This state is used to identify fields exclusive to the first file.
     */
    ONLY_IN_FILE1,
    /**
     * Indicates that the field exists only in the second JSON file during the comparison of two JSON structures.
     * This state is used to identify fields exclusive to the second file.
     */
    ONLY_IN_FILE2
}
