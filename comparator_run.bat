@echo off
setlocal enabledelayedexpansion

REM comparator_run.bat - Build and run the JSON Structure Comparator tool
REM This script verifies Java 24+ installation and builds/runs the application

REM Function to print colored output (Windows doesn't have native color support in basic batch)
:print_status
echo [INFO] %~1
goto :eof

:print_success
echo [SUCCESS] %~1
goto :eof

:print_warning
echo [WARNING] %~1
goto :eof

:print_error
echo [ERROR] %~1
goto :eof

REM Function to check Java version
:check_java_version
call :print_status "Checking Java installation..."

where java >nul 2>&1
if errorlevel 1 (
    call :print_error "Java is not installed or not in PATH"
    call :print_error "Please install Java 24 or higher and ensure it's in your PATH"
    exit /b 1
)

REM Get Java version
for /f "tokens=3" %%i in ('java -version 2^>^&1 ^| findstr /i version') do (
    set "JAVA_VERSION_RAW=%%i"
)
set "JAVA_VERSION_RAW=%JAVA_VERSION_RAW:"=%"

REM Extract major version number
for /f "tokens=1 delims=." %%a in ("%JAVA_VERSION_RAW%") do set "JAVA_VERSION=%%a"
if "%JAVA_VERSION:~0,2%"=="1." (
    for /f "tokens=2 delims=." %%a in ("%JAVA_VERSION_RAW%") do set "JAVA_VERSION=%%a"
)

call :print_status "Detected Java version: %JAVA_VERSION%"

if %JAVA_VERSION% LSS 24 (
    call :print_error "Java 24 or higher is required, but Java %JAVA_VERSION% is installed"
    call :print_error "Please upgrade your Java installation to version 24 or higher"
    exit /b 1
)

call :print_success "Java %JAVA_VERSION% detected - meets requirements"
goto :eof

REM Function to check if Maven wrapper exists
:check_maven_wrapper
if exist "mvnw.cmd" (
    call :print_status "Maven wrapper found"
    set "MAVEN_CMD=mvnw.cmd"
) else (
    where mvn >nul 2>&1
    if not errorlevel 1 (
        call :print_status "System Maven found"
        set "MAVEN_CMD=mvn"
    ) else (
        call :print_error "Neither Maven wrapper (mvnw.cmd) nor system Maven found"
        call :print_error "Please install Maven or ensure the Maven wrapper is present"
        exit /b 1
    )
)
goto :eof

REM Function to build the project
:build_project
call :print_status "Building the project..."

REM Clean and compile
%MAVEN_CMD% clean compile
if errorlevel 1 (
    call :print_error "Failed to compile the project"
    exit /b 1
)

REM Run tests
call :print_status "Running tests..."
%MAVEN_CMD% test
if errorlevel 1 (
    call :print_warning "Tests failed, but continuing with build..."
)

REM Package the application
call :print_status "Packaging the application..."
%MAVEN_CMD% package -DskipTests
if errorlevel 1 (
    call :print_error "Failed to package the application"
    exit /b 1
)

call :print_success "Build completed successfully"
goto :eof

REM Function to find or build the JAR file
:ensure_jar_exists
REM Look for existing JAR file (exclude original- prefix and test/sources jars)
set "JAR_FILE="
for /f "delims=" %%i in ('dir /b target\*.jar 2^>nul ^| findstr /v /i "original-" ^| findstr /v /i "tests.jar" ^| findstr /v /i "sources.jar"') do (
    if not defined JAR_FILE set "JAR_FILE=target\%%i"
)

if defined JAR_FILE (
    if exist "%JAR_FILE%" (
        call :print_success "Found existing JAR file: %JAR_FILE%"
        call :print_status "Skipping build as JAR already exists"
        goto :eof
    )
)

call :print_status "Executable JAR file not found, building the project..."

REM Check prerequisites for building
call :check_maven_wrapper
if errorlevel 1 exit /b 1

REM Build the project
call :build_project
if errorlevel 1 exit /b 1

REM Find the newly generated JAR file
set "JAR_FILE="
for /f "delims=" %%i in ('dir /b target\*.jar 2^>nul ^| findstr /v /i "original-" ^| findstr /v /i "tests.jar" ^| findstr /v /i "sources.jar"') do (
    if not defined JAR_FILE set "JAR_FILE=target\%%i"
)

if not defined JAR_FILE (
    call :print_error "Could not find the generated executable JAR file in target directory"
    call :print_error "Available JAR files:"
    dir /b target\*.jar 2>nul
    exit /b 1
)

call :print_status "Generated JAR file: %JAR_FILE%"
goto :eof

REM Function to validate file arguments
:validate_file_arguments
set "file1=%~1"
set "file2=%~2"
set "csv_output=%~3"

if not exist "%file1%" (
    call :print_error "First file does not exist: %file1%"
    exit /b 1
)

if not exist "%file2%" (
    call :print_error "Second file does not exist: %file2%"
    exit /b 1
)

REM Validate CSV output file if provided
if defined csv_output (
    REM Check if the file ends with .csv (case insensitive)
    set "csv_lower=!csv_output!"
    for %%i in (A B C D E F G H I J K L M N O P Q R S T U V W X Y Z) do call set "csv_lower=%%csv_lower:%%i=%%i%%"
    for %%i in (a b c d e f g h i j k l m n o p q r s t u v w x y z) do call set "csv_lower=%%csv_lower:%%i=%%i%%"

    if not "!csv_lower:~-4!"==".csv" (
        call :print_error "CSV output file must end with .csv extension: %csv_output%"
        exit /b 1
    )

    REM Check if the output directory exists
    for %%F in ("%csv_output%") do set "output_dir=%%~dpF"
    if not exist "!output_dir!" (
        call :print_error "Output directory does not exist: !output_dir!"
        exit /b 1
    )

    call :print_success "CSV output file validated: %csv_output%"
)

call :print_success "Input files validated successfully"
call :print_status "File 1: %file1%"
call :print_status "File 2: %file2%"
if defined csv_output (
    call :print_status "CSV Output: %csv_output%"
)
goto :eof

REM Function to run the application
:run_application
set "file1=%~1"
set "file2=%~2"
set "csv_output=%~3"

call :print_status "Running the JSON Structure Comparator..."
call :print_status "JAR file: %JAR_FILE%"

if defined csv_output (
    call :print_status "Comparing: %file1% vs %file2% (exporting to CSV: %csv_output%)"
    java --enable-preview -jar "%JAR_FILE%" "%file1%" "%file2%" -export-to-csv "%csv_output%"
) else (
    call :print_status "Comparing: %file1% vs %file2%"
    java --enable-preview -jar "%JAR_FILE%" "%file1%" "%file2%"
)
goto :eof

REM Function to show usage
:show_usage
echo Usage: %~n0 ^<file1^> ^<file2^> [-export-to-csv ^<output.csv^>]
echo.
echo Arguments:
echo   file1                   Path to the first JSON file to compare
echo   file2                   Path to the second JSON file to compare
echo.
echo Options:
echo   -export-to-csv FILE     Export comparison results to CSV file (optional)
echo   /h, /help               Show this help message
echo   /force-rebuild          Force rebuild even if JAR exists
echo.
echo Description:
echo   This script compares the structure of two JSON files using the JSON Structure Comparator tool.
echo   It will automatically build the application if the executable JAR file doesn't exist.
echo   By default, results are displayed in the terminal. Use -export-to-csv to save results to a CSV file.
echo.
echo Examples:
echo   %~n0 data1.json data2.json
echo   %~n0 file1.json file2.json -export-to-csv report.csv
echo   %~n0 C:\path\to\file1.json C:\path\to\file2.json -export-to-csv C:\path\to\output.csv
echo   %~n0 /force-rebuild file1.json file2.json
goto :eof

REM Main script logic
:main
echo =================================================
echo   JSON Structure Comparator Build ^& Run Script
echo =================================================
echo.

REM Parse arguments
set "FORCE_REBUILD=false"
set "CSV_OUTPUT="
set "file1="
set "file2="
set "argcount=0"

:parse_args
if "%~1"=="" goto args_parsed
if /i "%~1"=="/h" goto show_help
if /i "%~1"=="/help" goto show_help
if /i "%~1"=="/force-rebuild" (
    set "FORCE_REBUILD=true"
    shift
    goto parse_args
)
if /i "%~1"=="-export-to-csv" (
    if "%~2"=="" (
        call :print_error "Missing CSV output file path after '-export-to-csv'"
        goto show_help
    )
    set "CSV_OUTPUT=%~2"
    shift
    shift
    goto parse_args
)
if "%~1" NEQ "" (
    set /a argcount+=1
    if !argcount!==1 set "file1=%~1"
    if !argcount!==2 set "file2=%~1"
    if !argcount! GTR 2 (
        call :print_error "Too many arguments provided"
        goto show_help
    )
)
shift
goto parse_args

:show_help
call :show_usage
exit /b 0

:args_parsed
REM Validate that exactly two file arguments are provided
if !argcount! NEQ 2 (
    call :print_error "Exactly two file arguments are required"
    echo.
    call :show_usage
    exit /b 1
)

REM Check prerequisites
call :check_java_version
if errorlevel 1 exit /b 1

REM Validate file arguments
call :validate_file_arguments "%file1%" "%file2%" "%CSV_OUTPUT%"
if errorlevel 1 exit /b 1

REM Handle force rebuild
if "%FORCE_REBUILD%"=="true" (
    call :print_status "Force rebuild requested - removing existing JAR and rebuilding..."
    del /q target\*.jar 2>nul
    call :check_maven_wrapper
    if errorlevel 1 exit /b 1
    call :build_project
    if errorlevel 1 exit /b 1

    set "JAR_FILE="
    for /f "delims=" %%i in ('dir /b target\*.jar 2^>nul ^| findstr /v /i "original-" ^| findstr /v /i "tests.jar" ^| findstr /v /i "sources.jar"') do (
        if not defined JAR_FILE set "JAR_FILE=target\%%i"
    )

    if not defined JAR_FILE (
        call :print_error "Could not find the generated executable JAR file in target directory"
        exit /b 1
    )
) else (
    REM Ensure JAR exists (build only if necessary)
    call :ensure_jar_exists
    if errorlevel 1 exit /b 1
)

REM Run the application
echo.
echo =================================================
if defined CSV_OUTPUT (
    echo   Running JSON Structure Comparison (CSV Export)
) else (
    echo   Running JSON Structure Comparison
)
echo =================================================
call :run_application "%file1%" "%file2%" "%CSV_OUTPUT%"

goto :eof

REM Entry point
call :main %*
