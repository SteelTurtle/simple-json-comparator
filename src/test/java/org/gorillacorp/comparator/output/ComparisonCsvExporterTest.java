package org.gorillacorp.comparator.output;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class ComparisonCsvExporterTest {


    @Test
    @DisplayName("Tests that valid input files are successfully processed and the CSVPrinter writes the expected entries.")
    void testExportJsonComparisonReportToCsv_validInputs(@TempDir Path tempDir)
            throws IOException {
        // Create test JSON files
        String json1Content = """
                {
                    "name": "John",
                    "age": 30,
                    "city": "New York"
                }
                """;

        String json2Content = """
                {
                    "name": "Jane",
                    "age": 25,
                    "city": "Boston"
                }
                """;

        // generate an in-memory temporary file:
        Path file1 = tempDir.resolve("test1.json");
        Path file2 = tempDir.resolve("test2.json");
        Path outputFile = tempDir.resolve("output.csv");

        Files.writeString(file1, json1Content);
        Files.writeString(file2, json2Content);

        // Test the method
        assertDoesNotThrow(() -> ComparisonCsvExporter.exportJsonComparisonReportToCsv(
                file1.toString(),
                file2.toString(),
                outputFile.toString()
        ));

        // Verify the output file was created
        assertTrue(Files.exists(outputFile), "Output CSV file should be created");

        // Verify the file has content
        String csvContent = Files.readString(outputFile);
        assertFalse(csvContent.isEmpty(), "CSV file should not be empty");

        // Verify CSV headers are present
        assertTrue(csvContent.contains("Field Name"), "CSV should contain a Field Name header");
        assertTrue(csvContent.contains("File 1"), "CSV should contain a File 1 header");
        assertTrue(csvContent.contains("File 2"), "CSV should contain a File 2 header");
    }
}
