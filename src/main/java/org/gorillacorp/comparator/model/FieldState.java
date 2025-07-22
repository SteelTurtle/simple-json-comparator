package org.gorillacorp.comparator.model;

/**
 * Represents the various states of a field during the comparison of two JSON structures or files.
 * It is used to identify the presence and equality of fields in the compared structures.
 * <p>
 * The possible states are:
 * - COMMON_SAME: The field is present in both JSON files with identical values.
 * - COMMON_DIFFERENT: The field is present in both JSON files but with different values.
 * - ONLY_IN_FILE1: The field exists only in the first JSON file.
 * - ONLY_IN_FILE2: The field exists only in the second JSON file.
 */
public enum FieldState {
    COMMON_SAME, COMMON_DIFFERENT, ONLY_IN_FILE1, ONLY_IN_FILE2
}
