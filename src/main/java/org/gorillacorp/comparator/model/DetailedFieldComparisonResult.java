package org.gorillacorp.comparator.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the detailed result of comparing fields with values between two JSON files.
 * Contains a list of detailed field statuses and summary statistics.
 */
public record DetailedFieldComparisonResult(
    List<DetailedFieldStatus> fieldStatusList,
    String file1Name,
    String file2Name,
    int commonFields,
    int onlyInFile1,
    int onlyInFile2,
    int differentValues
) {

    /**
     * Creates a new builder instance.
     *
     * @return a new Builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Builder for DetailedFieldComparisonResult record.
     */
    public static class Builder {
        private List<DetailedFieldStatus> fieldStatusList = new ArrayList<>();
        private String file1Name;
        private String file2Name;
        private int commonFields;
        private int onlyInFile1;
        private int onlyInFile2;
        private int differentValues;

        /**
         * Sets the field status list.
         *
         * @param fieldStatusList the list of detailed field statuses
         * @return this builder instance
         */
        public Builder fieldStatusList(List<DetailedFieldStatus> fieldStatusList) {
            this.fieldStatusList = fieldStatusList != null ? new ArrayList<>(fieldStatusList) : new ArrayList<>();
            return this;
        }


        /**
         * Sets the name of the first file for comparison.
         *
         * @param file1Name the name of the first file
         * @return this builder instance
         */
        public Builder file1Name(String file1Name) {
            this.file1Name = file1Name;
            return this;
        }

        /**
         * Sets the name of the second file for comparison.
         *
         * @param file2Name the name of the second file
         * @return this builder instance
         */
        public Builder file2Name(String file2Name) {
            this.file2Name = file2Name;
            return this;
        }

        /**
         * Sets the number of common fields.
         *
         * @param commonFields the number of common fields
         * @return this builder instance
         */
        public Builder commonFields(int commonFields) {
            this.commonFields = commonFields;
            return this;
        }

        /**
         * Sets the number of fields only in file 1.
         *
         * @param onlyInFile1 the number of fields only in file 1
         * @return this builder instance
         */
        public Builder onlyInFile1(int onlyInFile1) {
            this.onlyInFile1 = onlyInFile1;
            return this;
        }

        /**
         * Sets the number of fields only in file 2.
         *
         * @param onlyInFile2 the number of fields only in file 2
         * @return this builder instance
         */
        public Builder onlyInFile2(int onlyInFile2) {
            this.onlyInFile2 = onlyInFile2;
            return this;
        }

        /**
         * Sets the number of fields with different values.
         *
         * @param differentValues the number of fields with different values
         * @return this builder instance
         */
        public Builder differentValues(int differentValues) {
            this.differentValues = differentValues;
            return this;
        }

        /**
         * Builds and returns a new DetailedFieldComparisonResult instance.
         *
         * @return a new DetailedFieldComparisonResult with the configured values
         */
        public DetailedFieldComparisonResult build() {
            return new DetailedFieldComparisonResult(
                List.copyOf(fieldStatusList),// Make immutable copy
                file1Name,
                file2Name,
                commonFields,
                onlyInFile1,
                onlyInFile2,
                differentValues
            );
        }
    }
}
