package org.gorillacorp.comparator.model;

/**
 * Represents the detailed status of a field in a JSON comparison.
 * Contains information about whether the field exists in each file, its values, and status.
 */
public record DetailedFieldStatus(
    String fieldName,
    boolean inFile1,
    boolean inFile2,
    String value1,
    String value2,
    String status,
    String difference
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
     * Builder for DetailedFieldStatus record.
     */
    public static class Builder {
        private String fieldName;
        private boolean inFile1;
        private boolean inFile2;
        private String value1;
        private String value2;
        private String status;
        private String difference;

        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        public Builder inFile1(boolean inFile1) {
            this.inFile1 = inFile1;
            return this;
        }

        public Builder inFile2(boolean inFile2) {
            this.inFile2 = inFile2;
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

        public DetailedFieldStatus build() {
            return new DetailedFieldStatus(fieldName, inFile1, inFile2, value1, value2, status, difference);
        }
    }
}
