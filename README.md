# Simple JSON Comparator

A utility tool for comparing JSON structures and identifying differences between
JSON files.

## Purpose

The Simple JSON Comparator is designed to help developers and testers compare
JSON files to identify structural and value differences. It's particularly
useful for:

- Validating API responses against expected outputs
- Comparing configuration files across environments
- Debugging differences between JSON payloads
- Verifying data transformations

## Current Functionality

The tool currently provides the following features:

- **Structure Comparison**: Compares the structure of two JSON files, ignoring
  the order of fields in objects
- **Detailed Field Comparison**: Identifies fields that are:
  - Common to both files with identical values
  - Common to both files but with different values
  - Present only in the first file
  - Present only in the second file
- **Hierarchical Path Support**: Handles nested objects and arrays with proper
  path notation (e.g., `parent.child` or `array[0].field`)
- **Formatted Output**: Displays comparison results in well-formatted tables:
  - Detailed field-by-field comparison
  - Summary statistics (total fields, common fields, unique fields)
- **Special Type Handling**: Provides special handling for binary data and POJOs

## How to Build

### Prerequisites

- Java 24 or higher (with preview features support)
- Maven 3.6 or higher

### Building the Project

1. Clone the repository:
   ```
   git clone https://github.com/SteelTurtle/simple-json-comparator
   cd simple-json-comparator
   ```

2. Build with Maven (from the project root directory):
   ```
   ./mvwn clean package
   ```

This will create an executable JAR file in the `target` directory named
`json-structure-comparator-1.0.0.jar`.

## How to Run

Run the tool by providing two JSON files to compare:

```
java -jar target/json-structure-comparator-1.0.0.jar file1.json file2.json
```

### Output

The tool will output:

1. A detailed comparison table showing each field, its presence in both files,
   values, and differences
2. A summary table with statistics about common and unique fields
3. The file paths that were compared

### Exit Codes

- `0`: JSON structures are identical
- `1`: JSON structures are different or an error occurred

## Note

This tool is a work in progress, and the functionality may change over time.
Future enhancements may include:

- Support for ignoring specific fields during comparison
- Custom comparison rules for specific data types
- Output in different formats (JSON, CSV, HTML)
- Integration with CI/CD pipelines
- GUI interface

## Dependencies

- Jackson Databind: For JSON parsing and manipulation
- SLF4J: For logging abstraction
- Logback: For logging implementation

## License

[Add license information here]
