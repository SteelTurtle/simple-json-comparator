# Simple JSON Comparator

Utility tool for comparing JSON structures and identifying differences between
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

## Quick Start (Recommended)

The easiest way to get started after cloning this repository is to use the
provided build-and-run script:

### Prerequisites

- Java 24 or higher (with preview features support)
- Maven 3.6 or higher (or use the included Maven wrapper)

### Using the Build Script

1. **Clone the repository:**
   ```bash
   git clone https://github.com/SteelTurtle/simple-json-comparator
   cd simple-json-comparator
   ```

2. **Make the script executable:**
   ```bash
   chmod +x comparator_run.sh
   ```

3. **Run the tool with your JSON files:**
   ```bash
   ./comparator_run.sh file1.json file2.json
   ```

### What the Script Does

The `comparator_run.sh` script automatically:

- ✅ **Verifies Java 24+ installation** - Ensures you have the correct Java
  version
- ✅ **Validates input files** - Checks that both JSON files exist and are
  readable
- ✅ **Smart building** - Only builds the project if the JAR doesn't already
  exist
- ✅ **Handles dependencies** - Uses Maven wrapper or system Maven automatically
- ✅ **Runs with correct flags** - Applies the `--enable-preview` flag required
  for Java 24 features

### Script Options

#### Basic usage:

```bash
  ./comparator_run.sh data1.json data2.json
```

#### Force rebuild of the JAR application:

```bash
  ./comparator_run.sh --force-rebuild file1.json file2.json
```

#### Show help:

```bash
  ./comparator_run.sh --help
```

### Script Benefits

- **Zero configuration** - Just clone and run
- **Automatic setup** - Handles all build prerequisites
- **Error prevention** - Validates everything before running
- **Time saving** - Skips rebuild if JAR already exists
- **User friendly** - Clear error messages and colored output

## Manual Build and Run (Alternative)

If you prefer to build and run manually:

### Building the Project

1. Clone the repository:
   ```bash
     git clone https://github.com/SteelTurtle/simple-json-comparator
     cd simple-json-comparator
   ```

2. Build with Maven (from the project root directory):
   ```bash
    ./mvnw clean package
   ```

This will create an executable JAR file in the `target` directory named
`json-structure-comparator-1.0.0.jar`.

### Running Manually

Copy the generated JAR file to any directory and run the tool by providing two
JSON files to compare:

```bash
  java --enable-preview -jar json-structure-comparator-1.0.0.jar file1.json file2.json
```

## Output

The tool will output:

1. A detailed comparison table showing each field, its presence in both files,
   values, and differences
2. A summary table with statistics about common and unique fields
3. The file paths that were compared

### Exit Codes

- `0`: JSON structures are identical
- `1`: JSON structures are different or an error occurred

## Troubleshooting

### Java Version Issues

If you get Java version errors, ensure you have Java 24+ installed:

### Permission Issues

If you get "permission denied" errors on the script:

```bash
  chmod +x comparator_run.sh
```

### Build Issues

If the build fails, try forcing a clean rebuild:

```bash
  ./comparator_run.sh --force-rebuild file1.json file2.json
```

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
