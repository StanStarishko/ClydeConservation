package com.conservation.exception;

/**
 * Exception thrown when persistence operations (save/load) fail.
 * 
 * Common scenarios:
 * - XML file not found or inaccessible
 * - XML parsing errors
 * - XSD schema validation failures
 * - File I/O errors (permissions, disk space)
 * - Corrupted data files
 * 
 * Stores the file path that caused the error for better error reporting.
 */
public class PersistenceException extends ConservationException {
    
    private final String filePath;
    
    /**
     * Constructs a PersistenceException with error message.
     * 
     * @param message detailed error message
     */
    public PersistenceException(String message) {
        super(message);
        this.filePath = null;
    }
    
    /**
     * Constructs a PersistenceException with message and file path.
     * 
     * Example: throw new PersistenceException(
     *     "Failed to parse XML file",
     *     "data/animals.xml"
     * );
     * 
     * @param message detailed error message
     * @param filePath path to the file that caused the error
     */
    public PersistenceException(String message, String filePath) {
        super(message + " [File: " + filePath + "]");
        this.filePath = filePath;
    }
    
    /**
     * Constructs a PersistenceException with message, file path and underlying cause.
     * 
     * Used when wrapping lower-level exceptions (IOException, SAXException, etc.)
     * 
     * Example: throw new PersistenceException(
     *     "Failed to save animals",
     *     "data/animals.xml",
     *     ioException
     * );
     * 
     * @param message detailed error message
     * @param filePath path to the file that caused the error
     * @param cause the underlying exception
     */
    public PersistenceException(String message, String filePath, Throwable cause) {
        super(message + " [File: " + filePath + "]", cause);
        this.filePath = filePath;
    }
    
    /**
     * Gets the file path that caused this exception.
     * 
     * @return file path, or null if not specified
     */
    public String getFilePath() {
        return filePath;
    }
}
