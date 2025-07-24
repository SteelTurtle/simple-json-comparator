package org.gorillacorp.comparator.output;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.gorillacorp.comparator.processor.JsonComparatorOperations;
import org.gorillacorp.comparator.processor.StructuredScopeOperationsHandler;
import org.gorillacorp.comparator.utils.FileOperations;

import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;

public final class ComparisonCsvExporter {
    private ComparisonCsvExporter() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Displays a detailed comparison report of two JSON files.
     * This method compares the structure and values of the given JSON files
     * and generates a report highlighting the similarities and differences.
     *
     * @param file1Path the file path of the first JSON file
     * @param file2Path the file path of the second JSON file
     * @throws IOException if there is an issue reading the files
     */
    public static void exportJsonComparisonReportToCsv(String file1Path,
                                                       String file2Path,
                                                       String destinationFilePath) throws IOException {
        try (var scope = new StructuredTaskScope.ShutdownOnFailure();
             var writer = new FileWriter(destinationFilePath);
             var csvPrinter = new CSVPrinter(writer, CSVFormat.Builder
                     .create()
                     .setDelimiter(',')
                     .setAutoFlush(true)
                     .setHeader(
                             "Field Name",
                             "File 1 (%s)".formatted(FileOperations.extractFileName(file1Path)),
                             "File 2 (%s)".formatted(FileOperations.extractFileName(file2Path)),
                             "Value in File 1",
                             "Value in File 2",
                             "Status",
                             "Difference"
                     ).get())
        ) {
            var compoundComparisonResults =
                    StructuredScopeOperationsHandler.getGetCompoundFieldsComparisonResult(
                            file1Path,
                            file2Path,
                            scope
                    );
            var comparisonResult = JsonComparatorOperations.compareFieldsWithValues(
                    file1Path,
                    file2Path,
                    compoundComparisonResults.fields1Map(),
                    compoundComparisonResults.fields2Map()
            );
            comparisonResult.fieldStatusList().forEach(
                    fieldStatus -> FileOperations.writeToCsv(fieldStatus, csvPrinter)
            );

        } catch (ExecutionException | InterruptedException exception) {
            StructuredScopeOperationsHandler.handleStructuredTaskScopeException(exception);
        }
    }


}
