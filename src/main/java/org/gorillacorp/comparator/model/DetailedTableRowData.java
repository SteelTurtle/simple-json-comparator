package org.gorillacorp.comparator.model;


/**
 * Represents a detailed row of data for a table, used in comparing files or data structures.
 * This record includes information about the field's presence in two files, values, status,
 * and any differences identified during comparison.
 */
public record DetailedTableRowData(
    String fieldName,
    String file1Presence,
    String file2Presence,
    String value1,
    String value2,
    String status,
    String difference
) {
    public static class Builder {
        private String fieldName;
        private String file1Presence;
        private String file2Presence;
        private String value1;
        private String value2;
        private String status;
        private String difference;

        /**
         * Creates a new builder instance.
         *
         * @return a new Builder instance
         */
        public static DetailedTableRowData.Builder builder() {
            return new DetailedTableRowData.Builder();
        }

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder file1Presence(String file1Presence) {
            this.file1Presence = file1Presence;
            return this;
        }

        public Builder file2Presence(String file2Presence) {
            this.file2Presence = file2Presence;
            return this;
        }

        public Builder value1(String value1) {
            this.value1 = value1;
            return this;
        }

        public Builder value2(String value2) {
            this.value2 = value2;
            return this;
        }

        public Builder status(String status) {
            this.status = status;
            return this;
        }

        public Builder difference(String difference) {
            this.difference = difference;
            return this;
        }

        public DetailedTableRowData build() {
            return new DetailedTableRowData(
                fieldName,
                file1Presence,
                file2Presence,
                value1,
                value2,
                status,
                difference
            );
        }
    }
}
