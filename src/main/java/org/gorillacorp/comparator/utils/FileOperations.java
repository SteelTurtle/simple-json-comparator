package org.gorillacorp.comparator.utils;

import org.apache.commons.csv.CSVPrinter;
import org.gorillacorp.comparator.exception.CustomJsonParseException;
import org.gorillacorp.comparator.model.DetailedFieldStatus;

import java.io.IOException;

public final class FileOperations {
    private FileOperations() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Extracts just the file name from a full path.
     *
     * @param filePath The full file path
     * @return Just the file name without the path
     */
    public static String extractFileName(String filePath) {
        if (filePath == null) return "";
        // Handle both Windows and Unix path separators
        int lastSlash = Math.max(filePath.lastIndexOf('/'), filePath.lastIndexOf('\\'));
        return lastSlash >= 0 ? filePath.substring(lastSlash + 1) : filePath;
    }

    /**
     * Writes the detailed status of a field from a JSON comparison to a CSV file.
     *
     * @param fieldStatus an instance of DetailedFieldStatus containing information about the field's name,
     *                    presence in the source files, values, status, and differences
     * @param csvPrinter  an instance of CSVPrinter used to write the field details to the CSV file
     * @throws CustomJsonParseException if an IOException occurs while writing to the CSV file
     */
    public static void writeToCsv(DetailedFieldStatus fieldStatus, CSVPrinter csvPrinter) {
        try {
            csvPrinter.printRecord(
                fieldStatus.fieldName(),
                fieldStatus.inFile1() ? "Yes" : "No",
                fieldStatus.inFile2() ? "Yes" : "No",
                fieldStatus.value1(),
                fieldStatus.value2(),
                fieldStatus.status(),
                fieldStatus.difference()
            );
        } catch (IOException e) {
            throw new CustomJsonParseException("Error writing to CSV file", e);
        }
    }
}
