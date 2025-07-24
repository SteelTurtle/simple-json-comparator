package org.gorillacorp.comparator.processor;

import com.fasterxml.jackson.databind.JsonNode;
import org.gorillacorp.comparator.utils.IOExceptionConsumer;
import org.slf4j.Logger;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;


/**
 * Utility class for traversing and extracting information from JSON nodes.
 * <p>
 * This class provides methods to extract field names and field-value pairs
 * from a JSON structure. It supports recursive traversal of JSON objects and
 * arrays, while also handling various JSON node types such as binary, POJO, and primitives.
 * The extracted information is returned in the form of sets or maps. The class assumes
 * JSON nodes are represented using the Jackson library's {@code JsonNode}.
 */
public final class JsonTraverser {
    private static final Logger log = getLogger(JsonTraverser.class);

    private JsonTraverser() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Extracts fields with their values from a JSON node.
     *
     * @param jsonNode The JSON node to extract fields from
     * @return A map of field names to their string representation of values
     */
    public static Map<String, String> extractFieldsWithValues(JsonNode jsonNode) throws IOException {
        var fieldsWithValues = new HashMap<String, String>();
        traverseWithValues(jsonNode, "", fieldsWithValues);
        return fieldsWithValues;
    }

    /**
     * Recursively traverses a JSON node and collects fields with their corresponding string representations
     * of values. Different kinds of JSON nodes (objects, arrays, primitives, etc.) are handled appropriately,
     * and their values are captured along with their paths in the JSON structure.
     *
     * @param currentJsonNode             The current JSON node being traversed.
     * @param path                        The current path in the JSON structure.
     * @param fieldsWithValuesAccumulator A map to collect field paths with their corresponding string representations of values.
     * @throws IOException If an error occurs while processing binary data or during JSON traversal.
     */
    private static void traverseWithValues(JsonNode currentJsonNode,
                                           String path,
                                           Map<String, String> fieldsWithValuesAccumulator) throws IOException {
        try {
            switch (currentJsonNode.getNodeType()) {
                case OBJECT -> {
                    accumulateValueIfPathNotEmpty(currentJsonNode, path, fieldsWithValuesAccumulator);
                    traverseObjectFields(currentJsonNode, path, fieldsWithValuesAccumulator);
                }
                case ARRAY -> {
                    accumulateValueIfPathNotEmpty(currentJsonNode, path, fieldsWithValuesAccumulator);
                    traverseArrayElements(currentJsonNode, path, fieldsWithValuesAccumulator);
                }
                case STRING, NUMBER, BOOLEAN, NULL -> {
                    accumulateValueIfPathNotEmpty(currentJsonNode, path, fieldsWithValuesAccumulator);
                    if (log.isDebugEnabled()) {
                        log.debug("Leaf value captured: {} = {}", path, getValueAsString(currentJsonNode));
                    }
                }
                case BINARY -> {
                    accumulateBinaryValueIfPathNotEmpty(currentJsonNode, path, fieldsWithValuesAccumulator);
                    log.debug("Binary data captured in the path: {}", path);
                }
                case POJO -> {
                    accumulatePojoValueIfPathNotEmpty(currentJsonNode, path, fieldsWithValuesAccumulator);
                    log.debug("POJO captured: {} = {}", path, currentJsonNode);
                }
                case MISSING -> log.warn("Missing node encountered in the path: {}", path);
            }
        } catch (RuntimeException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw e;
        }
    }

    /**
     * Accumulates the string representation of a JSON node's value into the provided map
     * if the given path is not empty. This method helps in collecting values associated
     * with specific paths in a JSON structure.
     *
     * @param node        The JSON node whose value is to be accumulated.
     * @param path        The path corresponding to the JSON node in the structure.
     *                    If the path is empty, no accumulation is performed.
     * @param accumulator A map to collect paths as keys and their corresponding string
     *                    representations of values from the JSON structure.
     */
    private static void accumulateValueIfPathNotEmpty(JsonNode node, String path, Map<String, String> accumulator) {
        if (!path.isEmpty()) {
            accumulator.put(path, getValueAsString(node));
        }
    }

    /**
     * Accumulates information about binary data length from a specified JSON node
     * into the provided map if the given path is not empty. If the path is empty,
     * no accumulation is performed.
     *
     * @param node        The JSON node containing the binary data to be processed.
     * @param path        The path corresponding to the JSON node in the structure.
     *                    Accumulation is only performed if this path is not empty.
     * @param accumulator A map to collect paths as keys and their corresponding
     *                    binary data length representation as values.
     */
    private static void accumulateBinaryValueIfPathNotEmpty(JsonNode node, String path, Map<String, String> accumulator) {
        if (!path.isEmpty()) {
            accumulateBinaryDataLength(node, path, accumulator);
        }
    }

    private static void accumulatePojoValueIfPathNotEmpty(JsonNode node, String path, Map<String, String> accumulator) {
        if (!path.isEmpty()) {
            accumulator.put(path, "[POJO: %s]".formatted(node.toString()));
        }
    }

    private static void traverseObjectFields(JsonNode objectNode, String path, Map<String, String> accumulator) {
        objectNode.fieldNames().forEachRemaining(IOExceptionConsumer.wrapIOException(
                fieldName -> {
                    var fullPath = path.isEmpty() ? fieldName : "%s.%s".formatted(path, fieldName);
                    var fieldValue = objectNode.get(fieldName);
                    traverseWithValues(fieldValue, fullPath, accumulator);
                })
        );
    }

    private static void traverseArrayElements(JsonNode arrayNode, String path, Map<String, String> accumulator) throws IOException {
        for (int i = 0; i < arrayNode.size(); i++) {
            var arrayElement = arrayNode.get(i);
            var arrayPath = path + "[" + i + "]";
            traverseWithValues(arrayElement, arrayPath, accumulator);
        }
    }

    private static void accumulateBinaryDataLength(JsonNode currentJsonNode, String path, Map<String, String> fieldsWithValuesAccumulator) {
        try {
            fieldsWithValuesAccumulator.put(path, "[BINARY DATA: " + currentJsonNode.binaryValue().length + " bytes]");
        } catch (IOException e) {
            fieldsWithValuesAccumulator.put(path, "[BINARY DATA: Unable to read length]");
            log.warn("Error reading binary data in the path: {}", path, e);
        }
    }


    /**
     * Converts the value of the given JsonNode into its string representation.
     * Handles different types of JsonNode, such as null, textual, numeric, boolean,
     * objects, and arrays, returning an appropriately formatted string.
     *
     * @param node The JsonNode to be converted into a string representation.
     * @return The string representation of the given JsonNode. Returns "null" for null nodes,
     * quoted strings for textual nodes, the appropriate string value for numeric
     * and boolean nodes, and the default string conversion for object and array nodes.
     */
    public static String getValueAsString(JsonNode node) {
        return switch (node) {
            case JsonNode n when n.isNull() -> "null";
            case JsonNode n when n.isTextual() -> "\"" + n.asText() + "\"";
            case JsonNode n when n.isNumber() -> n.asText();
            case JsonNode n when n.isBoolean() -> String.valueOf(n.asBoolean());
            case JsonNode n when n.isObject() -> n.toString();
            case JsonNode n when n.isArray() -> n.toString();
            default -> node.toString();
        };
    }
}
