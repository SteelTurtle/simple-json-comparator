#!/bin/bash

# build-and-run.sh - Build and run the JSON Structure Comparator tool
# This script verifies Java 24+ installation and builds/runs the application

set -e  # Exit on any error

# Color codes for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Function to check Java version
check_java_version() {
    print_status "Checking Java installation..."

    if ! command -v java &> /dev/null; then
        print_error "Java is not installed or not in PATH"
        print_error "Please install Java 24 or higher and ensure it's in your PATH"
        exit 1
    fi

    # Get Java version
    JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d '"' -f 2 | cut -d '.' -f 1)

    # Handle different Java version formats
    if [[ "$JAVA_VERSION" =~ ^1\. ]]; then
        JAVA_VERSION=$(echo "$JAVA_VERSION" | cut -d '.' -f 2)
    fi

    print_status "Detected Java version: $JAVA_VERSION"

    if [ "$JAVA_VERSION" -lt 24 ]; then
        print_error "Java 24 or higher is required, but Java $JAVA_VERSION is installed"
        print_error "Please upgrade your Java installation to version 24 or higher"
        exit 1
    fi

    print_success "Java $JAVA_VERSION detected - meets requirements"
}

# Function to check if Maven wrapper exists
check_maven_wrapper() {
    if [ -f "./mvnw" ]; then
        print_status "Maven wrapper found"
        MAVEN_CMD="./mvnw"
    elif command -v mvn &> /dev/null; then
        print_status "System Maven found"
        MAVEN_CMD="mvn"
    else
        print_error "Neither Maven wrapper (mvnw) nor system Maven found"
        print_error "Please install Maven or ensure the Maven wrapper is present"
        exit 1
    fi
}

# Function to build the project
build_project() {
    print_status "Building the project..."

    # Clean and compile
    if ! $MAVEN_CMD clean compile; then
        print_error "Failed to compile the project"
        exit 1
    fi

    # Run tests
    print_status "Running tests..."
    if ! $MAVEN_CMD test; then
        print_warning "Tests failed, but continuing with build..."
    fi

    # Package the application
    print_status "Packaging the application..."
    if ! $MAVEN_CMD package -DskipTests; then
        print_error "Failed to package the application"
        exit 1
    fi

    print_success "Build completed successfully"
}

# Function to find or build the JAR file
ensure_jar_exists() {
    # Look for existing shaded JAR file (exclude original- prefix and test/sources jars)
    JAR_FILE=$(find target -name "*.jar" -not -name "original-*.jar" -not -name "*-tests.jar" -not -name "*-sources.jar" 2>/dev/null | head -1)

    if [ -n "$JAR_FILE" ] && [ -f "$JAR_FILE" ]; then
        print_success "Found existing JAR file: $JAR_FILE"
        print_status "Skipping build as JAR already exists"
        return 0
    fi

    print_status "Executable JAR file not found, building the project..."

    # Check prerequisites for building
    check_maven_wrapper

    # Build the project
    build_project

    # Find the newly generated shaded JAR file (exclude original- prefix)
    JAR_FILE=$(find target -name "*.jar" -not -name "original-*.jar" -not -name "*-tests.jar" -not -name "*-sources.jar" | head -1)

    if [ -z "$JAR_FILE" ]; then
        print_error "Could not find the generated executable JAR file in target directory"
        print_error "Available JAR files:"
        find target -name "*.jar" 2>/dev/null | sed 's/^/  /' || echo "  None found"
        exit 1
    fi

    print_status "Generated JAR file: $JAR_FILE"
}

# Function to validate file arguments
validate_file_arguments() {
    local file1="$1"
    local file2="$2"

    if [ ! -f "$file1" ]; then
        print_error "First file does not exist: $file1"
        exit 1
    fi

    if [ ! -f "$file2" ]; then
        print_error "Second file does not exist: $file2"
        exit 1
    fi

    # Check if files are readable
    if [ ! -r "$file1" ]; then
        print_error "First file is not readable: $file1"
        exit 1
    fi

    if [ ! -r "$file2" ]; then
        print_error "Second file is not readable: $file2"
        exit 1
    fi

    print_success "Both input files validated successfully"
    print_status "File 1: $file1"
    print_status "File 2: $file2"
}

# Function to run the application
run_application() {
    local file1="$1"
    local file2="$2"

    print_status "Running the JSON Structure Comparator..."
    print_status "JAR file: $JAR_FILE"
    print_status "Comparing: $file1 vs $file2"

    java --enable-preview -jar "$JAR_FILE" "$file1" "$file2"
}

# Function to show usage
show_usage() {
    echo "Usage: $0 <file1> <file2>"
    echo ""
    echo "Arguments:"
    echo "  file1                   Path to the first JSON file to compare"
    echo "  file2                   Path to the second JSON file to compare"
    echo ""
    echo "Options:"
    echo "  -h, --help              Show this help message"
    echo "  --force-rebuild         Force rebuild even if JAR exists"
    echo ""
    echo "Description:"
    echo "  This script compares the structure of two JSON files using the JSON Structure Comparator tool."
    echo "  It will automatically build the application if the executable JAR file doesn't exist."
    echo ""
    echo "Examples:"
    echo "  $0 data1.json data2.json"
    echo "  $0 /path/to/file1.json /path/to/file2.json"
    echo "  $0 --force-rebuild file1.json file2.json"
}

# Main script logic
main() {
    echo "================================================="
    echo "  JSON Structure Comparator Build & Run Script"
    echo "================================================="
    echo ""

    # Parse arguments
    FORCE_REBUILD=false
    FILE_ARGS=()

    while [[ $# -gt 0 ]]; do
        case $1 in
            -h|--help)
                show_usage
                exit 0
                ;;
            --force-rebuild)
                FORCE_REBUILD=true
                shift
                ;;
            -*)
                print_error "Unknown option: $1"
                show_usage
                exit 1
                ;;
            *)
                FILE_ARGS+=("$1")
                shift
                ;;
        esac
    done

    # Validate that exactly two file arguments are provided
    if [ ${#FILE_ARGS[@]} -ne 2 ]; then
        print_error "Exactly two file arguments are required"
        echo ""
        show_usage
        exit 1
    fi

    local file1="${FILE_ARGS[0]}"
    local file2="${FILE_ARGS[1]}"

    # Check prerequisites
    check_java_version

    # Validate file arguments
    validate_file_arguments "$file1" "$file2"

    # Handle force rebuild
    if [ "$FORCE_REBUILD" = true ]; then
        print_status "Force rebuild requested - removing existing JAR and rebuilding..."
        rm -f target/*.jar 2>/dev/null || true
        check_maven_wrapper
        build_project
        JAR_FILE=$(find target -name "*.jar" -not -name "original-*.jar" -not -name "*-tests.jar" -not -name "*-sources.jar" | head -1)
        if [ -z "$JAR_FILE" ]; then
            print_error "Could not find the generated executable JAR file in target directory"
            exit 1
        fi
    else
        # Ensure JAR exists (build only if necessary)
        ensure_jar_exists
    fi

    # Run the application
    echo ""
    echo "================================================="
    echo "  Running JSON Structure Comparison"
    echo "================================================="
    run_application "$file1" "$file2"
}

# Run the main function with all arguments
main "$@"
