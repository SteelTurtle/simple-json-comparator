package org.gorillacorp.comparator.utils;

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
}
