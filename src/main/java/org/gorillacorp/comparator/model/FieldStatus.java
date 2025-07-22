package org.gorillacorp.comparator.model;

/**
 * Represents the status of a field in a JSON comparison.
 * Contains information about whether the field exists in each file and its status.
 */
public record FieldStatus(
    String fieldName,
    boolean inFile1,
    boolean inFile2,
    String status
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
     * Builder for FieldStatus record.
     */
    public static class Builder {
        private String fieldName;
        private boolean inFile1;
        private boolean inFile2;
        private String status;

        /**
         * Sets the field name.
         *
         * @param fieldName the name of the field
         * @return this builder instance
         */
        public Builder fieldName(String fieldName) {
            this.fieldName = fieldName;
            return this;
        }

        /**
         * Sets whether the field exists in file 1.
         *
         * @param inFile1 true if the field exists in file 1, false otherwise
         * @return this builder instance
         */
        public Builder inFile1(boolean inFile1) {
            this.inFile1 = inFile1;
            return this;
        }

        /**
         * Sets whether the field exists in file 2.
         *
         * @param inFile2 true if the field exists in file 2, false otherwise
         * @return this builder instance
         */
        public Builder inFile2(boolean inFile2) {
            this.inFile2 = inFile2;
            return this;
        }

        /**
         * Sets the status of the field comparison.
         *
         * @param status the status description
         * @return this builder instance
         */
        public Builder status(String status) {
            this.status = status;
            return this;
        }

        /**
         * Builds and returns a new FieldStatus instance.
         *
         * @return a new FieldStatus with the configured values
         */
        public FieldStatus build() {
            return new FieldStatus(fieldName, inFile1, inFile2, status);
        }
    }
}
