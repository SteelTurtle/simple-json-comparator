package org.gorillacorp.comparator;

import org.gorillacorp.comparator.processor.JsonComparatorOperations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JsonComparatorOperationsTest {

    @TempDir
    Path tempDir;

    @Test
    void testSimpleObjectWithDifferentFieldOrder() throws IOException {
        var json1 = """
            {
                "name": "John",
                "age": 30,
                "city": "New York"
            }
            """;

        var json2 = """
            {
                "city": "New York",
                "name": "John",
                "age": 30
            }
            """;

        var file1 = createTempJsonFile("test1.json", json1);
        var file2 = createTempJsonFile("test2.json", json2);

        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "JSON objects with same fields in different order should be considered equal");
    }

    @Test
    void testNestedObjectsWithDifferentFieldOrder() throws IOException {
        var json1 = """
            {
                "user": {
                    "name": "Alice",
                    "details": {
                        "age": 25,
                        "email": "alice@example.com"
                    }
                },
                "timestamp": "2023-01-01"
            }
            """;

        var json2 = """
            {
                "timestamp": "2023-01-01",
                "user": {
                    "details": {
                        "email": "alice@example.com",
                        "age": 25
                    },
                    "name": "Alice"
                }
            }
            """;

        var file1 = createTempJsonFile("nested1.json", json1);
        var file2 = createTempJsonFile("nested2.json", json2);

        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Nested JSON objects with same fields in different order should be considered equal");
    }

    @Test
    void testArraysWithSameOrderShouldBeEqual() throws IOException {
        // Arrange
        var json1 = """
            {
                "items": [
                    {"id": 1, "name": "Item A"},
                    {"id": 2, "name": "Item B"}
                ],
                "count": 2
            }
            """;

        var json2 = """
            {
                "count": 2,
                "items": [
                    {"name": "Item A", "id": 1},
                    {"name": "Item B", "id": 2}
                ]
            }
            """;

        var file1 = createTempJsonFile("array1.json", json1);
        var file2 = createTempJsonFile("array2.json", json2);

        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Arrays with objects having different field order should be considered equal");
    }

    @Test
    void testComplexNestedStructureWithDifferentFieldOrder() throws IOException {
        // Arrange
        var json1 = """
            {
                "company": {
                    "name": "Tech Corp",
                    "employees": [
                        {
                            "id": 1,
                            "personal": {
                                "name": "John",
                                "age": 30
                            },
                            "department": "Engineering"
                        }
                    ],
                    "location": "San Francisco"
                },
                "metadata": {
                    "version": "1.0",
                    "created": "2023-01-01"
                }
            }
            """;

        var json2 = """
            {
                "metadata": {
                    "created": "2023-01-01",
                    "version": "1.0"
                },
                "company": {
                    "location": "San Francisco",
                    "employees": [
                        {
                            "department": "Engineering",
                            "personal": {
                                "age": 30,
                                "name": "John"
                            },
                            "id": 1
                        }
                    ],
                    "name": "Tech Corp"
                }
            }
            """;

        var file1 = createTempJsonFile("complex1.json", json1);
        var file2 = createTempJsonFile("complex2.json", json2);

        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Complex nested structures with different field order should be considered equal");
    }

    @Test
    void testEmptyObjectsShouldBeEqual() throws IOException {

        var json1 = "{}";
        var json2 = "{}";

        var file1 = createTempJsonFile("empty1.json", json1);
        var file2 = createTempJsonFile("empty2.json", json2);

        // Act & Assert
        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Empty JSON objects should be considered equal");
    }

    @Test
    void testSingleFieldObjectsShouldBeEqual() throws IOException {
        var json1 = """
            {"name": "test"}
            """;
        var json2 = """
            {"name": "test"}
            """;

        var file1 = createTempJsonFile("single1.json", json1);
        var file2 = createTempJsonFile("single2.json", json2);

        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Single field objects with same content should be considered equal");
    }

    @Test
    void testDifferentValuesShouldNotBeEqual() throws IOException {
        // Arrange
        var json1 = """
            {
                "name": "John",
                "age": 30
            }
            """;

        var json2 = """
            {
                "age": 25,
                "name": "John"
            }
            """;

        var file1 = createTempJsonFile("diff1.json", json1);
        var file2 = createTempJsonFile("diff2.json", json2);

        assertFalse(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Objects with different values should not be considered equal, regardless of field order");
    }

    @Test
    void testDifferentFieldsShouldNotBeEqual() throws IOException {
        var json1 = """
            {
                "name": "John",
                "age": 30
            }
            """;

        var json2 = """
            {
                "email": "john@example.com",
                "name": "John"
            }
            """;

        var file1 = createTempJsonFile("field1.json", json1);
        var file2 = createTempJsonFile("field2.json", json2);

        assertFalse(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Objects with different fields should not be considered equal");
    }

    @Test
    void testMixedTypesWithDifferentOrderShouldBeEqual() throws IOException {
        var json1 = """
            {
                "string": "test",
                "number": 42,
                "boolean": true,
                "nullValue": null,
                "array": [1, 2, 3],
                "object": {"key": "value"}
            }
            """;

        var json2 = """
            {
                "object": {"key": "value"},
                "array": [1, 2, 3],
                "nullValue": null,
                "boolean": true,
                "number": 42,
                "string": "test"
            }
            """;

        var file1 = createTempJsonFile("mixed1.json", json1);
        var file2 = createTempJsonFile("mixed2.json", json2);

        assertTrue(JsonComparatorOperations.compareJson(file1.toString(), file2.toString()),
            "Objects with mixed data types in different field order should be considered equal");
    }

    private Path createTempJsonFile(String fileName, String jsonContent) throws IOException {
        var filePath = tempDir.resolve(fileName);
        Files.writeString(filePath, jsonContent);
        return filePath;
    }
}
