package org.gorillacorp.comparator.processor;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.gorillacorp.comparator.model.DetailedFieldComparisonResult;
import org.gorillacorp.comparator.model.DetailedFieldStatus;
import org.gorillacorp.comparator.model.FieldState;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.StructuredTaskScope;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * Utility class for comparing JSON structures.
 */
public final class JsonComparatorOperations {

    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonComparatorOperations() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Compares two JSON files to determine if their contents are equal, ignoring the order of elements
     * in arrays and the order of fields in objects. The comparison is performed in a concurrent and
     * efficient manner using structured task scopes.
     *
     * @param filePath1 the file path to the first JSON file
     * @param filePath2 the file path to the second JSON file
     * @return true if the two JSON files are considered equal regardless of order, false otherwise
     * @throws IOException if an error occurs while reading or parsing the JSON files
     */
    public static boolean compareJson(String filePath1, String filePath2) throws IOException {
        try (var reader1 = Files.newBufferedReader(Path.of(filePath1));
             var reader2 = Files.newBufferedReader(Path.of(filePath2));
             var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var jsonNode1SubTask = scope.fork(() -> mapper.readTree(reader1));
            var jsonNode2SubTask = scope.fork(() -> mapper.readTree(reader2));
            scope.join();
            scope.throwIfFailed();

            return compareJsonNodesIgnoringOrder(jsonNode1SubTask.get(), jsonNode2SubTask.get());
        } catch (ExecutionException | InterruptedException exception) {
            StructuredScopeOperationsHandler.handleStructuredTaskScopeException(exception);
            throw new AssertionError("This code should never be reached");
        }
    }


    /**
     * Compares two JSON nodes to determine if they are equal, regardless of the order of elements
     * in arrays or fields in objects. Other node types are compared using their natural equality.
     *
     * @param jsonNode1 the first JSON node to compare
     * @param jsonNode2 the second JSON node to compare
     * @return true if the two JSON nodes are considered equal ignoring order, false otherwise
     */
    public static boolean compareJsonNodesIgnoringOrder(JsonNode jsonNode1, JsonNode jsonNode2) {
        return jsonNode1.getNodeType() == jsonNode2.getNodeType() &&
               switch (jsonNode1.getNodeType()) {
                   case OBJECT -> compareObjectNodes(jsonNode1, jsonNode2);
                   case ARRAY -> compareArrayNodes(jsonNode1, jsonNode2);
                   default -> jsonNode1.equals(jsonNode2);
               };

    }


    /**
     * Compares two JSON object nodes to determine if they are equal by structure and values,
     * without considering the order of fields. The comparison includes checking if both
     * nodes have the same size, identical field names, and comparing the values of each field.
     *
     * @param node1 the first JSON object node to compare
     * @param node2 the second JSON object node to compare
     * @return true if the two JSON object nodes are considered equal, false otherwise
     */
    private static boolean compareObjectNodes(JsonNode node1, JsonNode node2) {
        if (node1.size() != node2.size()) {
            return false;
        }

        var fieldNames1 = collectFieldNames(node1);
        var fieldNames2 = collectFieldNames(node2);

        return fieldNames1.equals(fieldNames2) &&
               fieldNames1.stream()
                   .allMatch(fieldName -> compareJsonNodesIgnoringOrder(
                       node1.get(fieldName),
                       node2.get(fieldName)
                   ));
    }


    /**
     * Compares two JSON array nodes to determine if they are equal in size and structure,
     * recursively considering the equality of corresponding elements, regardless of their order.
     *
     * @param node1 the first JSON array node to compare
     * @param node2 the second JSON array node to compare
     * @return true if the two JSON array nodes are considered equal, false otherwise
     */
    private static boolean compareArrayNodes(JsonNode node1, JsonNode node2) {
        return node1.size() == node2.size() &&
               IntStream.range(0, node1.size())
                   .allMatch(i -> compareJsonNodesIgnoringOrder(
                       node1.get(i),
                       node2.get(i)
                   ));
    }


    /**
     * Collects and returns the field names from the specified JSON node.
     *
     * @param jsonNode the JSON node from which to collect field names
     * @return a set of field names found in the given JSON node
     */
    private static Set<String> collectFieldNames(JsonNode jsonNode) {
        var fieldNames = new HashSet<String>();
        jsonNode.fieldNames().forEachRemaining(fieldNames::add);
        return fieldNames;
    }


    /**
     * Loads and parses two JSON files, returning their contents as an array of {@code JsonNode} objects.
     * Each JSON file is read and parsed concurrently to improve performance.
     *
     * @param file1Path the path to the first JSON file
     * @param file2Path the path to the second JSON file
     * @return an array of {@code JsonNode} objects, where the first element represents the parsed
     * content of the first file and the second element represents the parsed content of the second file
     * @throws IOException if an error occurs while reading or parsing the JSON files
     */
    public static JsonNode[] loadJsonNodes(String file1Path, String file2Path) throws IOException {

        try (var reader1 = Files.newBufferedReader(Path.of(file1Path));
             var reader2 = Files.newBufferedReader(Path.of(file2Path));
             var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            var json1Subtask = scope.fork(() -> mapper.readTree(reader1));
            var json2Subtask = scope.fork(() -> mapper.readTree(reader2));
            scope.join();
            scope.throwIfFailed();
            return new JsonNode[]{json1Subtask.get(), json2Subtask.get()};
        } catch (ExecutionException | InterruptedException e) {
            StructuredScopeOperationsHandler.handleStructuredTaskScopeException(e);
            throw new AssertionError("This code should never be reached");
        }
    }


    /**
     * Determines the state of a field during the comparison of two JSON structures based on its presence
     * and value equality in two JSON files.
     *
     * @param inFile1     true if the field exists in the first JSON file, false otherwise
     * @param inFile2     true if the field exists in the second JSON file, false otherwise
     * @param valuesEqual true if the field values in both JSON files are equal, false otherwise
     * @return the state of the field, represented as a {@code FieldState} enumeration
     */
    private static FieldState determineFieldState(boolean inFile1,
                                                  boolean inFile2,
                                                  boolean valuesEqual) {
        if (inFile1 && inFile2) {
            return valuesEqual ? FieldState.COMMON_SAME : FieldState.COMMON_DIFFERENT;
        } else if (inFile1) {
            return FieldState.ONLY_IN_FILE1;
        } else {
            return FieldState.ONLY_IN_FILE2;
        }
    }


    /**
     * Compares fields and values from two JSON files, identifying common fields, fields
     * unique to each file, and fields with differing values. The method returns a
     * detailed comparison result including metadata and status information about the fields.
     *
     * @param file1Path the file path of the first JSON file
     * @param file2Path the file path of the second JSON file
     * @param fields1   a map of field names and their values from the first JSON file
     * @param fields2   a map of field names and their values from the second JSON file
     * @return a {@code DetailedFieldComparisonResult} containing the field comparison
     * details, including common fields, unique fields, and differing values
     */
    public static DetailedFieldComparisonResult compareFieldsWithValues(String file1Path,
                                                                        String file2Path,
                                                                        Map<String, String> fields1,
                                                                        Map<String, String> fields2) {
        var allFields = new HashSet<String>();
        allFields.addAll(fields1.keySet());
        allFields.addAll(fields2.keySet());

        var sortedFields = allFields.stream().sorted().toList();
        var fieldStatusAccumulator = new ArrayList<DetailedFieldStatus>();

        var commonFields = new AtomicInteger();
        var onlyInFile1 = new AtomicInteger();
        var onlyInFile2 = new AtomicInteger();
        var differentValues = new AtomicInteger();

        for (String field : sortedFields) {
            var fieldIsInFile1 = fields1.containsKey(field);
            var fieldIsInFile2 = fields2.containsKey(field);

            var value1 = fields1.get(field);
            var value2 = fields2.get(field);

            var fieldState = determineFieldState(fieldIsInFile1, fieldIsInFile2, Objects.equals(value1, value2));
            var result = switch (fieldState) {
                case COMMON_SAME -> new StatusResult("✓ Common", "Same value", commonFields::getAndIncrement);
                case COMMON_DIFFERENT ->
                    new StatusResult("⚠ Different Values", "Values differ", differentValues::getAndIncrement);
                case ONLY_IN_FILE1 ->
                    new StatusResult("⚠ Only in File 1", "Missing in File 2", onlyInFile1::getAndIncrement);
                case ONLY_IN_FILE2 ->
                    new StatusResult("⚠ Only in File 2", "Missing in File 1", onlyInFile2::getAndIncrement);
            };

            var status = result.status();
            var difference = result.difference();
            result.counterUpdate().run();

            fieldStatusAccumulator.add(DetailedFieldStatus.builder()
                .fieldName(field)
                .inFile1(fieldIsInFile1)
                .inFile2(fieldIsInFile2)
                .value1(value1)
                .value2(value2)
                .status(status)
                .difference(difference)
                .build()
            );
        }

        return DetailedFieldComparisonResult.builder()
            .fieldStatusList(fieldStatusAccumulator)
            .file1Name(file1Path.substring(file1Path.lastIndexOf('/') + 1))
            .file2Name(file2Path.substring(file2Path.lastIndexOf('/') + 1))
            .commonFields(commonFields.get())
            .onlyInFile1(onlyInFile1.get())
            .onlyInFile2(onlyInFile2.get())
            .differentValues(differentValues.get())
            .build();
    }

    /**
     * Represents the result of a status comparison between two entities, intended for use in JSON comparison operations.
     * <p>
     * The StatusResult record encapsulates:
     * - The status of the comparison, indicating success, failure, or other relevant states.
     * - The difference identified during the comparison, which provides details about discrepancies found.
     * - A Runnable task to update a counter or perform a related action post-comparison.
     * <p>
     * This record is used within JSON comparison workflows to track the outcome, highlight discrepancies,
     * and allow for additional processing or state updates triggered by the comparison results.
     * <p>
     * Fields:
     * - status: A String representing the status of the operation (e.g., "SUCCESS", "DIFFERENT").
     * - difference: A String describing the discrepancy or result of the comparison.
     * - counterUpdate: A Runnable that executes an action to update a counter or perform a related handling task.
     */
    private record StatusResult(String status, String difference, Runnable counterUpdate) {
    }
}
